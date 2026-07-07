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

package com.navercorp.pinpoint.collector.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class CaffeineDedupCacheTest {

    @Test
    void update_firstOccurrenceOnly() {
        try (CaffeineDedupCache<String> cache = new CaffeineDedupCache<>(Duration.ofMinutes(1), 100)) {
            assertThat(cache.update("a")).isTrue();
            assertThat(cache.update("a")).isFalse();
            assertThat(cache.update("b")).isTrue();

            assertThat(cache.estimatedSize()).isEqualTo(2);
        }
    }

    @Test
    void update_recordsHitMissStats() {
        try (CaffeineDedupCache<String> cache = new CaffeineDedupCache<>(Duration.ofMinutes(1), 100, true)) {
            cache.update("a");   // first occurrence -> actual write
            cache.update("a");   // hit  -> deduplicated (saved) write
            cache.update("a");   // hit

            CacheStats stats = cache.stats();
            // hit/miss are recorded only by the fast-path getIfPresent; the view's putIfAbsent
            // records nothing, so missCount matches the actual writes
            assertThat(stats.missCount()).isEqualTo(1);
            assertThat(stats.hitCount()).isEqualTo(2);
        }
    }

    @Test
    void update_statsDisabledByDefault() {
        try (CaffeineDedupCache<String> cache = new CaffeineDedupCache<>(Duration.ofMinutes(1), 100)) {
            cache.update("a");
            cache.update("a");

            CacheStats stats = cache.stats();
            assertThat(stats.missCount()).isZero();
            assertThat(stats.hitCount()).isZero();
        }
    }

    @Test
    void update_expiredKey_isAcceptedAgain() {
        // duplicate rejection is covered by update_firstOccurrenceOnly; asserting isFalse here
        // would be flaky with such a short TTL
        try (CaffeineDedupCache<String> cache = new CaffeineDedupCache<>(Duration.ofMillis(10), 100)) {
            assertThat(cache.update("a")).isTrue();

            Awaitility.await()
                    .atMost(5, TimeUnit.SECONDS)
                    .pollDelay(Duration.ofMillis(10))
                    .pollInterval(Duration.ofMillis(10))
                    .untilAsserted(() -> assertThat(cache.update("a")).isTrue());
        }
    }

    /**
     * Documents why {@link CaffeineDedupCache#update} uses computeIfAbsent instead of putIfAbsent:
     * the Map view's putIfAbsent bypasses the stats counter even with recordStats enabled.
     */
    @Test
    void caffeine_viewPutIfAbsent_doesNotRecordHitMissStats() {
        Cache<String, Boolean> cache = Caffeine.newBuilder()
                .recordStats()
                .build();
        ConcurrentMap<String, Boolean> view = cache.asMap();

        assertThat(view.putIfAbsent("a", Boolean.TRUE)).isNull();      // first occurrence
        assertThat(view.putIfAbsent("a", Boolean.TRUE)).isNotNull();   // duplicate

        CacheStats stats = cache.stats();
        assertThat(stats.missCount()).isZero();
        assertThat(stats.hitCount()).isZero();
    }

    /**
     * Counterpart: the Map view's computeIfAbsent does record hit/miss stats
     * ({@code LocalCache.computeIfAbsent} defaults to {@code recordStats=true}).
     */
    @Test
    void caffeine_viewComputeIfAbsent_recordsHitMissStats() {
        Cache<String, Boolean> cache = Caffeine.newBuilder()
                .recordStats()
                .build();
        ConcurrentMap<String, Boolean> view = cache.asMap();

        view.computeIfAbsent("a", k -> Boolean.TRUE);   // miss + load
        view.computeIfAbsent("a", k -> Boolean.TRUE);   // hit

        CacheStats stats = cache.stats();
        assertThat(stats.missCount()).isEqualTo(1);
        assertThat(stats.hitCount()).isEqualTo(1);
    }
}
