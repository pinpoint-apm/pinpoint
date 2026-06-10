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

package com.navercorp.pinpoint.otlp.trace.collector.service;

import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpUriStatSpan;
import com.navercorp.pinpoint.uristat.collector.dao.UriStatDao;
import com.navercorp.pinpoint.uristat.collector.model.UriStat;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpUriStatServiceTest {

    private final List<UriStat> inserted = new ArrayList<>();
    private final UriStatDao captureDao = inserted::addAll;
    private final OtlpUriStatService service = new OtlpUriStatService(captureDao, () -> "tenant1");

    private static OtlpUriStatSpan span(String uri, long startTime, int elapsed, boolean error) {
        return new OtlpUriStatSpan("svc", "app", "agent", uri, startTime, elapsed, error);
    }

    @Test
    void store_emptyInput_noInsert() {
        service.store(List.of());
        assertThat(inserted).isEmpty();
    }

    @Test
    void store_sameUriSameWindow_aggregatesIntoSingleRecord() {
        // Three requests to the same endpoint inside one 30s window: 50ms (bucket0), 150ms (bucket1),
        // 150ms (bucket1). One aggregated record, not three.
        service.store(List.of(
                span("/users/{id}", 1_000, 50, false),
                span("/users/{id}", 2_000, 150, false),
                span("/users/{id}", 5_000, 150, true)));

        assertThat(inserted).hasSize(1);
        UriStat stat = inserted.get(0);
        assertThat(stat.getUri()).isEqualTo("/users/{id}");
        assertThat(stat.getCount()).isEqualTo(3);
        assertThat(stat.getTimestamp()).isEqualTo(0); // 30s window floor
        assertThat(stat.getTot0()).isEqualTo(1);
        assertThat(stat.getTot1()).isEqualTo(2);
        assertThat(stat.getMaxLatencyMs()).isEqualTo(150);
        assertThat(stat.getTotalTimeMs()).isEqualTo(350);
        // only the third request was an error → failure bucket1 = 1
        assertThat(stat.getFailureCount()).isEqualTo(1);
        assertThat(stat.getFail1()).isEqualTo(1);
    }

    @Test
    void store_differentWindows_producesSeparateRecords() {
        // Same uri, but startTimes fall into different 30s windows (0 and 30000).
        service.store(List.of(
                span("/a", 10_000, 100, false),
                span("/a", 40_000, 100, false)));

        assertThat(inserted).hasSize(2);
        assertThat(inserted).extracting(UriStat::getTimestamp).containsExactlyInAnyOrder(0L, 30_000L);
    }

    @Test
    void store_differentUris_producesSeparateRecords() {
        service.store(List.of(
                span("/a", 1_000, 100, false),
                span("/b", 1_000, 100, false)));

        assertThat(inserted).hasSize(2);
        assertThat(inserted).extracting(UriStat::getUri).containsExactlyInAnyOrder("/a", "/b");
    }

    @Test
    void store_windowBoundary_floorsToWindowStart() {
        // 29,999ms still belongs to window 0; 30,000ms opens window 30,000 — exact floor semantics.
        service.store(List.of(
                span("/a", 29_999, 100, false),
                span("/a", 30_000, 100, false)));

        assertThat(inserted).hasSize(2);
        assertThat(inserted).extracting(UriStat::getTimestamp).containsExactlyInAnyOrder(0L, 30_000L);
    }

    @Test
    void store_setsTenantAndVersion() {
        service.store(List.of(span("/a", 1_000, 9_000, false)));

        UriStat stat = inserted.get(0);
        assertThat(stat.getTenantId()).isEqualTo("tenant1");
        assertThat(stat.getVersion()).isEqualTo(0);
        assertThat(stat.getTot7()).isEqualTo(1); // 9000ms → OVER_8000 bucket
    }
}
