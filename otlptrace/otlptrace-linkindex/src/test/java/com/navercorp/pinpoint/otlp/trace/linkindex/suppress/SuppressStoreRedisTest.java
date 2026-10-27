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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkEntry;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexKeys;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexStore;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexWriter;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkJoinService;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkMapWriter;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LettuceLinkIndexRedisConnector;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import com.navercorp.pinpoint.otlp.trace.linkindex.SlotQuota;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Suppression list against a real Redis: store round trip, the per-identifier counts of a join, and the
 * collector side reading what the manager wrote.
 */
@Testcontainers(disabledWithoutDocker = true)
class SuppressStoreRedisTest {

    private static final int BUCKETS = 16;
    private static final int OTEL_SERVER = ServiceType.OPENTELEMETRY_SERVER.getCode();
    private static final String MAPMATCH = SuppressKey.of("mapmatch-bg", "navi:12000");
    private static final String CHECKOUT = SuppressKey.of("p.checkout", "cart:7070");

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:5.0"))
            .withExposedPorts(6379);

    private LettuceLinkIndexRedisConnector connector;
    private LinkIndexConnection admin;
    private final AtomicLong now = new AtomicLong();
    private final long slot = LinkIndexKeys.slotOf(System.currentTimeMillis());

    @BeforeEach
    void setUp() throws Exception {
        connector = new LettuceLinkIndexRedisConnector(LettuceLinkIndexRedisConnector.Mode.STANDALONE,
                List.of(REDIS.getHost() + ":" + REDIS.getFirstMappedPort()), null,
                Duration.ofSeconds(5), Duration.ofSeconds(5));
        admin = connector.connect();
        admin.async().flushall().get(5, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() {
        admin.close();
        connector.close();
    }

    private SuppressStore newStore() {
        return new SuppressStore(connector.connect(), Duration.ofSeconds(5));
    }

    @Test
    void storeRoundTrip() {
        SuppressStore store = newStore();
        assertThat(store.readAll()).isEmpty();
        assertThat(store.readActive()).isEmpty();

        SuppressState candidate = SuppressState.candidate(slot, 7_000);
        SuppressState active = candidate.seen(slot + 1, 7_000, true).activated(slot + 2);
        store.write(Map.of(MAPMATCH, active, CHECKOUT, candidate), List.of());

        assertThat(store.readAll()).containsOnly(Map.entry(MAPMATCH, active), Map.entry(CHECKOUT, candidate));
        assertThat(store.readActive()).containsExactly(MAPMATCH);

        store.write(Map.of(CHECKOUT, candidate.seen(slot + 3, 50, false)), List.of(MAPMATCH));

        assertThat(store.readAll()).containsOnlyKeys(CHECKOUT);
        assertThat(store.readAll().get(CHECKOUT).lastSeenSlot()).isEqualTo(slot + 3);
        assertThat(store.readActive()).isEmpty();
        assertThat(store.getUndecodable()).isZero();
    }

    @Test
    void undecodableFieldIsSkipped() throws Exception {
        admin.async().hset(SuppressStore.KEY, SuppressStore.field("junk"), new byte[]{9, 9}).get(5, TimeUnit.SECONDS);
        SuppressStore store = newStore();
        store.write(Map.of(MAPMATCH, SuppressState.candidate(slot, 7_000)), List.of());

        assertThat(store.readAll()).containsOnlyKeys(MAPMATCH);
        assertThat(store.getUndecodable()).isEqualTo(1);
    }

    @Test
    void joinCountsMatchedAndFinallyUnmatchedEntriesPerIdentifier() {
        LinkIndexWriter writer = new LinkIndexWriter(connector.connect(), BUCKETS, 300_000, 10_000, 1_000_000, 1,
                Duration.ofSeconds(5), now::get);
        writer.start();
        try {
            // slot k-1: mapmatch client 1 (never matched), checkout client 2 (matched in k), cart server 5 (never matched)
            writeAt(writer, slot - 1, List.of(client(1, "mapmatch-bg", "navi:12000"), client(2, "p.checkout", "cart:7070"), server(5)));
            // slot k: checkout client 3 (matched in k), its servers 2 and 3, mapmatch client 4 (still waiting for k+1)
            writeAt(writer, slot, List.of(client(3, "p.checkout", "cart:7070"), server(2), server(3), client(4, "mapmatch-bg", "navi:12000")));

            LinkMapWriter mapWriter = mock(LinkMapWriter.class);
            when(mapWriter.write(anyMap())).thenReturn(1);
            LinkIndexStore store = new LinkIndexStore(connector.connect(), Duration.ofSeconds(5));
            try (LinkJoinService service = new LinkJoinService(store, mapWriter, code -> ServiceType.OPENTELEMETRY_SERVER, BUCKETS, 3, 1)) {
                assertThat(service.getIdentifierCounts(slot)).isNull();

                LinkJoinService.JoinSummary summary = service.join(slot);

                assertThat(summary.pairs()).isEqualTo(2);
                assertThat(summary.droppedClient()).isEqualTo(1);
                assertThat(summary.droppedServer()).isEqualTo(1);
                Map<String, LinkCounts> counts = service.getIdentifierCounts(slot);
                // the servers' parent span id (= trace) is the low 8 bytes of their trace id: parent class root
                assertThat(counts).containsOnly(
                        Map.entry(CHECKOUT, new LinkCounts(2, 0)),
                        Map.entry(MAPMATCH, new LinkCounts(0, 1)),      // client 4 is not decided yet
                        Map.entry(SuppressKey.ofServer("cart", SuppressKey.PARENT_ROOT), new LinkCounts(2, 1)));
                assertThat(service.getIdentifierCounts(slot - 1)).isNull();
            }
        } finally {
            writer.close();
        }
    }

    @Test
    void writerSuppressesAfterSamplingAndKeepsServerEntries() {
        // frontend -> cart:7070 is forced, probe 2 per slot; every second trace is sampled out (modulo 2)
        LinkIndexSuppression suppression = new LinkIndexSuppression(null, Duration.ofSeconds(60), 2,
                SuppressKey.parseRules("p.frontend|cart:7070"), SuppressKey.Rules.EMPTY);
        LinkIndexWriter writer = new LinkIndexWriter(connector.connect(), BUCKETS, 300_000, 10_000, 1_000_000, 2,
                Duration.ofSeconds(5), now::get, suppression);
        writer.start();
        try {
            now.set(LinkIndexKeys.slotStartMillis(slot) + 1_000);
            int sampled = 0;
            int queued = 0;
            int aSampledTrace = -1;
            for (int trace = 1; trace <= 40; trace++) {
                if (LinkIndexKeys.isSampled(traceId(trace), 2)) {
                    sampled++;
                    aSampledTrace = trace;
                }
                if (writer.offer(client(trace, "p.frontend", "cart:7070"))) {
                    queued++;
                }
            }
            // only sampled traces reach the suppression: 2 probes queued, the rest suppressed, nothing else consumed the probe allowance
            assertThat(sampled).isGreaterThan(4);
            assertThat(queued).isEqualTo(2);
            assertThat(suppression.getProbed()).isEqualTo(2);
            assertThat(suppression.getSuppressed()).isEqualTo(sampled - 2);
            assertThat(writer.getNotSampled()).isEqualTo(40 - sampled);
            // other identifiers and SERVER entries of the same application are not affected
            assertThat(writer.offer(client(aSampledTrace, "p.frontend", "ad:9555"))).isTrue();
            assertThat(writer.offer(LinkEntry.server(traceId(aSampledTrace), 2, 3, "p.frontend", OTEL_SERVER, 1, false))).isTrue();
            // next slot: a fresh probe allowance
            now.set(LinkIndexKeys.slotStartMillis(slot + 1) + 1_000);
            assertThat(writer.offer(client(aSampledTrace, "p.frontend", "cart:7070"))).isTrue();
            assertThat(suppression.getProbed()).isEqualTo(3);
        } finally {
            writer.close();
        }
    }

    @Test
    void managerWritesAndCollectorReads() {
        // activate after 1 slot / 1 counted slot, minimum 1 unmatched
        SuppressManager manager = new SuppressManager(newStore(), new SuppressPolicy(1, 1, 1, 100, 10));
        LinkIndexSuppression suppression = new LinkIndexSuppression(newStore(), Duration.ofSeconds(60), 1,
                SuppressKey.Rules.EMPTY, SuppressKey.Rules.EMPTY);

        manager.apply(slot, Map.of(MAPMATCH, new LinkCounts(0, 500)));          // candidate
        suppression.refresh();
        assertThat(suppression.getActiveCount()).isZero();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", slot + 1)).isTrue();

        SuppressManager.SuppressSummary summary = manager.apply(slot + 1, Map.of(MAPMATCH, new LinkCounts(0, 500)));   // active
        assertThat(summary.activated()).isEqualTo(1);
        suppression.refresh();
        assertThat(suppression.getActive()).isEqualTo(Set.of(MAPMATCH));
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", slot + 2)).isTrue();      // probe
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", slot + 2)).isFalse();

        summary = manager.apply(slot + 2, Map.of(MAPMATCH, new LinkCounts(1, 100)));            // one match: released
        assertThat(summary.released()).isEqualTo(1);
        suppression.refresh();
        assertThat(suppression.getActiveCount()).isZero();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", slot + 3)).isTrue();
        assertThat(suppression.allow("mapmatch-bg", "navi:12000", slot + 3)).isTrue();
    }

    private void writeAt(LinkIndexWriter writer, long slot, List<LinkEntry> entries) {
        now.set(LinkIndexKeys.slotStartMillis(slot) + 1_000);
        long before = writer.getWritten();
        for (LinkEntry entry : entries) {
            assertThat(writer.offer(entry)).isTrue();
        }
        long deadline = System.currentTimeMillis() + 10_000;
        while (writer.getWritten() < before + entries.size()) {
            assertThat(System.currentTimeMillis()).as("writer flush").isLessThan(deadline);
            Thread.yield();
        }
    }

    private static LinkEntry server(int trace) {
        return LinkEntry.server(traceId(trace), trace, 3, "cart", OTEL_SERVER, trace, false);
    }

    private static LinkEntry client(int trace, String application, String destination) {
        return LinkEntry.client(traceId(trace), trace, 1, application, OTEL_SERVER, destination);
    }

    private static byte[] traceId(int trace) {
        byte[] id = new byte[LinkEntry.TRACE_ID_LENGTH];
        id[15] = (byte) trace;
        id[0] = (byte) (trace * 31);
        return id;
    }

    /**
     * The whole recovery path when both identifiers of a call relation are ACTIVE: two writers (the collector of the
     * caller and the one of the callee) see the calls in opposite orders, so the first-of-slot probes are different
     * calls; the probes by trace id are the same traces on both sides, go through the quota and the queue, are
     * joined and release both identifiers.
     */
    @Test
    void bothSidesSuppressedRecoverThroughTheTraceProbes() throws Exception {
        String clientKey = SuppressKey.of("caller", "callee:8080");
        String serverKey = SuppressKey.ofServer("callee", SuppressKey.serverClass(traceId(1), 1));   // parent = spanId = trace
        SuppressManager manager = new SuppressManager(newStore(), new SuppressPolicy(1, 1, 1, 100, 10));
        manager.apply(slot - 3, Map.of(clientKey, new LinkCounts(0, 50), serverKey, new LinkCounts(0, 50)));
        manager.apply(slot - 2, Map.of(clientKey, new LinkCounts(0, 50), serverKey, new LinkCounts(0, 50)));
        LinkIndexSuppression callerSide = new LinkIndexSuppression(newStore(), Duration.ofSeconds(60), 1, 4, true,
                SuppressKey.Rules.EMPTY, SuppressKey.Rules.EMPTY);
        LinkIndexSuppression calleeSide = new LinkIndexSuppression(newStore(), Duration.ofSeconds(60), 1, 4, true,
                SuppressKey.Rules.EMPTY, SuppressKey.Rules.EMPTY);
        callerSide.refresh();
        calleeSide.refresh();
        assertThat(callerSide.getActive()).containsExactlyInAnyOrder(clientKey, serverKey);

        int traces = 200;
        int probeTraces = 0;
        for (int trace = 1; trace <= traces; trace++) {
            if (LinkIndexKeys.isProbe(traceId(trace), 4)) {
                probeTraces++;
            }
        }
        assertThat(probeTraces).isBetween(20, 80);

        // slot quota 100 per writer with other traffic competing for it
        LinkIndexWriter caller = new LinkIndexWriter(connector.connect(), BUCKETS, 300_000, 10_000, 1,
                Duration.ofSeconds(5), now::get, callerSide, SlotQuota.of(100));
        LinkIndexWriter callee = new LinkIndexWriter(connector.connect(), BUCKETS, 300_000, 10_000, 1,
                Duration.ofSeconds(5), now::get, calleeSide, SlotQuota.of(100));
        caller.start();
        callee.start();
        try {
            now.set(LinkIndexKeys.slotStartMillis(slot) + 1_000);
            int queued = 0;
            for (int trace = 1; trace <= traces; trace++) {                  // the caller's collector: ascending
                queued += caller.offer(client(trace, "caller", "callee:8080")) ? 1 : 0;
            }
            for (int i = 0; i < 20; i++) {                                     // other traffic on the same collector
                queued += caller.offer(client(1000 + i, "other", "elsewhere:1")) ? 1 : 0;
            }
            for (int trace = traces; trace >= 1; trace--) {                   // the callee's collector: descending
                queued += callee.offer(LinkEntry.server(traceId(trace), trace, 3, "callee", OTEL_SERVER, 1, false)) ? 1 : 0;
            }
            // first-of-slot probe + the trace probes on each side, plus the other traffic
            assertThat(queued).isEqualTo(2 * (probeTraces + 1) + 20);
            assertThat(callerSide.getProbed()).isEqualTo(probeTraces + 1);
            assertThat(calleeSide.getProbed()).isEqualTo(probeTraces + 1);
            long deadline = System.currentTimeMillis() + 10_000;
            while (caller.getWritten() + callee.getWritten() < queued) {
                assertThat(System.currentTimeMillis()).as("writer flush").isLessThan(deadline);
                Thread.yield();
            }

            LinkMapWriter mapWriter = mock(LinkMapWriter.class);
            when(mapWriter.write(anyMap())).thenReturn(1);
            LinkIndexStore store = new LinkIndexStore(connector.connect(), Duration.ofSeconds(5));
            try (LinkJoinService service = new LinkJoinService(store, mapWriter, code -> ServiceType.OPENTELEMETRY_SERVER, BUCKETS, 3, 1)) {
                LinkJoinService.JoinSummary summary = service.join(slot);
                // exactly the trace probes pair up: the first-of-slot probes (trace 1 and trace 200) are different calls
                assertThat(summary.pairs()).isEqualTo(probeTraces);
                Map<String, LinkCounts> counts = service.getIdentifierCounts(slot);
                assertThat(counts.get(clientKey).matched()).isEqualTo(probeTraces);
                assertThat(counts.get(serverKey).matched()).isEqualTo(probeTraces);

                SuppressManager.SuppressSummary suppressed = manager.apply(slot, counts);
                assertThat(suppressed.released()).isEqualTo(2);
            }
            callerSide.refresh();
            calleeSide.refresh();
            assertThat(callerSide.getActiveCount()).isZero();
            assertThat(calleeSide.getActiveCount()).isZero();
            // the next slot is indexed in full again
            now.set(LinkIndexKeys.slotStartMillis(slot + 1) + 1_000);
            assertThat(caller.offer(client(5, "caller", "callee:8080"))).isTrue();
            assertThat(caller.offer(client(6, "caller", "callee:8080"))).isTrue();
        } finally {
            caller.close();
            callee.close();
            callerSide.close();
            calleeSide.close();
        }
    }
}
