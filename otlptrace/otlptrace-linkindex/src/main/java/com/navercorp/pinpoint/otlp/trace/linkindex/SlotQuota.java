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

import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressKey;
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-collector write quota of a one-minute slot.
 * <ul>
 *     <li>The slot: at most {@code maxEntriesPerSlot} entries (store memory guard).</li>
 *     <li>One identifier: a CLIENT identifier (application + destination, {@link SuppressKey}) writes at most
 *     {@code clientPercent} % of the slot quota and the SERVER entries of one application at most
 *     {@code serverPercent} %, so that one flooding caller cannot crowd out the entries of the others.
 *     100 = no per-identifier limit.</li>
 * </ul>
 * The identifier limit is checked first, so the entries it rejects do not use the slot quota. At most
 * {@value #MAX_TRACKED_IDENTIFIERS} identifiers are tracked per slot; the entries of further identifiers get only the
 * slot quota (fail-open, counted by {@link #getUntracked}).
 * <p>
 * When a slot ends (at the first entry of a later slot), its summary goes to the gauges ({@link #getSlotEntries},
 * {@link #getSlotLimitReachedSecond}, {@link #getLimitedIdentifiers}) and to the log: the identifiers that reached
 * their limit and, when the slot quota was reached, the second it was reached and the largest identifiers. A line is
 * repeated only when the identifiers change or every {@value #LOG_REPEAT_SLOTS} slots.
 */
public class SlotQuota {

    public enum Result {
        ACCEPTED,
        IDENTIFIER_LIMIT,
        SLOT_LIMIT
    }

    static final int MAX_TRACKED_IDENTIFIERS = 10_000;
    static final int LOG_REPEAT_SLOTS = 10;
    static final int NOT_REACHED_SECOND = 60;
    private static final int LOG_TOP_LIMITED = 5;
    private static final int LOG_TOP_OFFERED = 3;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final int maxEntriesPerSlot;
    private final int clientPercent;
    private final int serverPercent;
    private final int clientLimit;
    private final int serverLimit;

    // ingest threads, guarded by lock
    private final Object lock = new Object();
    private long slot = Long.MIN_VALUE;
    private int accepted;
    private int slotDropped;
    private long slotLimitReachedMillis = -1;
    private Map<String, Usage> usage = new HashMap<>();

    private final AtomicLong untracked = new AtomicLong();

    // summary of the last ended slot, written under publishLock
    private volatile int slotEntries;
    private volatile int slotLimitReachedSecond = NOT_REACHED_SECOND;
    private volatile int limitedIdentifiers;

    // summary publishing and log throttling, guarded by publishLock
    private final Object publishLock = new Object();
    private long publishedSlot = Long.MIN_VALUE;
    private Set<String> loggedLimited = Set.of();
    private long loggedLimitedSlot = Long.MIN_VALUE;
    private boolean previousSlotLimitReached;
    private Set<String> loggedLargest = Set.of();
    private long loggedSlotLimitSlot = Long.MIN_VALUE;

    /**
     * @param maxEntriesPerSlot entries per slot and collector
     * @param clientPercent     share of {@code maxEntriesPerSlot} one CLIENT identifier may use, 1..100
     * @param serverPercent     share of {@code maxEntriesPerSlot} the SERVER entries of one application may use, 1..100
     */
    public SlotQuota(int maxEntriesPerSlot, int clientPercent, int serverPercent) {
        if (maxEntriesPerSlot < 1) {
            throw new IllegalArgumentException("maxEntriesPerSlot must be positive");
        }
        if (clientPercent < 1 || clientPercent > 100 || serverPercent < 1 || serverPercent > 100) {
            throw new IllegalArgumentException("clientPercent and serverPercent must be 1..100");
        }
        this.maxEntriesPerSlot = maxEntriesPerSlot;
        this.clientPercent = clientPercent;
        this.serverPercent = serverPercent;
        this.clientLimit = limit(maxEntriesPerSlot, clientPercent);
        this.serverLimit = limit(maxEntriesPerSlot, serverPercent);
    }

    /** only the slot quota */
    public static SlotQuota of(int maxEntriesPerSlot) {
        return new SlotQuota(maxEntriesPerSlot, 100, 100);
    }

    static int limit(int maxEntriesPerSlot, int percent) {
        if (percent == 100) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.max(1, (long) maxEntriesPerSlot * percent / 100);
    }

    /**
     * @param kind       side of the entry
     * @param identifier CLIENT: {@link SuppressKey#of}, SERVER: the application name
     * @param nowMillis  current time, for the slot
     */
    public Result acquire(LinkKind kind, String identifier, long nowMillis) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(identifier, "identifier");
        final long current = LinkIndexKeys.slotOf(nowMillis);
        Ended ended = null;
        final Result result;
        synchronized (lock) {
            // only forward: an entry whose clock was read just before the slot boundary and which takes the lock
            // after the first entry of the next slot is counted into that next slot instead of reopening its own
            if (current > slot) {
                ended = endSlot(current);
            }
            result = acquireLocked(kind, identifier, nowMillis);
        }
        if (ended != null) {
            publish(ended);
        }
        return result;
    }

    private Ended endSlot(long next) {
        final Ended ended = slot == Long.MIN_VALUE ? null : new Ended(slot, accepted, slotDropped, slotLimitReachedMillis, usage);
        slot = next;
        accepted = 0;
        slotDropped = 0;
        slotLimitReachedMillis = -1;
        usage = new HashMap<>();
        return ended;
    }

    private Result acquireLocked(LinkKind kind, String identifier, long nowMillis) {
        Usage counter = usage.get(identifier);
        if (counter == null) {
            if (usage.size() < MAX_TRACKED_IDENTIFIERS) {
                counter = new Usage(kind);
                usage.put(identifier, counter);
            } else {
                untracked.incrementAndGet();
            }
        }
        if (counter != null) {
            counter.offered++;
            if (counter.accepted >= (kind == LinkKind.CLIENT ? clientLimit : serverLimit)) {
                counter.limited++;
                return Result.IDENTIFIER_LIMIT;
            }
        }
        if (accepted >= maxEntriesPerSlot) {
            slotDropped++;
            return Result.SLOT_LIMIT;
        }
        accepted++;
        if (accepted == maxEntriesPerSlot) {
            // the entry that fills the slot quota, whether or not a rejection follows
            slotLimitReachedMillis = nowMillis;
        }
        if (counter != null) {
            counter.accepted++;
        }
        return Result.ACCEPTED;
    }

    /**
     * Publishes the summary of an ended slot to the gauges and the log. Package-private for tests.
     * A summary older than the last published one is ignored: a thread delayed between ending its slot and
     * publishing it must not roll the gauges and the log throttling back behind a newer slot.
     */
    void publish(Ended ended) {
        final List<Map.Entry<String, Usage>> limited = new ArrayList<>();
        for (Map.Entry<String, Usage> entry : ended.usage().entrySet()) {
            if (entry.getValue().limited > 0) {
                limited.add(entry);
            }
        }
        final boolean slotLimitReached = ended.slotLimitReachedMillis() >= 0;
        final int second = slotLimitReached
                ? (int) Math.min(NOT_REACHED_SECOND - 1, Math.max(0, (ended.slotLimitReachedMillis() - LinkIndexKeys.slotStartMillis(ended.slot())) / 1000))
                : NOT_REACHED_SECOND;
        synchronized (publishLock) {
            if (ended.slot() <= publishedSlot) {
                return;
            }
            publishedSlot = ended.slot();
            slotEntries = ended.accepted();
            slotLimitReachedSecond = second;
            limitedIdentifiers = limited.size();
            logLimited(ended.slot(), limited);
            logSlotLimit(ended, slotLimitReached, second);
        }
    }

    private void logLimited(long endedSlot, List<Map.Entry<String, Usage>> limited) {
        final Set<String> keys = new HashSet<>(limited.size());
        for (Map.Entry<String, Usage> entry : limited) {
            keys.add(entry.getKey());
        }
        if (keys.isEmpty()) {
            loggedLimited = Set.of();
            return;
        }
        if (keys.equals(loggedLimited) && endedSlot - loggedLimitedSlot < LOG_REPEAT_SLOTS) {
            return;
        }
        loggedLimited = keys;
        loggedLimitedSlot = endedSlot;
        limited.sort(byOffered());
        logger.info("Link index slot {}: per-identifier limit (client:{} server:{}) reached by {} identifiers: {}",
                endedSlot, display(clientLimit), display(serverLimit), limited.size(), describe(limited, LOG_TOP_LIMITED));
    }

    private void logSlotLimit(Ended ended, boolean reached, int second) {
        final boolean continued = previousSlotLimitReached;
        previousSlotLimitReached = reached;
        if (!reached) {
            loggedLargest = Set.of();
            return;
        }
        final List<Map.Entry<String, Usage>> largest = new ArrayList<>(ended.usage().entrySet());
        largest.sort(byOffered());
        final Set<String> largestKeys = topKeys(largest, LOG_TOP_OFFERED);
        // repeated only when the largest identifiers change (a new flooder) or every LOG_REPEAT_SLOTS slots
        if (continued && largestKeys.equals(loggedLargest) && ended.slot() - loggedSlotLimitSlot < LOG_REPEAT_SLOTS) {
            return;
        }
        loggedLargest = largestKeys;
        loggedSlotLimitSlot = ended.slot();
        logger.info("Link index slot {}: slot limit {} reached at second {}, dropped {}, largest identifiers: {}",
                ended.slot(), maxEntriesPerSlot, second, ended.slotDropped(), describe(largest, LOG_TOP_OFFERED));
    }

    private static Set<String> topKeys(List<Map.Entry<String, Usage>> sorted, int top) {
        final Set<String> keys = new HashSet<>();
        final int size = Math.min(top, sorted.size());
        for (int i = 0; i < size; i++) {
            keys.add(sorted.get(i).getKey());
        }
        return keys;
    }

    /** the slot whose slot-limit line was logged last, {@link Long#MIN_VALUE} when none; for tests */
    long getLoggedSlotLimitSlot() {
        synchronized (publishLock) {
            return loggedSlotLimitSlot;
        }
    }

    private static Comparator<Map.Entry<String, Usage>> byOffered() {
        return Comparator.comparingInt((Map.Entry<String, Usage> entry) -> entry.getValue().offered).reversed();
    }

    private static String describe(List<Map.Entry<String, Usage>> entries, int top) {
        final StringBuilder builder = new StringBuilder();
        final int size = Math.min(top, entries.size());
        for (int i = 0; i < size; i++) {
            final Map.Entry<String, Usage> entry = entries.get(i);
            final Usage counter = entry.getValue();
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(counter.kind == LinkKind.CLIENT ? SuppressKey.display(entry.getKey()) : entry.getKey() + " (SERVER)")
                    .append(" offered=").append(counter.offered)
                    .append(" written=").append(counter.accepted);
        }
        if (entries.size() > top) {
            builder.append(", ...");
        }
        return builder.toString();
    }

    private static String display(int limit) {
        return limit == Integer.MAX_VALUE ? "off" : Integer.toString(limit);
    }

    /** entries accepted in the last ended slot */
    public int getSlotEntries() {
        return slotEntries;
    }

    /** second (0..59) the slot quota was reached in the last ended slot, {@value #NOT_REACHED_SECOND} when it was not */
    public int getSlotLimitReachedSecond() {
        return slotLimitReachedSecond;
    }

    /** identifiers that reached their limit in the last ended slot */
    public int getLimitedIdentifiers() {
        return limitedIdentifiers;
    }

    /** entries one CLIENT identifier may write per slot, {@link Integer#MAX_VALUE} when the limit is off */
    public int getClientLimit() {
        return clientLimit;
    }

    /** entries that got only the slot quota because {@value #MAX_TRACKED_IDENTIFIERS} identifiers were tracked */
    public long getUntracked() {
        return untracked.get();
    }

    @Override
    public String toString() {
        return "SlotQuota{maxEntriesPerSlot=" + maxEntriesPerSlot + ", clientPercent=" + clientPercent
                + ", serverPercent=" + serverPercent + '}';
    }

    static final class Usage {
        private final LinkKind kind;
        private int offered;
        private int accepted;
        private int limited;

        private Usage(LinkKind kind) {
            this.kind = kind;
        }
    }

    /** the counters of a slot that ended; package-private for tests */
    record Ended(long slot, int accepted, int slotDropped, long slotLimitReachedMillis, Map<String, Usage> usage) {
    }
}
