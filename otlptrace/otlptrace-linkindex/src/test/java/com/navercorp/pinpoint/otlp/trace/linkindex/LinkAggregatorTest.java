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

import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.IntFunction;

import static com.navercorp.pinpoint.otlp.trace.linkindex.LinkEntryCodecTest.traceId;
import static org.assertj.core.api.Assertions.assertThat;

class LinkAggregatorTest {

    static final IntFunction<ServiceType> LOOKUP = code -> {
        if (code == ServiceType.OPENTELEMETRY_SERVER.getCode()) {
            return ServiceType.OPENTELEMETRY_SERVER;
        }
        return ServiceType.UNKNOWN;
    };

    private static final int OTEL_SERVER = ServiceType.OPENTELEMETRY_SERVER.getCode();

    private static MatchedPair pair(int trace, int elapsed, boolean error, long slot, LinkSource source) {
        LinkEntry server = LinkEntry.server(traceId(trace), trace, 3, "p.cart", OTEL_SERVER, elapsed, error);
        LinkEntry client = LinkEntry.client(traceId(trace), trace, 1, "p.checkout", OTEL_SERVER, "cart:7070");
        return new MatchedPair(server, client, slot, source);
    }

    @Test
    void aggregatesPerCalleeCallerDestinationSlotAndSource() {
        LinkAggregator aggregator = new LinkAggregator(LOOKUP, 1);
        aggregator.add(pair(1, 10, false, 100, LinkSource.SAME_SLOT));
        aggregator.add(pair(2, 4000, true, 100, LinkSource.SAME_SLOT));
        aggregator.add(pair(3, 20, false, 99, LinkSource.CROSS_SLOT));

        Map<LinkKey, LinkStat> stats = aggregator.getStats();
        assertThat(stats).hasSize(2);

        Vertex callee = Vertex.of(3, "p.cart", ServiceType.OPENTELEMETRY_SERVER);
        Vertex caller = Vertex.of(1, "p.checkout", ServiceType.OPENTELEMETRY_SERVER);
        LinkStat same = stats.get(new LinkKey(callee, caller, "cart:7070", 100, LinkSource.SAME_SLOT));
        HistogramSchema schema = ServiceType.OPENTELEMETRY_SERVER.getHistogramSchema();
        assertThat(same.getCount()).isEqualTo(2);
        assertThat(same.getSum()).isEqualTo(4010);
        assertThat(same.getMax()).isEqualTo(4000);
        assertThat(same.getHistogram())
                .containsEntry(schema.findHistogramSlot(10, false).getSlotCode(), 1L)
                .containsEntry(schema.findHistogramSlot(4000, true).getSlotCode(), 1L);

        LinkStat cross = stats.get(new LinkKey(callee, caller, "cart:7070", 99, LinkSource.CROSS_SLOT));
        assertThat(cross.getCount()).isEqualTo(1);
    }

    @Test
    void weightScalesCountsAndSumButNotMax() {
        LinkAggregator aggregator = new LinkAggregator(LOOKUP, 4);
        aggregator.add(pair(1, 10, false, 100, LinkSource.SAME_SLOT));

        LinkStat stat = aggregator.getStats().values().iterator().next();
        assertThat(stat.getCount()).isEqualTo(4);
        assertThat(stat.getSum()).isEqualTo(40);
        assertThat(stat.getMax()).isEqualTo(10);
    }

    @Test
    void merge() {
        LinkAggregator a = new LinkAggregator(LOOKUP, 1);
        a.add(pair(1, 10, false, 100, LinkSource.SAME_SLOT));
        LinkAggregator b = new LinkAggregator(LOOKUP, 1);
        b.add(pair(2, 30, false, 100, LinkSource.SAME_SLOT));
        b.add(pair(3, 30, false, 101, LinkSource.SAME_SLOT));

        a.merge(b);

        assertThat(a.getStats()).hasSize(2);
        long total = a.getStats().values().stream().mapToLong(LinkStat::getCount).sum();
        assertThat(total).isEqualTo(3);
    }
}
