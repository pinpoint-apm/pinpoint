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

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LinkIndexKeysTest {

    @Test
    void slotIsTheMinute() {
        long minute = 1_791_958_740_000L;
        assertThat(LinkIndexKeys.slotOf(minute)).isEqualTo(minute / 60_000);
        assertThat(LinkIndexKeys.slotOf(minute + 59_999)).isEqualTo(minute / 60_000);
        assertThat(LinkIndexKeys.slotOf(minute + 60_000)).isEqualTo(minute / 60_000 + 1);

        long slot = LinkIndexKeys.slotOf(minute);
        assertThat(LinkIndexKeys.slotStartMillis(slot)).isEqualTo(minute);
        assertThat(LinkIndexKeys.slotEndMillis(slot)).isEqualTo(minute + 60_000);
    }

    @Test
    void expirySpreadsOverAMinutePerBucket() {
        long slot = 29_865_979;
        long base = (LinkIndexKeys.slotEndMillis(slot) + 300_000) / 1000;
        Set<Long> seen = new HashSet<>();
        for (int bucket = 0; bucket < 128; bucket++) {
            long expireAt = LinkIndexKeys.expireAtSeconds(slot, bucket, 300_000);
            assertThat(expireAt).isBetween(base, base + 59);
            assertThat(LinkIndexKeys.expireAtSeconds(slot, bucket, 300_000)).isEqualTo(expireAt);
            seen.add(expireAt);
        }
        assertThat(seen).hasSize(60);
    }

    @Test
    void keys() {
        assertThat(new String(LinkIndexKeys.entryKey(29_865_979, 17), StandardCharsets.US_ASCII))
                .isEqualTo("pinpoint:otlp:link:29865979:17");
    }

    @Test
    void bucketIsStableAndInRange() {
        Random random = new Random(1);
        Set<Integer> seen = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            byte[] traceId = new byte[16];
            random.nextBytes(traceId);
            int bucket = LinkIndexKeys.bucketOf(traceId, 64);
            assertThat(bucket).isBetween(0, 63);
            assertThat(LinkIndexKeys.bucketOf(traceId.clone(), 64)).isEqualTo(bucket);
            seen.add(bucket);
        }
        assertThat(seen).hasSize(64);
    }

    @Test
    void samplingIsIndependentOfTheBucket() {
        // with the same hash and a modulo that divides the bucket count, sampled traces would
        // all fall into buckets 0, 4, 8, ...
        Random random = new Random(2);
        Set<Integer> sampledBuckets = new HashSet<>();
        int sampled = 0;
        for (int i = 0; i < 20_000; i++) {
            byte[] traceId = new byte[16];
            random.nextBytes(traceId);
            if (LinkIndexKeys.isSampled(traceId, 4)) {
                sampled++;
                sampledBuckets.add(LinkIndexKeys.bucketOf(traceId, 64));
            }
        }
        assertThat(sampled).isBetween(4_500, 5_500);
        assertThat(sampledBuckets).hasSize(64);
    }

    @Test
    void noSamplingByDefault() {
        assertThat(LinkIndexKeys.isSampled(new byte[16], 1)).isTrue();
        assertThat(LinkIndexKeys.isSampled(new byte[16], 0)).isTrue();
    }
}
