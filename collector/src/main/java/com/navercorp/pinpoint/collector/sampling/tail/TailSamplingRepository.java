/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.sampling.tail;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Binary-safe Lua-eval Redis repository for tail sampling.
 * Keys: buffer:{txid} (list), decision:{txid} (string), pending (zset).
 */
public class TailSamplingRepository {

    static final String BUFFER_PREFIX = "buffer:";
    static final String DECISION_PREFIX = "decision:";
    static final String PENDING_KEY = "pending";
    static final String ERROR_PREFIX = "error:";

    private final RedisTemplate<String, byte[]> template;
    private final byte[] acceptScript;
    private final byte[] decideScript;
    private final byte[] bufferTtlSeconds;
    private final byte[] decisionTtlSeconds;

    public TailSamplingRepository(RedisTemplate<String, byte[]> template,
                                  byte[] acceptScript, byte[] decideScript,
                                  long bufferTtlSeconds, long decisionTtlSeconds) {
        this.template = Objects.requireNonNull(template, "template");
        this.acceptScript = Objects.requireNonNull(acceptScript, "acceptScript");
        this.decideScript = Objects.requireNonNull(decideScript, "decideScript");
        this.bufferTtlSeconds = String.valueOf(bufferTtlSeconds).getBytes(StandardCharsets.UTF_8);
        this.decisionTtlSeconds = String.valueOf(decisionTtlSeconds).getBytes(StandardCharsets.UTF_8);
    }

    private static final byte[] FLAG_TRUE = "1".getBytes(StandardCharsets.UTF_8);
    private static final byte[] FLAG_FALSE = "0".getBytes(StandardCharsets.UTF_8);

    /**
     * Accepts a span into the tail-sampling buffer.
     *
     * @param error when true, marks the whole trace for keep-on-error (any span erroring forces keep)
     * @return "keep" | "drop" | "buffered"
     */
    public String accept(String txid, byte[] bufferedSpanBytes, long firstSeenMillis, boolean error) {
        byte[] bufferKey = key(BUFFER_PREFIX, txid);
        byte[] decisionKey = key(DECISION_PREFIX, txid);
        byte[] pendingKey = PENDING_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] errorKey = key(ERROR_PREFIX, txid);
        byte[] txidBytes = txid.getBytes(StandardCharsets.UTF_8);
        byte[] firstSeen = String.valueOf(firstSeenMillis).getBytes(StandardCharsets.UTF_8);
        byte[] errorFlag = error ? FLAG_TRUE : FLAG_FALSE;

        byte[] result = template.execute((RedisCallback<byte[]>) connection ->
                connection.scriptingCommands().eval(acceptScript, ReturnType.VALUE, 4,
                        bufferKey, decisionKey, pendingKey, errorKey,
                        bufferedSpanBytes, txidBytes, firstSeen, bufferTtlSeconds, errorFlag, decisionTtlSeconds));
        return result == null ? null : new String(result, StandardCharsets.UTF_8);
    }

    /**
     * Records the sampling decision for a transaction and returns buffered spans.
     * The proposed decision may be upgraded to keep inside the script when the trace's
     * error flag is set, so callers must act on the returned list, not on {@code keep}:
     *
     * @return null if another node already decided (skip); a non-empty list of buffered span
     *         bytes when the trace is kept; an empty list when the trace is dropped.
     */
    public List<byte[]> decide(String txid, boolean keep) {
        byte[] bufferKey = key(BUFFER_PREFIX, txid);
        byte[] decisionKey = key(DECISION_PREFIX, txid);
        byte[] pendingKey = PENDING_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] errorKey = key(ERROR_PREFIX, txid);
        byte[] txidBytes = txid.getBytes(StandardCharsets.UTF_8);
        byte[] decisionValue = (keep ? "keep" : "drop").getBytes(StandardCharsets.UTF_8);

        @SuppressWarnings("unchecked")
        List<byte[]> raw = template.execute((RedisCallback<List<byte[]>>) connection ->
                (List<byte[]>) (List<?>) connection.scriptingCommands().eval(decideScript, ReturnType.MULTI, 4,
                        bufferKey, decisionKey, pendingKey, errorKey,
                        decisionValue, txidBytes, decisionTtlSeconds));

        // When Lua returns `false` (another node already decided), Lettuce maps it to a
        // single-element list containing null.  An empty or null list also means "already decided".
        if (raw == null || raw.isEmpty() || raw.get(0) == null) {
            return null;
        }
        List<byte[]> spans = new ArrayList<>(raw.size() - 1);
        for (int i = 1; i < raw.size(); i++) {
            spans.add(raw.get(i));
        }
        return spans;
    }

    /**
     * Returns txids whose firstSeen score is &lt;= thresholdMillis and not yet decided (up to limit).
     */
    public List<String> findStale(long thresholdMillis, int limit) {
        Set<byte[]> members = template.execute((RedisCallback<Set<byte[]>>) connection ->
                connection.zSetCommands().zRangeByScore(
                        PENDING_KEY.getBytes(StandardCharsets.UTF_8),
                        Range.closed(0d, (double) thresholdMillis),
                        Limit.limit().count(limit)));
        List<String> result = new ArrayList<>();
        if (members != null) {
            for (byte[] m : members) {
                result.add(new String(m, StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    private static byte[] key(String prefix, String txid) {
        return (prefix + txid).getBytes(StandardCharsets.UTF_8);
    }
}
