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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/**
 * Maintains the suppression list from the per-identifier outcome of each joined slot ({@link LinkCounts}):
 * <ul>
 *     <li>an identifier with a matched entry is removed at once, whatever its state (released)</li>
 *     <li>an identifier with at least {@link SuppressPolicy#minUnmatchedPerSlot()} unmatched entries is tracked as a
 *     CANDIDATE and becomes ACTIVE once {@link SuppressPolicy#activateAfterSlots()} have passed since its first unmatched
 *     slot and {@link SuppressPolicy#activateMinSlots()} slots met the minimum</li>
 *     <li>identifiers unseen for {@link SuppressPolicy#idleSlots()} are forgotten, and the list is capped at
 *     {@link SuppressPolicy#maxKeys()} (longest unseen first)</li>
 * </ul>
 * The collectors keep writing a probe of every ACTIVE identifier, so a callee that starts reporting is seen here
 * and released. {@link #apply} never throws: a failed run is logged and counted, and the next slot recomputes
 * the same transitions from the store.
 */
public class SuppressManager {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final SuppressStore store;
    private final SuppressPolicy policy;

    // totals per kind: index LinkKind.ordinal()
    private final LongAdder[] activated = adders();
    private final LongAdder[] released = adders();
    private final LongAdder[] expired = adders();
    private final LongAdder errors = new LongAdder();
    private volatile long lastCandidates;
    private volatile long lastActive;
    private volatile long lastCandidatesServer;
    private volatile long lastActiveServer;

    private static LongAdder[] adders() {
        final LongAdder[] adders = new LongAdder[LinkKind.values().length];
        for (int i = 0; i < adders.length; i++) {
            adders[i] = new LongAdder();
        }
        return adders;
    }

    public SuppressManager(SuppressStore store, SuppressPolicy policy) {
        this.store = Objects.requireNonNull(store, "store");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public SuppressPolicy getPolicy() {
        return policy;
    }

    /**
     * SERVER identifiers included.
     *
     * @param slot   the joined slot k
     * @param counts outcome of slot k per identifier ({@link SuppressKey}, CLIENT and SERVER)
     */
    public SuppressSummary apply(long slot, Map<String, LinkCounts> counts) {
        return apply(slot, counts, true);
    }

    /**
     * @param slot          the joined slot k
     * @param counts        outcome of slot k per identifier ({@link SuppressKey}, CLIENT and SERVER)
     * @param serverEnabled {@code false} to keep SERVER identifiers out of the list: their counts are ignored and the
     *                      SERVER identifiers already in the list are removed, so that collectors still applying them
     *                      stop at their next refresh instead of at the idle expiry
     */
    public SuppressSummary apply(long slot, Map<String, LinkCounts> counts, boolean serverEnabled) {
        Objects.requireNonNull(counts, "counts");
        try {
            return doApply(slot, counts, serverEnabled);
        } catch (RuntimeException e) {
            errors.increment();
            logger.warn("Suppression list update failed for slot {}: {}", slot, e.toString());
            return SuppressSummary.failed(slot);
        }
    }

    private SuppressSummary doApply(long slot, Map<String, LinkCounts> counts, boolean serverEnabled) {
        final Map<String, SuppressState> current = store.readAll();
        final Transition transition = transition(slot, current, counts, policy, serverEnabled);
        if (!transition.upserts().isEmpty() || !transition.deletes().isEmpty()) {
            store.write(transition.upserts(), transition.deletes());
        }
        for (String key : transition.activated()) {
            logger.info("Suppression activated {} after {} unmatched slots since slot {}",
                    SuppressKey.display(key), transition.result().get(key).unmatchedSlots(), transition.result().get(key).firstUnmatchedSlot());
        }
        for (String key : transition.released()) {
            logger.info("Suppression released {} (matched in slot {})", SuppressKey.display(key), slot);
        }
        if (!transition.removed().isEmpty()) {
            logger.info("Suppression removed {} SERVER identifiers: SERVER suppression is off", transition.removed().size());
        }
        if (!transition.expired().isEmpty()) {
            logger.info("Suppression forgot {} idle identifiers", transition.expired().size());
        }
        int candidates = 0;
        int active = 0;
        int candidatesServer = 0;
        int activeServer = 0;
        for (Map.Entry<String, SuppressState> entry : transition.result().entrySet()) {
            final boolean server = SuppressKey.isServer(entry.getKey());
            if (entry.getValue().isActive()) {
                active++;
                activeServer += server ? 1 : 0;
            } else {
                candidates++;
                candidatesServer += server ? 1 : 0;
            }
        }
        add(activated, transition.activated());
        add(released, transition.released());
        add(expired, transition.expired());
        add(expired, transition.removed());
        lastCandidates = candidates;
        lastActive = active;
        lastCandidatesServer = candidatesServer;
        lastActiveServer = activeServer;
        final SuppressSummary summary = new SuppressSummary(slot, candidates, active,
                transition.activated().size(), transition.released().size(), transition.expired().size() + transition.removed().size(), false);
        logger.info("Suppressed {}", summary);
        return summary;
    }

    /**
     * Pure transition of the list for slot {@code slot}, SERVER identifiers included: no store access.
     */
    static Transition transition(long slot, Map<String, SuppressState> current, Map<String, LinkCounts> counts, SuppressPolicy policy) {
        return transition(slot, current, counts, policy, true);
    }

    /**
     * Pure transition of the list for slot {@code slot}: no store access.
     *
     * @param serverEnabled {@code false} removes the SERVER identifiers of the list and ignores the SERVER counts
     */
    static Transition transition(long slot, Map<String, SuppressState> current, Map<String, LinkCounts> counts, SuppressPolicy policy, boolean serverEnabled) {
        final Map<String, SuppressState> result = new HashMap<>(current);
        final Map<String, SuppressState> upserts = new HashMap<>();
        final Set<String> deletes = new HashSet<>();
        final List<String> activated = new ArrayList<>();
        final List<String> released = new ArrayList<>();
        final List<String> expired = new ArrayList<>();
        final List<String> removed = new ArrayList<>();

        if (!serverEnabled) {
            for (String key : current.keySet()) {
                if (SuppressKey.isServer(key)) {
                    deletes.add(key);
                    removed.add(key);
                    result.remove(key);
                }
            }
            counts = clientsOnly(counts);
        }

        for (Map.Entry<String, LinkCounts> entry : counts.entrySet()) {
            final String key = entry.getKey();
            final LinkCounts count = entry.getValue();
            SuppressState state = current.get(key);
            if (state != null && slot - state.lastSeenSlot() > policy.idleSlots()) {
                // back after the idle period: the old run of unmatched slots does not count any more
                deletes.add(key);
                expired.add(key);
                result.remove(key);
                state = null;
            }
            if (count.matched() > 0) {
                if (state != null) {
                    deletes.add(key);
                    released.add(key);
                    result.remove(key);
                }
                continue;
            }
            if (count.unmatched() >= policy.minUnmatchedPerSlot()) {
                SuppressState next = state == null
                        ? SuppressState.candidate(slot, count.unmatched())
                        : state.seen(slot, count.unmatched(), true);
                if (!next.isActive()
                        && slot - next.firstUnmatchedSlot() >= policy.activateAfterSlots()
                        && next.unmatchedSlots() >= policy.activateMinSlots()) {
                    next = next.activated(slot);
                    activated.add(key);
                }
                upserts.put(key, next);
                result.put(key, next);
                deletes.remove(key);   // re-created after the idle reset: the upsert replaces the stale field
            } else if (state != null && count.unmatched() > 0) {
                final SuppressState next = state.seen(slot, count.unmatched(), false);
                upserts.put(key, next);
                result.put(key, next);
            }
        }

        for (Map.Entry<String, SuppressState> entry : current.entrySet()) {
            final String key = entry.getKey();
            if (counts.containsKey(key) || !result.containsKey(key)) {
                continue;
            }
            if (slot - entry.getValue().lastSeenSlot() > policy.idleSlots()) {
                deletes.add(key);
                expired.add(key);
                result.remove(key);
            }
        }

        if (result.size() > policy.maxKeys()) {
            final List<Map.Entry<String, SuppressState>> oldestFirst = new ArrayList<>(result.entrySet());
            oldestFirst.sort(Comparator.comparingLong((Map.Entry<String, SuppressState> e) -> e.getValue().lastSeenSlot())
                    .thenComparing(Map.Entry::getKey));
            final int excess = result.size() - policy.maxKeys();
            for (int i = 0; i < excess; i++) {
                final String key = oldestFirst.get(i).getKey();
                upserts.remove(key);
                activated.remove(key);
                result.remove(key);
                if (current.containsKey(key)) {
                    deletes.add(key);
                }
                expired.add(key);
            }
        }
        return new Transition(upserts, deletes, activated, released, expired, removed, result);
    }

    static Map<String, LinkCounts> clientsOnly(Map<String, LinkCounts> counts) {
        final Map<String, LinkCounts> clients = new HashMap<>(counts.size());
        for (Map.Entry<String, LinkCounts> entry : counts.entrySet()) {
            if (!SuppressKey.isServer(entry.getKey())) {
                clients.put(entry.getKey(), entry.getValue());
            }
        }
        return clients;
    }

    private static void add(LongAdder[] totals, List<String> keys) {
        for (String key : keys) {
            totals[SuppressKey.kind(key).ordinal()].increment();
        }
    }

    private static long sum(LongAdder[] totals) {
        long sum = 0;
        for (LongAdder total : totals) {
            sum += total.sum();
        }
        return sum;
    }

    /** ACTIVE transitions, summed over every run, both kinds */
    public long getActivated() {
        return sum(activated);
    }

    /** ACTIVE transitions of {@code kind}, summed over every run */
    public long getActivated(LinkKind kind) {
        return activated[kind.ordinal()].sum();
    }

    /** identifiers removed because they matched, summed over every run, both kinds */
    public long getReleased() {
        return sum(released);
    }

    public long getReleased(LinkKind kind) {
        return released[kind.ordinal()].sum();
    }

    /** identifiers forgotten (idle or cap), summed over every run, both kinds */
    public long getExpired() {
        return sum(expired);
    }

    public long getExpired(LinkKind kind) {
        return expired[kind.ordinal()].sum();
    }

    /** failed runs, summed */
    public long getErrors() {
        return errors.sum();
    }

    /** CANDIDATE identifiers after the last run, both kinds */
    public long getLastCandidates() {
        return lastCandidates;
    }

    /** ACTIVE identifiers after the last run, both kinds */
    public long getLastActive() {
        return lastActive;
    }

    /** CANDIDATE SERVER identifiers after the last run */
    public long getLastCandidatesServer() {
        return lastCandidatesServer;
    }

    /** ACTIVE SERVER identifiers after the last run */
    public long getLastActiveServer() {
        return lastActiveServer;
    }

    /**
     * @param upserts fields to write
     * @param deletes fields to remove (disjoint from {@code upserts})
     * @param expired identifiers forgotten because they were idle or over the cap
     * @param removed SERVER identifiers removed because SERVER suppression is off
     * @param result  the list after the transition
     */
    record Transition(Map<String, SuppressState> upserts, Set<String> deletes,
                      List<String> activated, List<String> released, List<String> expired, List<String> removed,
                      Map<String, SuppressState> result) {
    }

    public record SuppressSummary(long slot, int candidates, int active, int activated, int released, int expired, boolean failed) {
        static SuppressSummary failed(long slot) {
            return new SuppressSummary(slot, 0, 0, 0, 0, 0, true);
        }
    }
}
