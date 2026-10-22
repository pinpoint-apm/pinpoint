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

    private final LongAdder activated = new LongAdder();
    private final LongAdder released = new LongAdder();
    private final LongAdder expired = new LongAdder();
    private final LongAdder errors = new LongAdder();
    private volatile long lastCandidates;
    private volatile long lastActive;

    public SuppressManager(SuppressStore store, SuppressPolicy policy) {
        this.store = Objects.requireNonNull(store, "store");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public SuppressPolicy getPolicy() {
        return policy;
    }

    /**
     * @param slot   the joined slot k
     * @param counts outcome of slot k per CLIENT identifier
     */
    public SuppressSummary apply(long slot, Map<String, LinkCounts> counts) {
        Objects.requireNonNull(counts, "counts");
        try {
            return doApply(slot, counts);
        } catch (RuntimeException e) {
            errors.increment();
            logger.warn("Suppression list update failed for slot {}: {}", slot, e.toString());
            return SuppressSummary.failed(slot);
        }
    }

    private SuppressSummary doApply(long slot, Map<String, LinkCounts> counts) {
        final Map<String, SuppressState> current = store.readAll();
        final Transition transition = transition(slot, current, counts, policy);
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
        if (!transition.expired().isEmpty()) {
            logger.info("Suppression forgot {} idle identifiers", transition.expired().size());
        }
        int candidates = 0;
        int active = 0;
        for (SuppressState state : transition.result().values()) {
            if (state.isActive()) {
                active++;
            } else {
                candidates++;
            }
        }
        activated.add(transition.activated().size());
        released.add(transition.released().size());
        expired.add(transition.expired().size());
        lastCandidates = candidates;
        lastActive = active;
        final SuppressSummary summary = new SuppressSummary(slot, candidates, active,
                transition.activated().size(), transition.released().size(), transition.expired().size(), false);
        logger.info("Suppressed {}", summary);
        return summary;
    }

    /**
     * Pure transition of the list for slot {@code slot}: no store access.
     */
    static Transition transition(long slot, Map<String, SuppressState> current, Map<String, LinkCounts> counts, SuppressPolicy policy) {
        final Map<String, SuppressState> result = new HashMap<>(current);
        final Map<String, SuppressState> upserts = new HashMap<>();
        final Set<String> deletes = new HashSet<>();
        final List<String> activated = new ArrayList<>();
        final List<String> released = new ArrayList<>();
        final List<String> expired = new ArrayList<>();

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
        return new Transition(upserts, deletes, activated, released, expired, result);
    }

    /** ACTIVE transitions, summed over every run */
    public long getActivated() {
        return activated.sum();
    }

    /** identifiers removed because they matched, summed over every run */
    public long getReleased() {
        return released.sum();
    }

    /** identifiers forgotten (idle or cap), summed over every run */
    public long getExpired() {
        return expired.sum();
    }

    /** failed runs, summed */
    public long getErrors() {
        return errors.sum();
    }

    /** CANDIDATE identifiers after the last run */
    public long getLastCandidates() {
        return lastCandidates;
    }

    /** ACTIVE identifiers after the last run */
    public long getLastActive() {
        return lastActive;
    }

    /**
     * @param upserts fields to write
     * @param deletes fields to remove (disjoint from {@code upserts})
     * @param result  the list after the transition
     */
    record Transition(Map<String, SuppressState> upserts, Set<String> deletes,
                      List<String> activated, List<String> released, List<String> expired,
                      Map<String, SuppressState> result) {
    }

    public record SuppressSummary(long slot, int candidates, int active, int activated, int released, int expired, boolean failed) {
        static SuppressSummary failed(long slot) {
            return new SuppressSummary(slot, 0, 0, 0, 0, 0, true);
        }
    }
}
