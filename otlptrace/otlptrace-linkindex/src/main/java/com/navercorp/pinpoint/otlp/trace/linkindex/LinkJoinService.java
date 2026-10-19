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

import com.navercorp.pinpoint.common.trace.ServiceType;
import io.lettuce.core.RedisFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.IntFunction;

/**
 * Join and cleanup of one slot, the two steps of the join job.
 * <ul>
 *     <li>{@link #join(long)}: read every bucket of slots k and k-1, count the calls with at least one
 *     side in k ({@link LinkJoiner}), aggregate and write the server map. It writes nothing to the
 *     store and deletes nothing, so a failed join can be re-run from the start with the same result.</li>
 *     <li>{@link #cleanup(long)}: delete the entry keys of k-1. The keys of k stay for the job of k+1.</li>
 * </ul>
 */
public class LinkJoinService implements AutoCloseable {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /** default buckets read and written per round trip of one worker */
    public static final int DEFAULT_CHUNK_BUCKETS = 16;
    /** default bucket size above which a slot is reported: the store's hash-max-ziplist-entries */
    public static final int DEFAULT_BUCKET_WARN_ENTRIES = 512;

    private final LinkIndexStore store;
    private final LinkMapWriter writer;
    private final IntFunction<ServiceType> serviceTypeLookup;
    private final int buckets;
    private final int threads;
    private final int sampleModulo;
    private final int chunkBuckets;
    private final int bucketWarnEntries;
    private final ExecutorService executor;

    // running totals over every join / cleanup of this instance, for the caller's metrics
    private final LongAdder totalEntries = new LongAdder();
    private final LongAdder totalPairs = new LongAdder();
    private final LongAdder totalLinks = new LongAdder();
    private final LongAdder totalRows = new LongAdder();
    private final LongAdder totalUnmatched = new LongAdder();
    private final LongAdder totalDroppedServer = new LongAdder();
    private final LongAdder totalDroppedClient = new LongAdder();
    private final LongAdder totalDeletedKeys = new LongAdder();
    private volatile long lastMaxBucketEntries;
    private volatile long lastOversizedBuckets;

    public LinkJoinService(LinkIndexStore store,
                           LinkMapWriter writer,
                           IntFunction<ServiceType> serviceTypeLookup,
                           int buckets,
                           int threads,
                           int sampleModulo) {
        this(store, writer, serviceTypeLookup, buckets, threads, sampleModulo,
                DEFAULT_CHUNK_BUCKETS, DEFAULT_BUCKET_WARN_ENTRIES);
    }

    /**
     * @param chunkBuckets      buckets per round trip of one worker. Each worker keeps
     *                          {@code chunkBuckets * 2} HGETALL replies (slots k and k-1) in flight, so the store buffers at
     *                          most {@code threads * chunkBuckets * 2} replies for this job: keep it small so
     *                          a pause of the job (GC) does not grow the store's output buffers
     * @param bucketWarnEntries a slot with a bucket above this many entries is reported: such a hash is
     *                          no longer compactly encoded and its HGETALL becomes a slow command
     */
    public LinkJoinService(LinkIndexStore store,
                           LinkMapWriter writer,
                           IntFunction<ServiceType> serviceTypeLookup,
                           int buckets,
                           int threads,
                           int sampleModulo,
                           int chunkBuckets,
                           int bucketWarnEntries) {
        this.store = Objects.requireNonNull(store, "store");
        this.writer = Objects.requireNonNull(writer, "writer");
        this.serviceTypeLookup = Objects.requireNonNull(serviceTypeLookup, "serviceTypeLookup");
        if (buckets < 1 || threads < 1 || sampleModulo < 1 || chunkBuckets < 1 || bucketWarnEntries < 1) {
            throw new IllegalArgumentException("buckets, threads, sampleModulo, chunkBuckets and bucketWarnEntries must be positive");
        }
        this.chunkBuckets = chunkBuckets;
        this.bucketWarnEntries = bucketWarnEntries;
        this.buckets = buckets;
        this.threads = threads;
        this.sampleModulo = sampleModulo;
        final AtomicInteger threadIndex = new AtomicInteger();
        this.executor = Executors.newFixedThreadPool(threads, r -> {
            final Thread thread = new Thread(r, "otlp-link-join-" + threadIndex.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
    }

    public JoinSummary join(long slot) {
        final LongAdder entries = new LongAdder();
        final LongAccumulator maxBucketEntries = new LongAccumulator(Math::max, 0);
        final LongAdder oversizedBuckets = new LongAdder();
        final LongAdder unmatched = new LongAdder();
        final LongAdder droppedServer = new LongAdder();
        final LongAdder droppedClient = new LongAdder();

        final List<Future<LinkAggregator>> futures = new ArrayList<>(threads);
        for (int worker = 0; worker < threads; worker++) {
            final int from = rangeStart(worker);
            final int to = rangeStart(worker + 1);
            futures.add(executor.submit(() -> {
                final LinkAggregator aggregator = new LinkAggregator(serviceTypeLookup, sampleModulo);
                final JoinCounters counters = new JoinCounters(entries, maxBucketEntries, oversizedBuckets, unmatched, droppedServer, droppedClient);
                for (int chunkStart = from; chunkStart < to; chunkStart += chunkBuckets) {
                    final int chunkEnd = Math.min(chunkStart + chunkBuckets, to);
                    joinChunk(slot, chunkStart, chunkEnd, aggregator, counters);
                }
                return aggregator;
            }));
        }

        final LinkAggregator total = new LinkAggregator(serviceTypeLookup, sampleModulo);
        for (Future<LinkAggregator> future : futures) {
            total.merge(get(future));
        }
        final Map<LinkKey, LinkStat> stats = total.getStats();
        long pairs = 0;
        for (LinkStat stat : stats.values()) {
            pairs += stat.getCount();
        }
        final int rows = writer.write(stats);
        final JoinSummary summary = new JoinSummary(slot, entries.sum(), pairs, stats.size(), rows,
                unmatched.sum(), droppedServer.sum(), droppedClient.sum(), maxBucketEntries.get(), oversizedBuckets.sum());
        record(summary);
        logger.info("Joined {}", summary);
        if (summary.oversizedBuckets() > 0) {
            logger.warn("{} buckets of slot {} hold more than {} entries (max {}): raise the bucket count of the "
                            + "collectors and the job together so a bucket stays compactly encoded and its HGETALL stays fast",
                    summary.oversizedBuckets(), slot, bucketWarnEntries, summary.maxBucketEntries());
        }
        return summary;
    }

    private void joinChunk(long slot, int chunkStart, int chunkEnd, LinkAggregator aggregator, JoinCounters counters) {
        final int size = chunkEnd - chunkStart;
        final List<RedisFuture<Map<byte[], byte[]>>> currentReads = new ArrayList<>(size);
        final List<RedisFuture<Map<byte[], byte[]>>> previousReads = new ArrayList<>(size);
        for (int bucket = chunkStart; bucket < chunkEnd; bucket++) {
            currentReads.add(store.readAsync(LinkIndexKeys.entryKey(slot, bucket)));
            previousReads.add(store.readAsync(LinkIndexKeys.entryKey(slot - 1, bucket)));
        }

        for (int i = 0; i < size; i++) {
            final List<LinkEntry> current = store.decode(store.await(currentReads.get(i)));
            final List<LinkEntry> previous = store.decode(store.await(previousReads.get(i)));
            counters.entries().add(current.size());
            counters.maxBucketEntries().accumulate(current.size());
            if (current.size() > bucketWarnEntries) {
                counters.oversizedBuckets().increment();
            }
            final JoinResult result = LinkJoiner.join(slot, current, previous);
            for (MatchedPair pair : result.pairs()) {
                aggregator.add(pair);
            }
            counters.unmatched().add(result.unmatched().size());
            counters.droppedServer().add(result.droppedServer());
            counters.droppedClient().add(result.droppedClient());
        }
    }

    public CleanupSummary cleanup(long slot) {
        final List<Future<Long>> futures = new ArrayList<>(threads);
        for (int worker = 0; worker < threads; worker++) {
            final int from = rangeStart(worker);
            final int to = rangeStart(worker + 1);
            futures.add(executor.submit(() -> {
                long deleted = 0;
                for (int chunkStart = from; chunkStart < to; chunkStart += chunkBuckets) {
                    final int chunkEnd = Math.min(chunkStart + chunkBuckets, to);
                    final List<RedisFuture<Long>> unlinks = new ArrayList<>(chunkEnd - chunkStart);
                    for (int bucket = chunkStart; bucket < chunkEnd; bucket++) {
                        unlinks.add(store.unlinkAsync(LinkIndexKeys.entryKey(slot - 1, bucket)));
                    }
                    for (RedisFuture<Long> unlink : unlinks) {
                        final Long count = store.await(unlink);
                        deleted += count == null ? 0 : count;
                    }
                }
                return deleted;
            }));
        }
        long deleted = 0;
        for (Future<Long> future : futures) {
            deleted += get(future);
        }
        final CleanupSummary summary = new CleanupSummary(slot, deleted);
        totalDeletedKeys.add(deleted);
        logger.info("Cleaned {}", summary);
        return summary;
    }

    private void record(JoinSummary summary) {
        totalEntries.add(summary.entries());
        totalPairs.add(summary.pairs());
        totalLinks.add(summary.links());
        totalRows.add(summary.rows());
        totalUnmatched.add(summary.unmatched());
        totalDroppedServer.add(summary.droppedServer());
        totalDroppedClient.add(summary.droppedClient());
        lastMaxBucketEntries = summary.maxBucketEntries();
        lastOversizedBuckets = summary.oversizedBuckets();
    }

    /** entries read from slot k, summed over every join */
    public long getTotalEntries() {
        return totalEntries.sum();
    }

    /** matched calls, summed over every join */
    public long getTotalPairs() {
        return totalPairs.sum();
    }

    /** distinct InLink rows, summed over every join */
    public long getTotalLinks() {
        return totalLinks.sum();
    }

    /** HBase puts, summed over every join */
    public long getTotalRows() {
        return totalRows.sum();
    }

    /** entries without a partner at join time, summed over every join */
    public long getTotalUnmatched() {
        return totalUnmatched.sum();
    }

    /** SERVER entries of k-1 that never matched, summed over every join */
    public long getTotalDroppedServer() {
        return totalDroppedServer.sum();
    }

    /** CLIENT entries of k-1 that never matched, summed over every join */
    public long getTotalDroppedClient() {
        return totalDroppedClient.sum();
    }

    /** keys deleted, summed over every cleanup */
    public long getTotalDeletedKeys() {
        return totalDeletedKeys.sum();
    }

    /** entries of the largest bucket of the last joined slot */
    public long getLastMaxBucketEntries() {
        return lastMaxBucketEntries;
    }

    /** buckets above the warn threshold in the last joined slot */
    public long getLastOversizedBuckets() {
        return lastOversizedBuckets;
    }

    private int rangeStart(int worker) {
        return (int) ((long) buckets * worker / threads);
    }

    private static <T> T get(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted", e);
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException(cause);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    /**
     * @param entries        entries read from slot k
     * @param pairs          matched calls (weighted by the sampling modulo)
     * @param links          distinct InLink rows
     * @param rows           HBase puts (InLink + MapAppHost)
     * @param unmatched      entries of k without a partner yet (read again by the job of k+1)
     * @param droppedServer  SERVER entries of k-1 that never matched
     * @param droppedClient  CLIENT entries of k-1 that never matched
     * @param maxBucketEntries  entries of the largest bucket of k
     * @param oversizedBuckets  buckets of k above the warn threshold
     */
    public record JoinSummary(long slot, long entries, long pairs, int links, int rows,
                              long unmatched, long droppedServer, long droppedClient,
                              long maxBucketEntries, long oversizedBuckets) {
    }

    private record JoinCounters(LongAdder entries, LongAccumulator maxBucketEntries, LongAdder oversizedBuckets,
                                LongAdder unmatched, LongAdder droppedServer, LongAdder droppedClient) {
    }

    public record CleanupSummary(long slot, long deletedKeys) {
    }
}
