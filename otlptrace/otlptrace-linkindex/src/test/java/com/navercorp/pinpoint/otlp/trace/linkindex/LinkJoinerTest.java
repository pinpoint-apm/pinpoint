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

import java.util.List;

import static com.navercorp.pinpoint.otlp.trace.linkindex.LinkEntryCodecTest.traceId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class LinkJoinerTest {

    private static final long SLOT = 1000;

    private static LinkEntry server(int trace, long spanId) {
        return LinkEntry.server(traceId(trace), spanId, 0, "b", 1900, 10, false);
    }

    private static LinkEntry client(int trace, long spanId) {
        return LinkEntry.client(traceId(trace), spanId, 0, "a", 1900, "b:80");
    }

    @Test
    void sameSlot() {
        LinkEntry s = server(1, 11);
        LinkEntry c = client(1, 11);

        JoinResult result = LinkJoiner.join(SLOT, List.of(s, c), List.of());

        assertThat(result.pairs()).containsExactly(new MatchedPair(s, c, SLOT, LinkSource.SAME_SLOT));
        assertThat(result.unmatched()).isEmpty();
    }

    @Test
    void serverInSlotClientInPreviousSlot() {
        LinkEntry s = server(1, 11);
        LinkEntry c = client(1, 11);

        JoinResult result = LinkJoiner.join(SLOT, List.of(s), List.of(c));

        // recorded in the SERVER entry's slot
        assertThat(result.pairs()).containsExactly(new MatchedPair(s, c, SLOT, LinkSource.SAME_SLOT));
        assertThat(result.droppedClient()).isZero();
    }

    @Test
    void serverInPreviousSlotClientInSlot() {
        LinkEntry s = server(1, 11);
        LinkEntry c = client(1, 11);

        JoinResult result = LinkJoiner.join(SLOT, List.of(c), List.of(s));

        assertThat(result.pairs()).containsExactly(new MatchedPair(s, c, SLOT - 1, LinkSource.CROSS_SLOT));
        assertThat(result.droppedServer()).isZero();
    }

    @Test
    void callWithBothSidesInThePreviousSlotIsNotCountedAgain() {
        // counted by the job of the previous slot
        JoinResult result = LinkJoiner.join(SLOT, List.of(), List.of(server(1, 11), client(1, 11)));

        assertThat(result.pairs()).isEmpty();
        assertThat(result.droppedServer()).isZero();
        assertThat(result.droppedClient()).isZero();
    }

    @Test
    void resentSideOfACallCountedInThePreviousSlotIsIgnored() {
        // the CLIENT span was re-sent and arrived again in k: the call was already counted by k-1
        JoinResult result = LinkJoiner.join(SLOT, List.of(client(1, 11), server(2, 22)), List.of(server(1, 11), client(1, 11)));

        assertThat(result.pairs()).isEmpty();
        assertThat(result.unmatched().get(0).spanId()).isEqualTo(22L);
        assertThat(result.unmatched()).hasSize(1);
    }

    /**
     * Known limitation (see the class comment of {@link LinkJoiner}): the side of a cross-slot call re-sent
     * one slot after the call was counted matches its partner again. Both directions.
     */
    @Test
    void resentSideOfACrossSlotCallIsCountedAgain() {
        // CLIENT in k-2, SERVER in k-1: counted by the job of k-1 (SAME_SLOT, slot k-1)
        JoinResult jobOfPrevious = LinkJoiner.join(SLOT - 1, List.of(server(1, 11)), List.of(client(1, 11)));
        assertThat(jobOfPrevious.pairs()).extracting(MatchedPair::source).containsExactly(LinkSource.SAME_SLOT);
        // the CLIENT re-sent in k: k-2 is deleted, the SERVER of k-1 is matched again (CROSS_SLOT, slot k-1)
        JoinResult jobOfCurrent = LinkJoiner.join(SLOT, List.of(client(1, 11)), List.of(server(1, 11)));
        assertThat(jobOfCurrent.pairs()).extracting(MatchedPair::source, MatchedPair::slot)
                .containsExactly(tuple(LinkSource.CROSS_SLOT, SLOT - 1));

        // SERVER in k-2, CLIENT in k-1: counted by the job of k-1 (CROSS_SLOT, slot k-2)
        jobOfPrevious = LinkJoiner.join(SLOT - 1, List.of(client(2, 22)), List.of(server(2, 22)));
        assertThat(jobOfPrevious.pairs()).extracting(MatchedPair::source).containsExactly(LinkSource.CROSS_SLOT);
        // the SERVER re-sent in k: matched again with the CLIENT of k-1 (SAME_SLOT, slot k)
        jobOfCurrent = LinkJoiner.join(SLOT, List.of(server(2, 22)), List.of(client(2, 22)));
        assertThat(jobOfCurrent.pairs()).extracting(MatchedPair::source, MatchedPair::slot)
                .containsExactly(tuple(LinkSource.SAME_SLOT, SLOT));
    }

    @Test
    void unmatchedEntriesOfTheSlotAreReported() {
        LinkEntry s = server(1, 11);
        LinkEntry c = client(2, 22);

        JoinResult result = LinkJoiner.join(SLOT, List.of(s, c), List.of());

        assertThat(result.pairs()).isEmpty();
        assertThat(result.unmatched()).containsExactly(s, c);
    }

    @Test
    void entriesOfThePreviousSlotWithoutPartnerAreDropped() {
        JoinResult result = LinkJoiner.join(SLOT, List.of(), List.of(server(1, 11), client(2, 22), client(3, 33)));

        assertThat(result.pairs()).isEmpty();
        assertThat(result.unmatched()).isEmpty();
        assertThat(result.droppedServer()).isEqualTo(1);
        assertThat(result.droppedClient()).isEqualTo(2);
    }

    @Test
    void sameSpanIdInAnotherTraceDoesNotMatch() {
        JoinResult result = LinkJoiner.join(SLOT, List.of(server(1, 11), client(2, 11)), List.of());

        assertThat(result.pairs()).isEmpty();
        assertThat(result.unmatched()).hasSize(2);
    }

    @Test
    void consecutiveJobsCountEachCallOnce() {
        // slot k-1: call 1 complete, call 2 SERVER only, call 3 CLIENT only
        // slot k  : call 2 CLIENT, call 3 SERVER, call 4 complete, call 5 SERVER only
        List<LinkEntry> previous = List.of(server(1, 1), client(1, 1), server(2, 2), client(3, 3));
        List<LinkEntry> current = List.of(client(2, 2), server(3, 3), server(4, 4), client(4, 4), server(5, 5));

        JoinResult jobOfPrevious = LinkJoiner.join(SLOT - 1, previous, List.of());
        JoinResult jobOfCurrent = LinkJoiner.join(SLOT, current, previous);

        assertThat(jobOfPrevious.pairs()).extracting(p -> p.server().spanId()).containsExactly(1L);
        assertThat(jobOfCurrent.pairs()).extracting(p -> p.server().spanId(), MatchedPair::source)
                .containsExactlyInAnyOrder(
                        tuple(3L, LinkSource.SAME_SLOT),
                        tuple(4L, LinkSource.SAME_SLOT),
                        tuple(2L, LinkSource.CROSS_SLOT));
        assertThat(jobOfCurrent.unmatched()).extracting(LinkEntry::spanId).containsExactly(5L);
        assertThat(jobOfCurrent.droppedServer()).isZero();
        assertThat(jobOfCurrent.droppedClient()).isZero();
    }
}
