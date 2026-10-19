/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.otlp.trace.linkindex;

import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexConnection;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * Collector-side writer of the link index.
 * <p>
 * {@link #offer} never blocks the ingest thread: entries go to a bounded queue and are dropped
 * (and counted) when it is full. A single writer thread drains the queue and sends pipelined
 * HSETs on its own connection.
 * <p>
 * Expiry: the writer cannot tell from HSET whether it created the key and every collector writes
 * the same keys, so each writer sends {@code EXPIREAT key} once per (slot, bucket), tracked by a bit
 * set per slot. The absolute time ({@link LinkIndexKeys#expireAtSeconds}, spread per bucket) keeps the
 * expiry fixed however many collectors send it.
 * <p>
 * Late writes: the cleanup of the job of slot k+1 runs in slot k+2 and deletes the keys of k. An entry of
 * slot k still queued at that point would recreate its key, without EXPIREAT when this writer had already
 * sent it for that bucket, and no later cleanup deletes k again. So an entry whose slot is older than the
 * previous slot at write time is dropped and counted ({@link #getDroppedLate}); it would not have been
 * joined anyway.
 */
public class LinkIndexWriter implements AutoCloseable {

    private final Logger logger = LogManager.getLogger(this.getClass());

    static final int BATCH_SIZE = 1000;
    private static final long POLL_MILLIS = 100;

    private final LinkIndexConnection connection;
    private final int buckets;
    private final long ttlMillis;
    private final int maxEntriesPerSlot;
    private final int sampleModulo;
    private final Duration timeout;
    private final LongSupplier clock;
    private final BlockingQueue<Pending> queue;
    private final Thread thread;

    private volatile boolean running = true;

    // writer thread only
    private long expireSlot = Long.MIN_VALUE;
    private BitSet expireSent;
    private BitSet previousExpireSent;

    // ingest threads
    private final Object slotLock = new Object();
    private long countSlot = Long.MIN_VALUE;
    private int countInSlot;

    private final AtomicLong offered = new AtomicLong();
    private final AtomicLong notSampled = new AtomicLong();
    private final AtomicLong droppedQueueFull = new AtomicLong();
    private final AtomicLong droppedSlotLimit = new AtomicLong();
    private final AtomicLong droppedLate = new AtomicLong();
    private final AtomicLong written = new AtomicLong();
    private final AtomicLong expireAtSent = new AtomicLong();
    private final AtomicLong writeErrors = new AtomicLong();

    public LinkIndexWriter(LinkIndexConnection connection,
                           int buckets,
                           long ttlMillis,
                           int queueSize,
                           int maxEntriesPerSlot,
                           int sampleModulo,
                           Duration timeout,
                           LongSupplier clock) {
        this.connection = Objects.requireNonNull(connection, "connection");
        if (buckets < 1 || ttlMillis < 1 || queueSize < 1 || maxEntriesPerSlot < 1 || sampleModulo < 1) {
            throw new IllegalArgumentException("buckets, ttl, queueSize, maxEntriesPerSlot and sampleModulo must be positive");
        }
        this.buckets = buckets;
        this.ttlMillis = ttlMillis;
        this.maxEntriesPerSlot = maxEntriesPerSlot;
        this.sampleModulo = sampleModulo;
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.queue = new ArrayBlockingQueue<>(queueSize);
        this.connection.setAutoFlushCommands(false);
        this.thread = new Thread(this::run, "otlp-link-index-writer");
        this.thread.setDaemon(true);
    }

    public void start() {
        thread.start();
    }

    /**
     * @return {@code true} if the entry was queued
     */
    public boolean offer(LinkEntry entry) {
        offered.incrementAndGet();
        if (!LinkIndexKeys.isSampled(entry.traceId(), sampleModulo)) {
            notSampled.incrementAndGet();
            return false;
        }
        final long slot = LinkIndexKeys.slotOf(clock.getAsLong());
        if (!acquireSlotQuota(slot)) {
            droppedSlotLimit.incrementAndGet();
            return false;
        }
        if (!queue.offer(new Pending(entry, slot))) {
            droppedQueueFull.incrementAndGet();
            return false;
        }
        return true;
    }

    private boolean acquireSlotQuota(long slot) {
        synchronized (slotLock) {
            if (slot != countSlot) {
                countSlot = slot;
                countInSlot = 0;
            }
            if (countInSlot >= maxEntriesPerSlot) {
                return false;
            }
            countInSlot++;
            return true;
        }
    }

    private void run() {
        final List<Pending> batch = new ArrayList<>(BATCH_SIZE);
        while (running || !queue.isEmpty()) {
            try {
                final Pending first = queue.poll(POLL_MILLIS, TimeUnit.MILLISECONDS);
                if (first == null) {
                    continue;
                }
                batch.add(first);
                queue.drainTo(batch, BATCH_SIZE - 1);
                write(batch);
            } catch (InterruptedException e) {
                if (!running) {
                    break;
                }
            } catch (RuntimeException e) {
                logger.warn("Link index write loop failed", e);
            } finally {
                batch.clear();
            }
        }
    }

    /**
     * @return {@code true} if an entry of {@code slot} must not be written any more: the cleanup of
     * {@code slot + 1}, which deletes the keys of {@code slot}, may already have run
     */
    static boolean isLate(long slot, long currentSlot) {
        return slot < currentSlot - 1;
    }

    void write(List<Pending> batch) {
        final long currentSlot = LinkIndexKeys.slotOf(clock.getAsLong());
        final RedisClusterAsyncCommands<byte[], byte[]> commands = connection.async();
        final List<RedisFuture<?>> futures = new ArrayList<>(batch.size() + 16);
        final List<long[]> expireMarked = new ArrayList<>();
        int sent = 0;
        for (Pending pending : batch) {
            if (isLate(pending.slot(), currentSlot)) {
                droppedLate.incrementAndGet();
                continue;
            }
            sent++;
            final LinkEntry entry = pending.entry();
            final int bucket = LinkIndexKeys.bucketOf(entry.traceId(), buckets);
            final byte[] key = LinkIndexKeys.entryKey(pending.slot(), bucket);
            futures.add(commands.hset(key, LinkEntryCodec.encodeField(entry), LinkEntryCodec.encodeValue(entry)));
            if (markExpire(pending.slot(), bucket)) {
                futures.add(commands.expireat(key, LinkIndexKeys.expireAtSeconds(pending.slot(), bucket, ttlMillis)));
                expireMarked.add(new long[]{pending.slot(), bucket});
            }
        }
        if (sent == 0) {
            return;
        }
        connection.flushCommands();

        final String failure = await(futures);
        if (failure != null) {
            final long errors = writeErrors.incrementAndGet();
            if (errors == 1 || errors % 100 == 0) {
                logger.warn("Link index write failed. batch:{} totalFailedBatches:{} cause:{}", sent, errors, failure);
            }
            // send EXPIREAT again with the next write to these buckets
            for (long[] marked : expireMarked) {
                unmarkExpire(marked[0], (int) marked[1]);
            }
            return;
        }
        written.addAndGet(sent);
        expireAtSent.addAndGet(expireMarked.size());
    }

    /**
     * @return {@code null} when every command of the batch succeeded, otherwise why not. {@code awaitAll}
     * returns {@code false} only on a timeout; a failed command (WRONGTYPE, a dropped connection, an
     * interrupt) makes it throw, and that path is a failure too: the EXPIREAT bookkeeping must be undone
     * or a later write to the bucket would recreate the key without an expiry.
     */
    private String await(List<RedisFuture<?>> futures) {
        try {
            if (!LettuceFutures.awaitAll(timeout.toMillis(), TimeUnit.MILLISECONDS, futures.toArray(new RedisFuture[0]))) {
                return "timeout after " + timeout;
            }
        } catch (RuntimeException e) {
            return e.toString();
        }
        for (RedisFuture<?> future : futures) {
            if (future.getError() != null) {
                return future.getError();
            }
        }
        return null;
    }

    /**
     * @return {@code true} if EXPIREAT has not been sent for this (slot, bucket) yet
     */
    private boolean markExpire(long slot, int bucket) {
        final BitSet bits = expireBits(slot);
        if (bits == null) {
            // a backlog older than the previous slot: no bookkeeping, always send so no key is left without expiry
            return true;
        }
        if (bits.get(bucket)) {
            return false;
        }
        bits.set(bucket);
        return true;
    }

    private void unmarkExpire(long slot, int bucket) {
        final BitSet bits = expireBits(slot);
        if (bits != null) {
            bits.clear(bucket);
        }
    }

    private BitSet expireBits(long slot) {
        if (slot > expireSlot) {
            previousExpireSent = slot == expireSlot + 1 ? expireSent : null;
            expireSent = new BitSet(buckets);
            expireSlot = slot;
            return expireSent;
        }
        if (slot == expireSlot) {
            return expireSent;
        }
        if (slot == expireSlot - 1) {
            return previousExpireSent;
        }
        return null;
    }

    public long getOffered() {
        return offered.get();
    }

    public long getNotSampled() {
        return notSampled.get();
    }

    public long getDroppedQueueFull() {
        return droppedQueueFull.get();
    }

    public long getDroppedSlotLimit() {
        return droppedSlotLimit.get();
    }

    /** entries dropped at write time because their slot was older than the previous slot ({@link #isLate}) */
    public long getDroppedLate() {
        return droppedLate.get();
    }

    public long getWritten() {
        return written.get();
    }

    public long getExpireAtSent() {
        return expireAtSent.get();
    }

    public long getWriteErrors() {
        return writeErrors.get();
    }

    public int getQueueSize() {
        return queue.size();
    }

    @Override
    public void close() {
        running = false;
        thread.interrupt();
        try {
            thread.join(timeout.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        connection.close();
    }

    record Pending(LinkEntry entry, long slot) {
    }
}
