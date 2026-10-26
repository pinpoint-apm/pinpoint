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
    void queueFullIsCountedPerKind() {
        LinkIndexWriter writer = writer(LinkIndexSuppression.NONE, SlotQuota.of(100), 1);

        assertThat(writer.offer(server(1, "p.cart"))).isTrue();
        assertThat(writer.offer(client(2, "p.checkout", "cart:7070"))).isFalse();

        assertThat(writer.getDropped(DropReason.QUEUE_FULL, LinkKind.CLIENT)).isEqualTo(1);
        assertThat(writer.getDropped(DropReason.QUEUE_FULL, LinkKind.SERVER)).isZero();
        assertThat(writer.getDroppedQueueFull()).isEqualTo(1);
    }
}
