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

package com.navercorp.pinpoint.otlp.trace.linkindex.suppress;

import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexConnection;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The suppression list in the link index store: one hash, field = {@link SuppressKey}, value =
 * {@link SuppressStateCodec}. The join job writes it; the collectors read the ACTIVE fields.
 * The hash has no expiry: the job removes released and idle identifiers and caps its size.
 * Thread-safe: the connection auto-flushes and Lettuce multiplexes concurrent commands.
 */
public class SuppressStore {

    public static final byte[] KEY = "pinpoint:otlp:link:suppress".getBytes(StandardCharsets.US_ASCII);

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final RedisClusterAsyncCommands<byte[], byte[]> commands;
    private final Duration timeout;
    private final AtomicLong undecodable = new AtomicLong();

    public SuppressStore(LinkIndexConnection connection, Duration timeout) {
        this.commands = Objects.requireNonNull(connection, "connection").async();
        this.timeout = Objects.requireNonNull(timeout, "timeout");
    }

    /**
     * @return every identifier with its state; fields that cannot be decoded are skipped and counted
     */
    public Map<String, SuppressState> readAll() {
        final Map<byte[], byte[]> hash = await(commands.hgetall(KEY));
        if (hash == null || hash.isEmpty()) {
            return Map.of();
        }
        final Map<String, SuppressState> states = new HashMap<>(hash.size());
        for (Map.Entry<byte[], byte[]> field : hash.entrySet()) {
            try {
                states.put(new String(field.getKey(), StandardCharsets.UTF_8), SuppressStateCodec.decode(field.getValue()));
            } catch (RuntimeException e) {
                final long count = undecodable.incrementAndGet();
                if (count == 1 || count % 1_000 == 0) {
                    logger.warn("Skip undecodable suppression field. total:{} cause:{}", count, e.getMessage());
                }
            }
        }
        return states;
    }

    /**
     * @return the identifiers the collectors must suppress
     */
    public Set<String> readActive() {
        final Map<String, SuppressState> states = readAll();
        if (states.isEmpty()) {
            return Set.of();
        }
        final Set<String> active = new HashSet<>();
        for (Map.Entry<String, SuppressState> entry : states.entrySet()) {
            if (entry.getValue().isActive()) {
                active.add(entry.getKey());
            }
        }
        return active;
    }

    /**
     * One HSET for the upserts, one HDEL for the deletes; a key must not be in both.
     */
    public void write(Map<String, SuppressState> upserts, Collection<String> deletes) {
        Objects.requireNonNull(upserts, "upserts");
        Objects.requireNonNull(deletes, "deletes");
        RedisFuture<Long> set = null;
        RedisFuture<Long> del = null;
        if (!upserts.isEmpty()) {
            final Map<byte[], byte[]> fields = new HashMap<>(upserts.size());
            for (Map.Entry<String, SuppressState> entry : upserts.entrySet()) {
                fields.put(field(entry.getKey()), SuppressStateCodec.encode(entry.getValue()));
            }
            set = commands.hset(KEY, fields);
        }
        if (!deletes.isEmpty()) {
            final byte[][] fields = new byte[deletes.size()][];
            int i = 0;
            for (String key : deletes) {
                fields[i++] = field(key);
            }
            del = commands.hdel(KEY, fields);
        }
        if (set != null) {
            await(set);
        }
        if (del != null) {
            await(del);
        }
    }

    public long getUndecodable() {
        return undecodable.get();
    }

    static byte[] field(String key) {
        return key.getBytes(StandardCharsets.UTF_8);
    }

    private <T> T await(RedisFuture<T> future) {
        try {
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the suppression list", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Suppression list command failed", e.getCause());
        } catch (TimeoutException e) {
            throw new IllegalStateException("Suppression list command timed out after " + timeout, e);
        }
    }
}
