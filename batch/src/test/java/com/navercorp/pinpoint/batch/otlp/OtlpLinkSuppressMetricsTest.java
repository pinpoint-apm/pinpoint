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

package com.navercorp.pinpoint.batch.otlp;

import com.navercorp.pinpoint.otlp.trace.linkindex.LinkKind;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressManager;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OtlpLinkSuppressMetricsTest {
    private static final String PREFIX = OtlpLinkJoinJobConfig.OtlpLinkSuppressMetrics.METRIC_PREFIX;

    @Test
    void registersTotalsAndGauges() {
        SuppressManager manager = mock(SuppressManager.class);
        when(manager.getActivated(LinkKind.CLIENT)).thenReturn(3L);
        when(manager.getActivated(LinkKind.SERVER)).thenReturn(1L);
        when(manager.getReleased(LinkKind.CLIENT)).thenReturn(2L);
        when(manager.getExpired(LinkKind.CLIENT)).thenReturn(5L);
        when(manager.getErrors()).thenReturn(1L);
        when(manager.getLastCandidates()).thenReturn(4L);
        when(manager.getLastActive()).thenReturn(2L);
        when(manager.getLastCandidatesServer()).thenReturn(3L);
        when(manager.getLastActiveServer()).thenReturn(1L);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        new OtlpLinkJoinJobConfig.OtlpLinkSuppressMetrics(manager, registry);

        assertThat(PREFIX).isEqualTo("batch.otlp.linkjoin.suppress.");
        assertThat(registry.get(PREFIX + "activated").tag("kind", "client").functionCounter().count()).isEqualTo(3);
        assertThat(registry.get(PREFIX + "activated").tag("kind", "server").functionCounter().count()).isEqualTo(1);
        assertThat(registry.get(PREFIX + "released").tag("kind", "client").functionCounter().count()).isEqualTo(2);
        assertThat(registry.get(PREFIX + "expired").tag("kind", "client").functionCounter().count()).isEqualTo(5);
        assertThat(registry.get(PREFIX + "expired").tag("kind", "server").functionCounter().count()).isZero();
        assertThat(registry.get(PREFIX + "errors").functionCounter().count()).isEqualTo(1);
        assertThat(registry.get(PREFIX + "candidates").gauge().value()).isEqualTo(4);
        assertThat(registry.get(PREFIX + "active").gauge().value()).isEqualTo(2);
        assertThat(registry.get(PREFIX + "candidates.server").gauge().value()).isEqualTo(3);
        assertThat(registry.get(PREFIX + "active.server").gauge().value()).isEqualTo(1);
        assertThat(registry.getMeters()).hasSize(11);
    }
}
