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

import com.navercorp.pinpoint.otlp.trace.linkindex.SlotQuota.Result;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressKey;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlotQuotaTest {

    private static final long SLOT = 29_000_000L;
    private static final String MAPMATCH = SuppressKey.of("mapmatch-bg", "navi:12000");
    private static final String CHECKOUT = SuppressKey.of("p.checkout", "cart:7070");

    private static long at(long slot, int second) {
        return LinkIndexKeys.slotStartMillis(slot) + second * 1000L;
    }

    private static Result client(SlotQuota quota, String identifier, long now) {
        return quota.acquire(LinkKind.CLIENT, identifier, now);
    }

    @Test
    void identifierLimitIsAShareOfTheSlotQuota() {
        assertThat(SlotQuota.limit(5000, 20)).isEqualTo(1000);
        assertThat(SlotQuota.limit(3, 20)).isEqualTo(1);
        assertThat(SlotQuota.limit(1_000_000, 20)).isEqualTo(200_000);
        assertThat(SlotQuota.limit(5000, 100)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void invalidSettingsAreRejected() {
        assertThatThrownBy(() -> new SlotQuota(0, 20, 100)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SlotQuota(10, 0, 100)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SlotQuota(10, 20, 101)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void oneIdentifierCannotTakeTheWholeSlot() {
        SlotQuota quota = new SlotQuota(10, 20, 100);   // 2 per CLIENT identifier
        long now = at(SLOT, 1);

        assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.IDENTIFIER_LIMIT);
        assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.IDENTIFIER_LIMIT);
        // the other identifiers keep their share
        assertThat(client(quota, CHECKOUT, now)).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, CHECKOUT, now)).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, CHECKOUT, now)).isEqualTo(Result.IDENTIFIER_LIMIT);
    }

    @Test
    void entriesRejectedByTheIdentifierLimitUseNoSlotQuota() {
        SlotQuota quota = new SlotQuota(4, 50, 100);    // 2 per CLIENT identifier
        long now = at(SLOT, 1);
        client(quota, MAPMATCH, now);
        client(quota, MAPMATCH, now);
        for (int i = 0; i < 100; i++) {
            assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.IDENTIFIER_LIMIT);
        }

        assertThat(client(quota, CHECKOUT, now)).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, CHECKOUT, now)).isEqualTo(Result.ACCEPTED);
    }

    @Test
    void entriesRejectedByTheSlotQuotaDoNotUseTheIdentifierShare() {
        SlotQuota quota = new SlotQuota(4, 50, 100);    // 2 per CLIENT identifier
        long now = at(SLOT, 1);
        client(quota, SuppressKey.of("a", "x"), now);
        client(quota, SuppressKey.of("b", "x"), now);
        client(quota, SuppressKey.of("c", "x"), now);
        assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.SLOT_LIMIT);
        assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.SLOT_LIMIT);

        // next slot: MAPMATCH starts from zero, and SLOT_LIMIT is never reported as IDENTIFIER_LIMIT
        long next = at(SLOT + 1, 0);
        assertThat(client(quota, MAPMATCH, next)).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, MAPMATCH, next)).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, MAPMATCH, next)).isEqualTo(Result.IDENTIFIER_LIMIT);
    }

    @Test
    void serverEntriesAreLimitedPerApplicationOnlyWhenConfigured() {
        SlotQuota off = new SlotQuota(10, 20, 100);
        SlotQuota on = new SlotQuota(10, 20, 30);       // 3 per SERVER application
        long now = at(SLOT, 1);
        for (int i = 0; i < 10; i++) {
            assertThat(off.acquire(LinkKind.SERVER, "sec.gateway.internal", now)).isEqualTo(Result.ACCEPTED);
        }
        assertThat(off.acquire(LinkKind.SERVER, "sec.gateway.internal", now)).isEqualTo(Result.SLOT_LIMIT);

        for (int i = 0; i < 3; i++) {
            assertThat(on.acquire(LinkKind.SERVER, "sec.gateway.internal", now)).isEqualTo(Result.ACCEPTED);
        }
        assertThat(on.acquire(LinkKind.SERVER, "sec.gateway.internal", now)).isEqualTo(Result.IDENTIFIER_LIMIT);
        assertThat(on.acquire(LinkKind.SERVER, "p.cart", now)).isEqualTo(Result.ACCEPTED);
    }

    @Test
    void hundredPercentKeepsOnlyTheSlotQuota() {
        SlotQuota quota = SlotQuota.of(3);
        long now = at(SLOT, 1);
        assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, MAPMATCH, now)).isEqualTo(Result.SLOT_LIMIT);
    }

    @Test
    void beyondTheTrackedIdentifiersOnlyTheSlotQuotaApplies() {
        int max = SlotQuota.MAX_TRACKED_IDENTIFIERS;
        SlotQuota quota = new SlotQuota(max + 500, 1, 100);     // 105 per CLIENT identifier
        long now = at(SLOT, 1);
        for (int i = 0; i < max; i++) {
            assertThat(client(quota, SuppressKey.of("app", "10.0.0." + i), now)).isEqualTo(Result.ACCEPTED);
        }
        // a new identifier is not tracked: no identifier limit, only the slot quota; counted as untracked
        String untracked = SuppressKey.of("app", "new");
        for (int i = 0; i < 500; i++) {
            assertThat(client(quota, untracked, now)).isEqualTo(Result.ACCEPTED);
        }
        assertThat(client(quota, untracked, now)).isEqualTo(Result.SLOT_LIMIT);
        assertThat(quota.getUntracked()).isEqualTo(501);
    }

    @Test
    void theEndedSlotIsSummarizedInTheGauges() {
        SlotQuota quota = new SlotQuota(4, 50, 100);    // 2 per CLIENT identifier
        assertThat(quota.getSlotLimitReachedSecond()).isEqualTo(SlotQuota.NOT_REACHED_SECOND);

        client(quota, MAPMATCH, at(SLOT, 1));
        client(quota, MAPMATCH, at(SLOT, 1));
        client(quota, MAPMATCH, at(SLOT, 2));            // identifier limit
        client(quota, CHECKOUT, at(SLOT, 3));
        client(quota, SuppressKey.of("c", "x"), at(SLOT, 4));
        client(quota, SuppressKey.of("d", "x"), at(SLOT, 17));   // slot limit at second 17
        client(quota, SuppressKey.of("e", "x"), at(SLOT, 30));
        // the gauges change only when the slot ends
        assertThat(quota.getSlotEntries()).isZero();

        client(quota, CHECKOUT, at(SLOT + 1, 0));

        assertThat(quota.getSlotEntries()).isEqualTo(4);
        assertThat(quota.getSlotLimitReachedSecond()).isEqualTo(17);
        assertThat(quota.getLimitedIdentifiers()).isEqualTo(1);

        client(quota, CHECKOUT, at(SLOT + 2, 0));

        assertThat(quota.getSlotEntries()).isEqualTo(1);
        assertThat(quota.getSlotLimitReachedSecond()).isEqualTo(SlotQuota.NOT_REACHED_SECOND);
        assertThat(quota.getLimitedIdentifiers()).isZero();
    }

    @Test
    void anEntryClockedBeforeTheBoundaryDoesNotReopenTheEndedSlot() {
        SlotQuota quota = new SlotQuota(4, 50, 100);    // 2 per CLIENT identifier
        client(quota, MAPMATCH, at(SLOT, 58));
        client(quota, MAPMATCH, at(SLOT, 59));
        client(quota, CHECKOUT, at(SLOT, 59));
        // first entry of the next slot ends SLOT
        assertThat(client(quota, MAPMATCH, at(SLOT + 1, 0))).isEqualTo(Result.ACCEPTED);
        assertThat(quota.getSlotEntries()).isEqualTo(3);

        // a late entry whose clock was read in SLOT is counted into SLOT + 1 instead of resetting it
        assertThat(client(quota, MAPMATCH, at(SLOT, 59))).isEqualTo(Result.ACCEPTED);
        assertThat(client(quota, MAPMATCH, at(SLOT + 1, 1))).isEqualTo(Result.IDENTIFIER_LIMIT);
        assertThat(quota.getSlotEntries()).isEqualTo(3);

        client(quota, CHECKOUT, at(SLOT + 2, 0));
        assertThat(quota.getSlotEntries()).isEqualTo(2);
    }

    @Test
    void concurrentOffersNeverExceedTheLimits() throws Exception {
        SlotQuota quota = new SlotQuota(1000, 20, 100);   // 200 per CLIENT identifier
        long now = at(SLOT, 1);
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<int[]>> futures = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                futures.add(executor.submit(() -> {
                    int[] counts = new int[Result.values().length];
                    for (int i = 0; i < 1000; i++) {
                        String identifier = i % 2 == 0 ? MAPMATCH : SuppressKey.of("app", "dest" + (i % 50));
                        counts[client(quota, identifier, now).ordinal()]++;
                    }
                    return counts;
                }));
            }
            int[] total = new int[Result.values().length];
            for (Future<int[]> future : futures) {
                int[] counts = future.get(30, TimeUnit.SECONDS);
                for (int i = 0; i < counts.length; i++) {
                    total[i] += counts[i];
                }
            }
            // 4,000 MAPMATCH offers (at most 200 accepted) and 4,000 over 25 identifiers (160 each, under their
            // share): the slot quota is filled exactly, and only MAPMATCH can reach its identifier limit
            assertThat(total[Result.ACCEPTED.ordinal()]).isEqualTo(1000);
            assertThat(total[Result.IDENTIFIER_LIMIT.ordinal()] + total[Result.SLOT_LIMIT.ordinal()]).isEqualTo(7000);
            assertThat(total[Result.IDENTIFIER_LIMIT.ordinal()]).isLessThanOrEqualTo(4000 - 200);
        } finally {
            executor.shutdownNow();
        }
    }
}
