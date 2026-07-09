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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.UriStatHistogram;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpUriStatMapperTest {

    private static final String AGENT_ID = "agent-1";
    private static final String AGENT_NAME = "agent-name";
    private static final String APP_NAME = "app-1";
    private static final String SERVICE_NAME = "svc-1";
    private static final long INTERVAL = 30_000L;

    private static IdAndName id(String agentId, String applicationName, String serviceName) {
        return new IdAndName(agentId, AGENT_NAME, applicationName, serviceName);
    }

    private OtlpUriStatMapper.Aggregator newAggregator() {
        return new OtlpUriStatMapper(INTERVAL).newAggregator();
    }

    @Test
    void aggregatesSameUriInSameWindow() {
        OtlpUriStatMapper.Aggregator agg = newAggregator();
        IdAndName idAndName = id(AGENT_ID, APP_NAME, SERVICE_NAME);
        // two requests to same uri in the same collection window: elapsed 50ms (slot 0) and 200ms (slot 1)
        agg.add(idAndName, "/users/{id}", 50, false, 1_000L);
        agg.add(idAndName, "/users/{id}", 200, false, 2_000L);

        List<AgentUriStatBo> result = agg.build();
        assertThat(result).hasSize(1);
        AgentUriStatBo bo = result.get(0);
        assertThat(bo.getServiceName()).isEqualTo(SERVICE_NAME);
        assertThat(bo.getApplicationName()).isEqualTo(APP_NAME);
        assertThat(bo.getAgentId()).isEqualTo(AGENT_ID);
        assertThat(bo.getEachUriStatBoList()).hasSize(1);

        EachUriStatBo each = bo.getEachUriStatBoList().get(0);
        assertThat(each.getUri()).isEqualTo("/users/{id}");
        assertThat(each.getTimestamp()).isZero(); // 1000/2000 floored to 30000-window → 0

        UriStatHistogram total = each.getTotalHistogram();
        assertThat(total.getTimestampHistogram()).containsExactly(1, 1, 0, 0, 0, 0, 0, 0);
        assertThat(total.getTotal()).isEqualTo(250L);
        assertThat(total.getMax()).isEqualTo(200L);
        // no errors → failed histogram is null (downstream treats as empty)
        assertThat(each.getFailedHistogram()).isNull();
    }

    @Test
    void recordsErrorIntoFailedHistogram() {
        OtlpUriStatMapper.Aggregator agg = newAggregator();
        IdAndName idAndName = id(AGENT_ID, APP_NAME, SERVICE_NAME);
        agg.add(idAndName, "/order", 120, false, 0L);  // ok, slot 1
        agg.add(idAndName, "/order", 400, true, 0L);    // error, slot 2

        EachUriStatBo each = agg.build().get(0).getEachUriStatBoList().get(0);
        UriStatHistogram total = each.getTotalHistogram();
        assertThat(total.getTimestampHistogram()).containsExactly(0, 1, 1, 0, 0, 0, 0, 0);
        assertThat(total.getMax()).isEqualTo(400L);

        UriStatHistogram failed = each.getFailedHistogram();
        assertThat(failed).isNotNull();
        assertThat(failed.getTimestampHistogram()).containsExactly(0, 0, 1, 0, 0, 0, 0, 0);
        assertThat(failed.getTotal()).isEqualTo(400L);
        assertThat(failed.getMax()).isEqualTo(400L);
    }

    @Test
    void separatesDifferentAgents() {
        OtlpUriStatMapper.Aggregator agg = newAggregator();
        agg.add(id("agent-a", APP_NAME, SERVICE_NAME), "/a", 10, false, 0L);
        agg.add(id("agent-b", APP_NAME, SERVICE_NAME), "/a", 10, false, 0L);

        List<AgentUriStatBo> result = agg.build();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AgentUriStatBo::getAgentId)
                .containsExactlyInAnyOrder("agent-a", "agent-b");
    }

    @Test
    void separatesDifferentTimeWindows() {
        OtlpUriStatMapper.Aggregator agg = newAggregator();
        IdAndName idAndName = id(AGENT_ID, APP_NAME, SERVICE_NAME);
        agg.add(idAndName, "/a", 10, false, 1_000L);          // window 0
        agg.add(idAndName, "/a", 10, false, INTERVAL + 5_000L); // window 30000

        List<EachUriStatBo> eachList = agg.build().get(0).getEachUriStatBoList();
        assertThat(eachList).hasSize(2);
        assertThat(eachList).extracting(EachUriStatBo::getTimestamp)
                .containsExactlyInAnyOrder(0L, INTERVAL);
    }

    @Test
    void skipsEmptyUri() {
        OtlpUriStatMapper.Aggregator agg = newAggregator();
        agg.add(id(AGENT_ID, APP_NAME, SERVICE_NAME), "", 10, false, 0L);
        agg.add(id(AGENT_ID, APP_NAME, SERVICE_NAME), null, 10, false, 0L);
        assertThat(agg.build()).isEmpty();
    }

    @Test
    void fallsBackToDefaultServiceNameWhenBlank() {
        OtlpUriStatMapper.Aggregator agg = newAggregator();
        agg.add(id(AGENT_ID, APP_NAME, ""), "/a", 10, false, 0L);
        assertThat(agg.build().get(0).getServiceName()).isEqualTo("DEFAULT");
    }

    @Test
    void classifiesOver8000IntoLastSlot() {
        OtlpUriStatMapper.Aggregator agg = newAggregator();
        agg.add(id(AGENT_ID, APP_NAME, SERVICE_NAME), "/slow", 9_000, false, 0L);
        UriStatHistogram total = agg.build().get(0).getEachUriStatBoList().get(0).getTotalHistogram();
        assertThat(total.getTimestampHistogram()).containsExactly(0, 0, 0, 0, 0, 0, 0, 1);
    }
}
