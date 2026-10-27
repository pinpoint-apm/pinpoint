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

import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexWriter.DropReason;
import com.navercorp.pinpoint.otlp.trace.linkindex.redis.LinkIndexConnection;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.LinkIndexSuppression;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressKey;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.navercorp.pinpoint.otlp.trace.linkindex.LinkEntryCodecTest.traceId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * The offer path of {@link LinkIndexWriter} (suppression, quota, drop counters); the writes are covered by
 * {@link LinkIndexRedisTest}. The writer is not started, so offered entries stay queued.
 */
class LinkIndexWriterTest {

    private static final long NOW = LinkIndexKeys.slotStartMillis(29_000_000L) + 1_000;

    private static LinkIndexWriter writer(LinkIndexSuppression suppression, SlotQuota quota, int queueSize) {
        return new LinkIndexWriter(mock(LinkIndexConnection.class), 16, 300_000, queueSize, 1,
                Duration.ofSeconds(5), () -> NOW, suppression, quota);
    }

    private static LinkEntry client(int trace, String application, String destination) {
        return LinkEntry.client(traceId(trace), trace, 1, application, 1220, destination);
    }

    private static LinkEntry server(int trace, String application) {
        return LinkEntry.server(traceId(trace), trace, 3, application, 1220, 10, false);
    }

    @Test
    void identifierAndSlotLimitsAreCountedPerReasonAndKind() {
        LinkIndexWriter writer = writer(LinkIndexSuppression.NONE, new SlotQuota(4, 50, 100), 100);

        assertThat(writer.offer(client(1, "mapmatch-bg", "navi:12000"))).isTrue();
        assertThat(writer.offer(client(2, "mapmatch-bg", "navi:12000"))).isTrue();
        assertThat(writer.offer(client(3, "mapmatch-bg", "navi:12000"))).isFalse();
        assertThat(writer.offer(server(4, "p.cart"))).isTrue();
        assertThat(writer.offer(server(5, "p.cart"))).isTrue();
        assertThat(writer.offer(server(6, "p.cart"))).isFalse();

        assertThat(writer.getDropped(DropReason.IDENTIFIER_LIMIT, LinkKind.CLIENT)).isEqualTo(1);
        assertThat(writer.getDropped(DropReason.SLOT_LIMIT, LinkKind.SERVER)).isEqualTo(1);
        assertThat(writer.getDroppedIdentifierLimit()).isEqualTo(1);
        assertThat(writer.getDroppedSlotLimit()).isEqualTo(1);
        assertThat(writer.getQueueSize()).isEqualTo(4);
    }

    @Test
    void suppressedEntriesUseNoQuota() {
        LinkIndexSuppression suppression = new LinkIndexSuppression(null, Duration.ofSeconds(60), 1,
                SuppressKey.parseRules("mapmatch-bg|navi:12000"), SuppressKey.Rules.EMPTY);
        LinkIndexWriter writer = writer(suppression, new SlotQuota(2, 100, 100), 100);

        assertThat(writer.offer(client(1, "mapmatch-bg", "navi:12000"))).isTrue();     // probe
        for (int i = 2; i < 10; i++) {
            assertThat(writer.offer(client(i, "mapmatch-bg", "navi:12000"))).isFalse();
        }
        assertThat(suppression.getSuppressed()).isEqualTo(8);
        // one quota entry left for the others: the suppressed entries did not take it
        assertThat(writer.offer(client(10, "p.checkout", "cart:7070"))).isTrue();
        assertThat(writer.getDroppedSlotLimit()).isZero();
        assertThat(writer.getDroppedIdentifierLimit()).isZero();
    }

    @Test
    void suppressedServerEntriesUseNoQuotaAndAreSplitByParentClass() {
        // every SERVER entry of gateway whose parent is a B3 root span is forced; probe 1 per slot
        LinkIndexSuppression suppression = new LinkIndexSuppression(null, Duration.ofSeconds(60), 1,
                SuppressKey.parseRules("server:sec.gateway.internal|root"), SuppressKey.Rules.EMPTY);
        LinkIndexWriter writer = writer(suppression, new SlotQuota(3, 100, 100), 100);

        assertThat(writer.offer(rootParentServer(1, "sec.gateway.internal"))).isTrue();     // probe
        for (int i = 2; i < 8; i++) {
            assertThat(writer.offer(rootParentServer(i, "sec.gateway.internal"))).isFalse();
        }
        assertThat(suppression.getSuppressed(LinkKind.SERVER)).isEqualTo(6);
        assertThat(suppression.getSuppressed(LinkKind.CLIENT)).isZero();
        // a SERVER entry of the same application with another parent is not suppressed
        assertThat(writer.offer(server(8, "sec.gateway.internal"))).isTrue();
        // the suppressed entries did not take the slot quota: one entry left for the others
        assertThat(writer.offer(client(9, "p.checkout", "cart:7070"))).isTrue();
        assertThat(writer.offer(client(10, "p.checkout", "cart:7070"))).isFalse();
        assertThat(writer.getDroppedSlotLimit()).isEqualTo(1);
        assertThat(writer.getQueueSize()).isEqualTo(3);
    }

    /** a SERVER entry whose parent span id is the low 8 bytes of its trace id (B3 / Jaeger root span) */
    private static LinkEntry rootParentServer(int trace, String application) {
        byte[] traceId = traceId(trace);
        long low = BytesUtils.bytesToLong(traceId, 8);
        return LinkEntry.server(traceId, low, 3, application, 1220, 10, false);
    }

    @Test
    void queueFullIsCountedPerKind() {
        LinkIndexWriter writer = writer(LinkIndexSuppression.NONE, SlotQuota.of(100), 1);

        assertThat(writer.offer(server(1, "p.cart"))).isTrue();
        assertThat(writer.offer(client(2, "p.checkout", "cart:7070"))).isFalse();

        assertThat(writer.getDropped(DropReason.QUEUE_FULL, LinkKind.CLIENT)).isEqualTo(1);
        assertThat(writer.getDropped(DropReason.QUEUE_FULL, LinkKind.SERVER)).isZero();
        assertThat(writer.getDroppedQueueFull()).isEqualTo(1);
    }

    @Test
    void queueFullGivesTheQuotaBack() {
        // slot quota 10, 2 per CLIENT identifier, queue of 1: the first entry fills the queue
        SlotQuota quota = new SlotQuota(10, 20, 100);
        LinkIndexWriter writer = writer(LinkIndexSuppression.NONE, quota, 1);
        assertThat(writer.offer(server(1, "p.cart"))).isTrue();

        // both entries of A are refused by the full queue: neither takes A's share nor the slot quota
        assertThat(writer.offer(client(2, "a", "cart:7070"))).isFalse();
        assertThat(writer.offer(client(3, "a", "cart:7070"))).isFalse();
        assertThat(writer.getDroppedQueueFull()).isEqualTo(2);
        assertThat(writer.getDroppedIdentifierLimit()).isZero();

        // the slot ends with the one queued entry counted, not three
        quota.acquire(LinkKind.SERVER, "p.cart", NOW + 60_000);     // first entry of the next slot ends it
        assertThat(quota.getSlotEntries()).isEqualTo(1);
    }
}
