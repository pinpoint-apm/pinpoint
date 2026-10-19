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

import com.navercorp.pinpoint.common.util.BytesUtils;

import java.nio.charset.StandardCharsets;

/**
 * Key layout of the link index.
 * <ul>
 *     <li>entry key {@code pinpoint:otlp:link:{slot}:{bucket}}: written by the collectors, read by the
 *     join jobs of {@code slot} and {@code slot + 1}, deleted by the cleanup of {@code slot + 1}</li>
 * </ul>
 * A slot is one minute, aligned with the server-map time slot. Entries of one trace always land in
 * the same bucket so a bucket can be joined on its own.
 */
public final class LinkIndexKeys {

    public static final long SLOT_MILLIS = 60_000L;

    static final String ENTRY_PREFIX = "pinpoint:otlp:link:";

    private LinkIndexKeys() {
    }

    public static long slotOf(long epochMillis) {
        return Math.floorDiv(epochMillis, SLOT_MILLIS);
    }

    public static long slotStartMillis(long slot) {
        return slot * SLOT_MILLIS;
    }

    public static long slotEndMillis(long slot) {
        return (slot + 1) * SLOT_MILLIS;
    }

    /** expiry spread: keys of one slot expire over this many seconds instead of all at once */
    public static final int EXPIRE_SPREAD_SECONDS = 60;

    /**
     * Absolute expiry (epoch seconds) of the keys of {@code slot} in {@code bucket}:
     * {@code slot end + ttl + (bucket mod 60)s}. Spread per bucket so that one slot's keys do not all
     * expire in the same second (the nbase-arc guide warns that mass expiry at a fixed time delays
     * memory reclaim and responses), and still a function of (slot, bucket) only, so every collector
     * that sends it sets the same value.
     */
    public static long expireAtSeconds(long slot, int bucket, long ttlMillis) {
        return (slotEndMillis(slot) + ttlMillis) / 1000 + Math.floorMod(bucket, EXPIRE_SPREAD_SECONDS);
    }

    public static byte[] entryKey(long slot, int bucket) {
        return key(ENTRY_PREFIX, slot, bucket);
    }

    private static byte[] key(String prefix, long slot, int bucket) {
        return (prefix + slot + ':' + bucket).getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Bucket of a trace. Uses FNV-1a over all 16 bytes followed by a murmur3 finalizer.
     */
    public static int bucketOf(byte[] traceId, int buckets) {
        int hash = 0x811c9dc5;
        for (byte b : traceId) {
            hash ^= (b & 0xff);
            hash *= 0x01000193;
        }
        return Math.floorMod(fmix32(hash), buckets);
    }

    /**
     * Trace sampling for the {@code sample-modulo} safety valve. Deliberately uses a different hash
     * from {@link #bucketOf}: with the same hash and a modulo that divides the bucket count, the
     * sampled traces would all fall into 1/modulo of the buckets.
     */
    public static boolean isSampled(byte[] traceId, int modulo) {
        if (modulo <= 1) {
            return true;
        }
        final long low = BytesUtils.bytesToLong(traceId, 8);
        return Math.floorMod(fmix64(low), (long) modulo) == 0;
    }

    static int fmix32(int h) {
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        return h;
    }

    static long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }
}
