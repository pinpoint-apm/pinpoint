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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Joins one bucket of slot {@code k} with the same bucket of slot {@code k-1}.
 * <p>
 * The entries of a slot are kept until the job of the next slot has read them, so nothing is copied
 * between slots. The job of slot k counts only the calls with at least one side in k; a call whose
 * two sides are both in k-1 was counted by the job of k-1:
 * <ul>
 *     <li>SERVER in k, CLIENT in k or in k-1 → {@link LinkSource#SAME_SLOT}, slot k</li>
 *     <li>SERVER in k-1, CLIENT in k → {@link LinkSource#CROSS_SLOT}, slot k-1</li>
 *     <li>SERVER and CLIENT both in k-1 → not counted (already counted by the job of k-1)</li>
 * </ul>
 * The link is always recorded in the SERVER entry's slot, so each call is counted exactly once, by
 * the job of the later of its two slots, when its sides are at most one slot apart.
 * <p>
 * The keys matched inside k-1 are excluded before joining: an entry of k with such a key is a
 * duplicate (a re-sent span) of a call already counted, and is neither counted nor reported.
 * <p>
 * Known limitation: a call matched across slots (one side in k-2, the other in k-1, counted by the job of
 * k-1) is counted again when the k-2 side is re-sent in k, because k-2 is gone and nothing records the match
 * ({@code LinkJoinerTest.resentSideOfACrossSlotCallIsCountedAgain}). This takes a retransmission at least
 * one slot late, as an OTel collector queue replaying after an outage does; the native span path counts a
 * re-sent span again as well.
 */
public final class LinkJoiner {

    private LinkJoiner() {
    }

    /**
     * @param slot     slot k
     * @param current  entries of slot k
     * @param previous entries of slot k-1 (all of them, matched by the previous job or not)
     */
    public static JoinResult join(long slot, Collection<LinkEntry> current, Collection<LinkEntry> previous) {
        final Map<PairKey, LinkEntry> currentServer = new LinkedHashMap<>();
        final Map<PairKey, LinkEntry> currentClient = new LinkedHashMap<>();
        split(current, currentServer, currentClient);

        final Map<PairKey, LinkEntry> previousServer = new LinkedHashMap<>();
        final Map<PairKey, LinkEntry> previousClient = new LinkedHashMap<>();
        split(previous, previousServer, previousClient);

        // calls with both sides in k-1: counted by the job of k-1
        final Set<PairKey> countedBefore = new HashSet<>();
        for (PairKey key : previousServer.keySet()) {
            if (previousClient.containsKey(key)) {
                countedBefore.add(key);
            }
        }
        for (PairKey key : countedBefore) {
            previousServer.remove(key);
            previousClient.remove(key);
            currentServer.remove(key);
            currentClient.remove(key);
        }

        final List<MatchedPair> pairs = new ArrayList<>();
        final List<LinkEntry> unmatched = new ArrayList<>();

        for (Map.Entry<PairKey, LinkEntry> entry : currentServer.entrySet()) {
            final LinkEntry server = entry.getValue();
            LinkEntry client = currentClient.remove(entry.getKey());
            if (client == null) {
                client = previousClient.remove(entry.getKey());
            }
            if (client != null) {
                pairs.add(new MatchedPair(server, client, slot, LinkSource.SAME_SLOT));
            } else {
                unmatched.add(server);
            }
        }

        for (Map.Entry<PairKey, LinkEntry> entry : currentClient.entrySet()) {
            final LinkEntry client = entry.getValue();
            final LinkEntry server = previousServer.remove(entry.getKey());
            if (server != null) {
                pairs.add(new MatchedPair(server, client, slot - 1, LinkSource.CROSS_SLOT));
            } else {
                unmatched.add(client);
            }
        }

        return new JoinResult(pairs, unmatched, new ArrayList<>(previousServer.values()), new ArrayList<>(previousClient.values()));
    }

    private static void split(Collection<LinkEntry> entries, Map<PairKey, LinkEntry> server, Map<PairKey, LinkEntry> client) {
        if (entries == null) {
            return;
        }
        for (LinkEntry entry : entries) {
            final Map<PairKey, LinkEntry> target = entry.kind() == LinkKind.SERVER ? server : client;
            target.putIfAbsent(entry.pairKey(), entry);
        }
    }
}
