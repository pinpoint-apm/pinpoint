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

import com.navercorp.pinpoint.otlp.trace.linkindex.LinkJoinService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OtlpLinkJoinMetricsTest {

    private static final String PREFIX = OtlpLinkJoinJobConfig.OtlpLinkJoinMetrics.METRIC_PREFIX;

    @Test
    void registersTotalsAndGauges() {
        LinkJoinService service = mock(LinkJoinService.class);
        when(service.getTotalEntries()).thenReturn(4151L);
        when(service.getTotalPairs()).thenReturn(1683L);
        when(service.getTotalRows()).thenReturn(34L);
        when(service.getTotalDroppedServer()).thenReturn(626L);
        when(service.getTotalDroppedClient()).thenReturn(159L);
        when(service.getTotalDeletedKeys()).thenReturn(472L);
        when(service.getLastMaxBucketEntries()).thenReturn(61L);
        OtlpLinkJoinLauncher launcher = mock(OtlpLinkJoinLauncher.class);
        when(launcher.getFailedLaunches()).thenReturn(2L);
        when(launcher.getLastDoneSlotAgeSeconds()).thenReturn(30L);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        new OtlpLinkJoinJobConfig.OtlpLinkJoinMetrics(service, launcher, registry);

        assertThat(registry.get(PREFIX + "entries").functionCounter().count()).isEqualTo(4151);
        assertThat(registry.get(PREFIX + "pairs").functionCounter().count()).isEqualTo(1683);
        assertThat(registry.get(PREFIX + "rows").functionCounter().count()).isEqualTo(34);
        assertThat(registry.get(PREFIX + "dropped").tag("side", "server").functionCounter().count()).isEqualTo(626);
        assertThat(registry.get(PREFIX + "dropped").tag("side", "client").functionCounter().count()).isEqualTo(159);
        assertThat(registry.get(PREFIX + "deleted.keys").functionCounter().count()).isEqualTo(472);
        assertThat(registry.get(PREFIX + "max.bucket.entries").gauge().value()).isEqualTo(61);
        assertThat(registry.get(PREFIX + "oversized.buckets").gauge().value()).isZero();
        assertThat(registry.get(PREFIX + "skipped.slots").functionCounter().count()).isZero();
        assertThat(registry.get(PREFIX + "failed.launches").functionCounter().count()).isEqualTo(2);
        assertThat(registry.get(PREFIX + "last.done.slot.age.seconds").gauge().value()).isEqualTo(30);
        // 8 counters (dropped twice) + 2 slot gauges + 2 launcher counters + 1 lag gauge
        assertThat(registry.getMeters()).hasSize(13);
    }
}
