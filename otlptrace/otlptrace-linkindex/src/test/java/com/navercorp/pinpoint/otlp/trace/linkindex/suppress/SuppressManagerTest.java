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

import com.navercorp.pinpoint.otlp.trace.linkindex.LinkKind;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SuppressManagerTest {

    // small thresholds: ACTIVE after 3 slots since the first unmatched slot and 3 counted slots, minimum 100 per slot
    private static final SuppressPolicy POLICY = new SuppressPolicy(3, 3, 100, 10, 4);
    private static final String MAPMATCH = SuppressKey.of("mapmatch-bg", "navi:12000");
    private static final String GATEWAY = SuppressKey.of("sec.gateway.internal", "coin-api-dev");

    private static Map<String, LinkCounts> counts(Object... keyAndCounts) {
        Map<String, LinkCounts> counts = new HashMap<>();
        for (int i = 0; i < keyAndCounts.length; i += 3) {
            counts.put((String) keyAndCounts[i], new LinkCounts((Integer) keyAndCounts[i + 1], (Integer) keyAndCounts[i + 2]));
        }
        return counts;
    }

    @Test
    void candidateBecomesActiveAfterBothConditions() {
        Map<String, SuppressState> list = Map.of();
        // slot 10: first unmatched slot
        SuppressManager.Transition t = SuppressManager.transition(10, list, counts(MAPMATCH, 0, 7_000), POLICY);
        assertThat(t.upserts()).containsOnlyKeys(MAPMATCH);
        assertThat(t.result().get(MAPMATCH).state()).isEqualTo(SuppressState.State.CANDIDATE);
        assertThat(t.activated()).isEmpty();

        // slots 11, 12: counted, but only 2 slots have passed
        t = SuppressManager.transition(11, t.result(), counts(MAPMATCH, 0, 7_000), POLICY);
        t = SuppressManager.transition(12, t.result(), counts(MAPMATCH, 0, 7_000), POLICY);
        assertThat(t.activated()).isEmpty();
        assertThat(t.result().get(MAPMATCH).unmatchedSlots()).isEqualTo(3);

        // slot 13: 3 slots passed and 4 counted
        t = SuppressManager.transition(13, t.result(), counts(MAPMATCH, 0, 7_000), POLICY);
        assertThat(t.activated()).containsExactly(MAPMATCH);
        SuppressState active = t.result().get(MAPMATCH);
        assertThat(active.isActive()).isTrue();
        assertThat(active.activatedSlot()).isEqualTo(13);
        assertThat(active.firstUnmatchedSlot()).isEqualTo(10);
        assertThat(t.upserts().get(MAPMATCH)).isEqualTo(active);
    }

    @Test
    void slotsWithoutEnoughUnmatchedDoNotCount() {
        SuppressManager.Transition t = SuppressManager.transition(10, Map.of(), counts(MAPMATCH, 0, 7_000), POLICY);
        // slots 11..13: entries below the minimum keep the identifier but do not count toward activation
        t = SuppressManager.transition(11, t.result(), counts(MAPMATCH, 0, 5), POLICY);
        t = SuppressManager.transition(12, t.result(), counts(MAPMATCH, 0, 5), POLICY);
        t = SuppressManager.transition(13, t.result(), counts(MAPMATCH, 0, 5), POLICY);

        assertThat(t.activated()).isEmpty();
        SuppressState state = t.result().get(MAPMATCH);
        assertThat(state.state()).isEqualTo(SuppressState.State.CANDIDATE);
        assertThat(state.unmatchedSlots()).isEqualTo(1);
        assertThat(state.lastSeenSlot()).isEqualTo(13);
        assertThat(state.unmatched()).isEqualTo(7_015);
    }

    @Test
    void gapOfTheJobDoesNotActivateEarly() {
        // slot 10 counted once, then the job was down until slot 18 (within the idle period): enough slots have
        // passed since the first unmatched slot, but only 2 slots were counted
        SuppressManager.Transition t = SuppressManager.transition(10, Map.of(), counts(MAPMATCH, 0, 7_000), POLICY);
        t = SuppressManager.transition(18, t.result(), counts(MAPMATCH, 0, 7_000), POLICY);

        assertThat(t.activated()).isEmpty();
        assertThat(t.expired()).isEmpty();
        assertThat(t.result().get(MAPMATCH).unmatchedSlots()).isEqualTo(2);
        assertThat(t.result().get(MAPMATCH).firstUnmatchedSlot()).isEqualTo(10);
    }

    @Test
    void identifierBackAfterTheIdlePeriodStartsOver() {
        // a candidate one counted slot short of activation, then unseen for longer than idleSlots (10)
        SuppressState almost = SuppressState.candidate(10, 500).seen(11, 500, true);   // slots=2, first=10
        Map<String, SuppressState> list = Map.of(MAPMATCH, almost);

        SuppressManager.Transition t = SuppressManager.transition(30, list, counts(MAPMATCH, 0, 7_000), POLICY);

        assertThat(t.activated()).isEmpty();
        assertThat(t.expired()).containsExactly(MAPMATCH);
        assertThat(t.deletes()).isEmpty();                       // the upsert replaces the stale field
        SuppressState fresh = t.result().get(MAPMATCH);
        assertThat(fresh.state()).isEqualTo(SuppressState.State.CANDIDATE);
        assertThat(fresh.firstUnmatchedSlot()).isEqualTo(30);
        assertThat(fresh.unmatchedSlots()).isEqualTo(1);
        assertThat(fresh.unmatched()).isEqualTo(7_000);
        assertThat(t.upserts().get(MAPMATCH)).isEqualTo(fresh);
    }

    @Test
    void activeBackAfterTheIdlePeriodWithAMatchIsSimplyRemoved() {
        SuppressState active = SuppressState.candidate(1, 7_000).seen(2, 7_000, true).seen(3, 7_000, true).activated(4);

        SuppressManager.Transition t = SuppressManager.transition(40, Map.of(MAPMATCH, active), counts(MAPMATCH, 2, 7_000), POLICY);

        assertThat(t.result()).isEmpty();
        assertThat(t.deletes()).containsExactly(MAPMATCH);
        assertThat(t.upserts()).isEmpty();
    }

    @Test
    void smallIdentifiersAreNotTracked() {
        SuppressManager.Transition t = SuppressManager.transition(10, Map.of(), counts(MAPMATCH, 0, 99), POLICY);

        assertThat(t.upserts()).isEmpty();
        assertThat(t.result()).isEmpty();
    }

    @Test
    void oneMatchReleasesCandidateAndActive() {
        Map<String, SuppressState> list = new HashMap<>();
        list.put(MAPMATCH, SuppressState.candidate(1, 7_000).seen(2, 7_000, true).seen(3, 7_000, true).activated(4));
        list.put(GATEWAY, SuppressState.candidate(3, 1_000));

        SuppressManager.Transition t = SuppressManager.transition(5, list, counts(MAPMATCH, 1, 7_000, GATEWAY, 3, 0), POLICY);

        assertThat(t.released()).containsExactlyInAnyOrder(MAPMATCH, GATEWAY);
        assertThat(t.deletes()).containsExactlyInAnyOrder(MAPMATCH, GATEWAY);
        assertThat(t.upserts()).isEmpty();
        assertThat(t.result()).isEmpty();
    }

    @Test
    void activeStaysActiveOnProbesOnly() {
        SuppressState active = SuppressState.candidate(1, 7_000).seen(2, 7_000, true).seen(3, 7_000, true).activated(4);

        SuppressManager.Transition t = SuppressManager.transition(5, Map.of(MAPMATCH, active), counts(MAPMATCH, 0, 300), POLICY);

        assertThat(t.activated()).isEmpty();
        assertThat(t.released()).isEmpty();
        SuppressState next = t.result().get(MAPMATCH);
        assertThat(next.isActive()).isTrue();
        assertThat(next.activatedSlot()).isEqualTo(4);
        assertThat(next.unmatchedSlots()).isEqualTo(4);
        assertThat(next.lastSeenSlot()).isEqualTo(5);
    }

    @Test
    void idleIdentifiersAreForgotten() {
        Map<String, SuppressState> list = Map.of(
                MAPMATCH, SuppressState.candidate(1, 7_000),          // last seen slot 1
                GATEWAY, SuppressState.candidate(5, 1_000));          // last seen slot 5

        SuppressManager.Transition t = SuppressManager.transition(12, list, Map.of(), POLICY);   // idle after 10 slots

        assertThat(t.expired()).containsExactly(MAPMATCH);
        assertThat(t.deletes()).containsExactly(MAPMATCH);
        assertThat(t.result()).containsOnlyKeys(GATEWAY);
    }

    @Test
    void listIsCappedLongestUnseenFirst() {
        Map<String, SuppressState> list = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            list.put(SuppressKey.of("app" + i, "dest"), SuppressState.candidate(10 + i, 500));   // last seen 10..13
        }
        // slot 14 adds a fifth identifier: the cap of 4 drops the longest unseen (app0, slot 10)
        SuppressManager.Transition t = SuppressManager.transition(14, list, counts(MAPMATCH, 0, 7_000), POLICY);

        assertThat(t.result()).hasSize(4).containsKey(MAPMATCH).doesNotContainKey(SuppressKey.of("app0", "dest"));
        assertThat(t.expired()).containsExactly(SuppressKey.of("app0", "dest"));
        assertThat(t.deletes()).containsExactly(SuppressKey.of("app0", "dest"));
        assertThat(t.upserts()).containsOnlyKeys(MAPMATCH);
    }

    @Test
    void capNeverDeletesAndUpsertsTheSameKey() {
        Map<String, SuppressState> list = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            list.put(SuppressKey.of("app" + i, "dest"), SuppressState.candidate(20, 500));   // all last seen 20
        }
        // two new identifiers at slot 20 as well: ties fall back to the key order, and whichever goes is not upserted
        SuppressManager.Transition t = SuppressManager.transition(20, list,
                counts(MAPMATCH, 0, 7_000, GATEWAY, 0, 7_000), POLICY);

        assertThat(t.result()).hasSize(4);
        assertThat(t.expired()).hasSize(2);
        for (String key : t.deletes()) {
            assertThat(t.upserts()).doesNotContainKey(key);
        }
        for (String key : t.expired()) {
            assertThat(t.upserts()).doesNotContainKey(key);
            assertThat(t.result()).doesNotContainKey(key);
        }
    }

    @Test
    void applyWritesTheTransitionAndKeepsTotals() {
        SuppressStore store = mock(SuppressStore.class);
        SuppressState active = SuppressState.candidate(1, 7_000).seen(2, 7_000, true).seen(3, 7_000, true).activated(4);
        when(store.readAll()).thenReturn(Map.of(MAPMATCH, active, GATEWAY, SuppressState.candidate(4, 1_000)));
        SuppressManager manager = new SuppressManager(store, POLICY);

        SuppressManager.SuppressSummary summary = manager.apply(5, counts(MAPMATCH, 2, 100, GATEWAY, 0, 1_000));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, SuppressState>> upserts = ArgumentCaptor.forClass(Map.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> deletes = ArgumentCaptor.forClass(Collection.class);
        verify(store).write(upserts.capture(), deletes.capture());
        assertThat(upserts.getValue()).containsOnlyKeys(GATEWAY);
        assertThat(deletes.getValue()).containsExactly(MAPMATCH);
        assertThat(summary.failed()).isFalse();
        assertThat(summary.released()).isEqualTo(1);
        assertThat(summary.candidates()).isEqualTo(1);
        assertThat(summary.active()).isZero();
        assertThat(manager.getReleased()).isEqualTo(1);
        assertThat(manager.getLastCandidates()).isEqualTo(1);
        assertThat(manager.getLastActive()).isZero();
    }

    @Test
    void serverIdentifiersFollowTheSameRulesAndAreCountedApart() {
        SuppressStore store = mock(SuppressStore.class);
        String gatewayRoot = SuppressKey.ofServer("sec.gateway.internal", SuppressKey.PARENT_ROOT);
        String gatewayOther = SuppressKey.ofServer("sec.gateway.internal", SuppressKey.PARENT_OTHER);
        SuppressState readyRoot = SuppressState.candidate(1, 10_000).seen(2, 10_000, true).seen(3, 10_000, true);
        SuppressState readyOther = SuppressState.candidate(1, 500).seen(2, 500, true).seen(3, 500, true);
        when(store.readAll()).thenReturn(Map.of(gatewayRoot, readyRoot, gatewayOther, readyOther, MAPMATCH, readyRoot));
        SuppressManager manager = new SuppressManager(store, POLICY);

        // slot 4: the root class never matched -> ACTIVE; the other class matched once -> released; the CLIENT one -> ACTIVE
        SuppressManager.SuppressSummary summary = manager.apply(4, counts(gatewayRoot, 0, 10_000, gatewayOther, 1, 400, MAPMATCH, 0, 7_000));

        assertThat(summary.activated()).isEqualTo(2);
        assertThat(summary.released()).isEqualTo(1);
        assertThat(manager.getActivated()).isEqualTo(2);
        assertThat(manager.getActivated(LinkKind.SERVER)).isEqualTo(1);
        assertThat(manager.getActivated(LinkKind.CLIENT)).isEqualTo(1);
        assertThat(manager.getReleased(LinkKind.SERVER)).isEqualTo(1);
        assertThat(manager.getReleased(LinkKind.CLIENT)).isZero();
        assertThat(manager.getExpired(LinkKind.SERVER)).isZero();
        assertThat(manager.getLastActive()).isEqualTo(2);
        assertThat(manager.getLastActiveServer()).isEqualTo(1);
        assertThat(manager.getLastCandidates()).isZero();
        assertThat(manager.getLastCandidatesServer()).isZero();
    }

    @Test
    void applyWithoutChangesDoesNotWrite() {
        SuppressStore store = mock(SuppressStore.class);
        when(store.readAll()).thenReturn(Map.of());
        SuppressManager manager = new SuppressManager(store, POLICY);

        manager.apply(5, counts(MAPMATCH, 0, 10));

        verify(store, never()).write(anyMap(), any());
    }

    @Test
    void applyNeverThrows() {
        SuppressStore store = mock(SuppressStore.class);
        when(store.readAll()).thenThrow(new IllegalStateException("store down"));
        SuppressManager manager = new SuppressManager(store, POLICY);

        SuppressManager.SuppressSummary summary = manager.apply(5, counts(MAPMATCH, 0, 7_000));

        assertThat(summary.failed()).isTrue();
        assertThat(manager.getErrors()).isEqualTo(1);
    }
}
