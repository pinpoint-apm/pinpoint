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

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collector side of the suppression list: decides whether a CLIENT entry is written.
 * <ul>
 *     <li>The ACTIVE identifiers are refreshed from the store every {@code refresh}; a failed refresh keeps the
 *     previous snapshot, and nothing is suppressed until the first snapshot arrives (fail-open).</li>
 *     <li>{@code force} rules suppress at once without the store; {@code pin} rules are never suppressed.</li>
 *     <li>A suppressed identifier still writes the first {@code probePerSlot} entries of every slot (per collector),
 *     so the join job sees a match as soon as the callee starts reporting and releases it.</li>
 * </ul>
 */
public class LinkIndexSuppression implements AutoCloseable {

    /** suppresses nothing */
    public static final LinkIndexSuppression NONE = new LinkIndexSuppression(null, Duration.ofSeconds(60), 1,
            SuppressKey.Rules.EMPTY, SuppressKey.Rules.EMPTY);

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final SuppressStore store;
    private final Duration refresh;
    private final int probePerSlot;
    private final SuppressKey.Rules forced;
    private final SuppressKey.Rules pinned;

    private volatile Set<String> active = Set.of();
    private volatile long probeSlot = Long.MIN_VALUE;
    private final Object probeLock = new Object();
    private final ConcurrentHashMap<String, AtomicInteger> probeCount = new ConcurrentHashMap<>();

    private final AtomicLong suppressed = new AtomicLong();
    private final AtomicLong probed = new AtomicLong();
    private final AtomicLong refreshes = new AtomicLong();
    private final AtomicLong refreshErrors = new AtomicLong();

    private ScheduledExecutorService scheduler;

    /**
     * @param store        the suppression list, {@code null} to apply only the static rules
     * @param refresh      snapshot interval
     * @param probePerSlot entries of a suppressed identifier still written per slot by this collector
     */
    public LinkIndexSuppression(SuppressStore store, Duration refresh, int probePerSlot,
                                SuppressKey.Rules forced, SuppressKey.Rules pinned) {
        this.store = store;
        this.refresh = Objects.requireNonNull(refresh, "refresh");
        if (probePerSlot < 1 || refresh.isNegative() || refresh.isZero()) {
            throw new IllegalArgumentException("probePerSlot and refresh must be positive");
        }
        this.probePerSlot = probePerSlot;
        this.forced = Objects.requireNonNull(forced, "forced");
        this.pinned = Objects.requireNonNull(pinned, "pinned");
    }

    public synchronized void start() {
        if (store == null || scheduler != null) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r, "otlp-link-suppress-refresh");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::refresh, 0, refresh.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Replaces the ACTIVE snapshot from the store. Public for tests and for a manual refresh.
     */
    public void refresh() {
        if (store == null) {
            return;
        }
        try {
            final Set<String> next = Set.copyOf(store.readActive());
            final Set<String> previous = active;
            if (!next.equals(previous)) {
                logChange(previous, next);
            }
            active = next;
            refreshes.incrementAndGet();
        } catch (RuntimeException e) {
            final long count = refreshErrors.incrementAndGet();
            if (count == 1 || count % 100 == 0) {
                logger.warn("Suppression list refresh failed (keeping {} identifiers). total:{} cause:{}", active.size(), count, e.toString());
            }
        }
    }

    private void logChange(Set<String> previous, Set<String> next) {
        final Set<String> added = new HashSet<>(next);
        added.removeAll(previous);
        final Set<String> removed = new HashSet<>(previous);
        removed.removeAll(next);
        logger.info("Suppression list changed: active {} -> {}, added {}, removed {}",
                previous.size(), next.size(), display(added), display(removed));
    }

    private static Set<String> display(Set<String> keys) {
        final Set<String> display = new HashSet<>(keys.size());
        for (String key : keys) {
            display.add(SuppressKey.display(key));
        }
        return display;
    }

    /**
     * @param slot the current slot, for the probe allowance
     * @return {@code true} if the CLIENT entry is to be written
     */
    public boolean allow(String applicationName, String destinationId, long slot) {
        if (active.isEmpty() && forced.isEmpty()) {
            return true;
        }
        final String key = SuppressKey.of(applicationName, destinationId);
        if (pinned.matches(applicationName, key)) {
            return true;
        }
        if (!active.contains(key) && !forced.matches(applicationName, key)) {
            return true;
        }
        if (slot != probeSlot) {
            synchronized (probeLock) {
                if (slot != probeSlot) {
                    probeCount.clear();
                    probeSlot = slot;
                }
            }
        }
        final int count = probeCount.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
        if (count <= probePerSlot) {
            probed.incrementAndGet();
            return true;
        }
        suppressed.incrementAndGet();
        return false;
    }

    /** ACTIVE identifiers of the last snapshot (static rules not included) */
    public int getActiveCount() {
        return active.size();
    }

    Set<String> getActive() {
        return active;
    }

    /** CLIENT entries not written */
    public long getSuppressed() {
        return suppressed.get();
    }

    /** CLIENT entries of suppressed identifiers written as probes */
    public long getProbed() {
        return probed.get();
    }

    public long getRefreshes() {
        return refreshes.get();
    }

    public long getRefreshErrors() {
        return refreshErrors.get();
    }

    @Override
    public synchronized void close() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    @Override
    public String toString() {
        return "LinkIndexSuppression{store=" + (store != null) + ", refresh=" + refresh + ", probePerSlot=" + probePerSlot
                + ", forced=" + forced + ", pinned=" + pinned + '}';
    }
}
