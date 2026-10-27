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

import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexKeys;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkKind;
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
 * Collector side of the suppression list: decides whether an entry is written.
 * <ul>
 *     <li>The ACTIVE identifiers are refreshed from the store every {@code refresh}; a failed refresh keeps the
 *     previous snapshot, and nothing is suppressed until the first snapshot arrives (fail-open).</li>
 *     <li>{@code force} rules suppress at once without the store; {@code pin} rules are never suppressed.</li>
 *     <li>A suppressed identifier still writes the first {@code probePerSlot} entries of every slot (per collector),
 *     so the join job sees a match as soon as the callee starts reporting and releases it.</li>
 *     <li>CLIENT identifiers ({@link SuppressKey#of}) and SERVER identifiers ({@link SuppressKey#ofServer}) are
 *     handled alike; {@code serverEnabled=false} ignores the SERVER identifiers of the list and of the force rules.</li>
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
    private final int probeModulo;
    private final boolean serverEnabled;
    private final SuppressKey.Rules forced;
    private final SuppressKey.Rules pinned;

    private volatile Set<String> active = Set.of();
    private volatile int activeServer;
    private volatile long probeSlot = Long.MIN_VALUE;
    private final Object probeLock = new Object();
    private final ConcurrentHashMap<String, AtomicInteger> probeCount = new ConcurrentHashMap<>();

    private final AtomicLong suppressedClient = new AtomicLong();
    private final AtomicLong suppressedServer = new AtomicLong();
    private final AtomicLong probedClient = new AtomicLong();
    private final AtomicLong probedServer = new AtomicLong();
    private final AtomicLong refreshes = new AtomicLong();
    private final AtomicLong refreshErrors = new AtomicLong();

    private ScheduledExecutorService scheduler;

    /**
     * SERVER identifiers enabled, no trace-sampled probes.
     *
     * @param store        the suppression list, {@code null} to apply only the static rules
     * @param refresh      snapshot interval
     * @param probePerSlot entries of a suppressed identifier still written per slot by this collector
     */
    public LinkIndexSuppression(SuppressStore store, Duration refresh, int probePerSlot,
                                SuppressKey.Rules forced, SuppressKey.Rules pinned) {
        this(store, refresh, probePerSlot, 0, true, forced, pinned);
    }

    /**
     * No trace-sampled probes.
     *
     * @param serverEnabled {@code false} to ignore the SERVER identifiers of the list and of the force rules
     */
    public LinkIndexSuppression(SuppressStore store, Duration refresh, int probePerSlot, boolean serverEnabled,
                                SuppressKey.Rules forced, SuppressKey.Rules pinned) {
        this(store, refresh, probePerSlot, 0, serverEnabled, forced, pinned);
    }

    /**
     * @param store         the suppression list, {@code null} to apply only the static rules
     * @param refresh       snapshot interval
     * @param probePerSlot  entries of a suppressed identifier still written per slot by this collector: the first
     *                      ones of the slot
     * @param probeModulo   in addition, every entry whose trace id is selected by {@link LinkIndexKeys#isProbe}
     *                      (1 in this many, by a hash independent of the writer's trace sampling) is written whatever
     *                      its identifier. The decision depends on the trace id alone, so the CLIENT and the SERVER
     *                      side of one call take it alike on any collector: when both identifiers of a call relation
     *                      are suppressed, these probes are the pairs that can still match and release them, where
     *                      the first-of-slot probes of the two sides may never be the same calls. {@code 0} or
     *                      {@code 1} turns them off.
     * @param serverEnabled {@code false} to ignore the SERVER identifiers of the list and of the force rules
     */
    public LinkIndexSuppression(SuppressStore store, Duration refresh, int probePerSlot, int probeModulo, boolean serverEnabled,
                                SuppressKey.Rules forced, SuppressKey.Rules pinned) {
        this.store = store;
        this.refresh = Objects.requireNonNull(refresh, "refresh");
        if (probePerSlot < 1 || refresh.isNegative() || refresh.isZero()) {
            throw new IllegalArgumentException("probePerSlot and refresh must be positive");
        }
        if (probeModulo < 0) {
            throw new IllegalArgumentException("probeModulo must not be negative");
        }
        this.probePerSlot = probePerSlot;
        this.probeModulo = probeModulo;
        this.serverEnabled = serverEnabled;
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
            final Set<String> read = store.readActive();
            final Set<String> next = new HashSet<>(read.size());
            int servers = 0;
            for (String key : read) {
                if (SuppressKey.isServer(key)) {
                    if (!serverEnabled) {
                        continue;
                    }
                    servers++;
                }
                next.add(key);
            }
            final Set<String> previous = active;
            if (!next.equals(previous)) {
                logChange(previous, next);
            }
            active = Set.copyOf(next);
            activeServer = servers;
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
        return allowIdentifier(applicationName, SuppressKey.of(applicationName, destinationId), slot);
    }

    /**
     * @param parentClass {@link SuppressKey#serverClass} of the entry
     * @param slot        the current slot, for the probe allowance
     * @return {@code true} if the SERVER entry is to be written
     */
    public boolean allowServer(String applicationName, String parentClass, long slot) {
        if (!serverEnabled) {
            return true;
        }
        return allowIdentifier(applicationName, SuppressKey.ofServer(applicationName, parentClass), slot);
    }

    /**
     * {@link #allow} / {@link #allowServer} for a caller that already built the identifier; no trace-sampled probe
     *
     * @param key  {@link SuppressKey#of} or {@link SuppressKey#ofServer} of the entry
     * @param slot the current slot, for the probe allowance
     * @return {@code true} if the entry is to be written
     */
    public boolean allowIdentifier(String applicationName, String key, long slot) {
        return allowIdentifier(applicationName, key, slot, null);
    }

    /**
     * @param key     {@link SuppressKey#of} or {@link SuppressKey#ofServer} of the entry
     * @param slot    the current slot, for the first-of-slot probe allowance
     * @param traceId the 16-byte trace id of the entry for the trace-sampled probes, {@code null} for none
     * @return {@code true} if the entry is to be written
     */
    public boolean allowIdentifier(String applicationName, String key, long slot, byte[] traceId) {
        if (active.isEmpty() && forced.isEmpty()) {
            return true;
        }
        final boolean server = SuppressKey.isServer(key);
        if (server && !serverEnabled) {
            return true;
        }
        if (pinned.matches(applicationName, key)) {
            return true;
        }
        if (!active.contains(key) && !forced.matches(applicationName, key)) {
            return true;
        }
        if (traceId != null && LinkIndexKeys.isProbe(traceId, probeModulo)) {
            // the same decision on both sides of the call: these probes still pair up when both identifiers are
            // suppressed and the first-of-slot probes of the two sides are different calls
            (server ? probedServer : probedClient).incrementAndGet();
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
            (server ? probedServer : probedClient).incrementAndGet();
            return true;
        }
        (server ? suppressedServer : suppressedClient).incrementAndGet();
        return false;
    }

    /** ACTIVE identifiers of the last snapshot, both kinds (static rules not included) */
    public int getActiveCount() {
        return active.size();
    }

    /** ACTIVE SERVER identifiers of the last snapshot */
    public int getActiveServerCount() {
        return activeServer;
    }

    Set<String> getActive() {
        return active;
    }

    /** entries of a suppressed identifier still written per slot by this collector */
    public int getProbePerSlot() {
        return probePerSlot;
    }

    /** trace id modulo of the probes written whatever the identifier, {@code 0} or {@code 1} when off */
    public int getProbeModulo() {
        return probeModulo;
    }

    public boolean isServerEnabled() {
        return serverEnabled;
    }

    /** entries not written, both kinds */
    public long getSuppressed() {
        return suppressedClient.get() + suppressedServer.get();
    }

    /** entries of {@code kind} not written */
    public long getSuppressed(LinkKind kind) {
        return kind == LinkKind.SERVER ? suppressedServer.get() : suppressedClient.get();
    }

    /**
     * entries of suppressed identifiers let through as probes, both kinds. Counted when the suppression lets the
     * entry through: the quota and the queue may still drop it ({@code dropped} by reason and kind), so this is not
     * the number of probes stored.
     */
    public long getProbed() {
        return probedClient.get() + probedServer.get();
    }

    /** entries of {@code kind} of suppressed identifiers let through as probes (before the quota and the queue) */
    public long getProbed(LinkKind kind) {
        return kind == LinkKind.SERVER ? probedServer.get() : probedClient.get();
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
                + ", probeModulo=" + probeModulo + ", serverEnabled=" + serverEnabled + ", forced=" + forced + ", pinned=" + pinned + '}';
    }
}
