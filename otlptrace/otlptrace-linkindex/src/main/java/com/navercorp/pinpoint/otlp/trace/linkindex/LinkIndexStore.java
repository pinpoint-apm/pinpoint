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
import io.lettuce.core.RedisFuture;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Read and delete operations of the join job on the link index. The job writes nothing to the store.
 * Thread-safe: the connection auto-flushes and Lettuce multiplexes concurrent commands.
 */
public class LinkIndexStore {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final RedisClusterAsyncCommands<byte[], byte[]> commands;
    private final Duration timeout;
    private final AtomicLong undecodable = new AtomicLong();

    public LinkIndexStore(LinkIndexConnection connection, Duration timeout) {
        this.commands = Objects.requireNonNull(connection, "connection").async();
        this.timeout = Objects.requireNonNull(timeout, "timeout");
    }

    public RedisFuture<Map<byte[], byte[]>> readAsync(byte[] key) {
        return commands.hgetall(key);
    }

    /**
     * Decodes a hash read by {@link #readAsync}. Cells that cannot be decoded are skipped and counted.
     */
    public List<LinkEntry> decode(Map<byte[], byte[]> hash) {
        if (hash == null || hash.isEmpty()) {
            return List.of();
        }
        final List<LinkEntry> entries = new ArrayList<>(hash.size());
        for (Map.Entry<byte[], byte[]> cell : hash.entrySet()) {
            try {
                entries.add(LinkEntryCodec.decode(cell.getKey(), cell.getValue()));
            } catch (RuntimeException e) {
                final long count = undecodable.incrementAndGet();
                if (count == 1 || count % 10_000 == 0) {
                    logger.warn("Skip undecodable link index cell. total:{} cause:{}", count, e.getMessage());
                }
            }
        }
        return entries;
    }

    /**
     * One UNLINK per key: keys of different buckets live in different cluster hash slots
     * and a multi-key UNLINK would fail with CROSSSLOT.
     */
    public RedisFuture<Long> unlinkAsync(byte[] key) {
        return commands.unlink(key);
    }

    public <T> T await(RedisFuture<T> future) {
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the link index", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Link index command failed", e.getCause());
        } catch (TimeoutException e) {
            throw new IllegalStateException("Link index command timed out after " + timeout, e);
        }
    }

    public void awaitAll(List<? extends RedisFuture<?>> futures) {
        for (RedisFuture<?> future : futures) {
            await(future);
        }
    }

    public long getUndecodable() {
        return undecodable.get();
    }
}
