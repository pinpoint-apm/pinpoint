/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.servicemap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.nodes.AgentServerGroupListWriter;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.service.AlertViewService;
import com.navercorp.pinpoint.web.applicationmap.view.AgentHistogramNodeView;
import com.navercorp.pinpoint.web.applicationmap.view.AgentTimeSeriesHistogramNodeView;
import com.navercorp.pinpoint.web.applicationmap.view.ApplicationApdexScoreSlotView;
import com.navercorp.pinpoint.web.applicationmap.view.ApplicationTimeSeriesHistogramNodeView;
import com.navercorp.pinpoint.web.applicationmap.view.NodeView;
import com.navercorp.pinpoint.web.applicationmap.view.ServerListNodeView;
import com.navercorp.pinpoint.common.server.bo.Application;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.List;

/**
 * The frontend reads the group's Apdex from the "apdex" field of this view, so the field name and
 * the merged formula are part of the response contract.
 */
public class ServiceGroupNodeViewTest {

    private final ObjectMapper mapper = mapper();

    static ObjectMapper mapper() {
        ObjectMapper mapper = Jackson.newMapper();

        SimpleModule module = new SimpleModule();
        AlertViewService alertViewService = new AlertViewService();
        AgentServerGroupListWriter agentServerGroupListWriter = new AgentServerGroupListWriter();
        module.addSerializer(NodeView.class, new NodeView.NodeViewSerializer(alertViewService, agentServerGroupListWriter));
        mapper.registerModule(module);

        return mapper;
    }

    @Test
    public void serialize_apdex() throws JsonProcessingException, JSONException {
        ServiceGroupNodeView view = new ServiceGroupNodeView("myService", List.of(
                newNodeView("app1", 6, 2, 2),
                newNodeView("app2", 4, 2, 4)
        ));

        String actualStr = mapper.writeValueAsString(view);

        JSONAssert.assertEquals("{\"key\":\"myService\",\"type\":\"service\",\"serviceName\":\"myService\","
                        + "\"apdex\":{\"apdexScore\":0.6,"
                        + "\"apdexFormula\":{\"satisfiedCount\":10,\"toleratingCount\":4,\"totalSamples\":20}}}",
                actualStr, JSONCompareMode.LENIENT);
    }

    @Test
    public void serialize_apdex_emptyGroup() throws JsonProcessingException, JSONException {
        ServiceGroupNodeView view = new ServiceGroupNodeView("myService", List.of());

        String actualStr = mapper.writeValueAsString(view);

        JSONAssert.assertEquals("{\"apdex\":{\"apdexScore\":0.0,"
                        + "\"apdexFormula\":{\"satisfiedCount\":0,\"toleratingCount\":0,\"totalSamples\":0}}}",
                actualStr, JSONCompareMode.LENIENT);
    }

    private NodeView newNodeView(String applicationName, long fastCount, long normalCount, long slowCount) {
        Application application = new Application(applicationName, ServiceType.STAND_ALONE);

        Node node = new Node(application);
        node.setNodeHistogram(newNodeHistogram(application, fastCount, normalCount, slowCount));

        return new NodeView(node,
                ApplicationTimeSeriesHistogramNodeView.emptyView(),
                ApplicationApdexScoreSlotView.emptyView(),
                ServerListNodeView.emptyView(),
                AgentHistogramNodeView.emptyView(),
                AgentTimeSeriesHistogramNodeView.emptyView(),
                true);
    }

    private NodeHistogram newNodeHistogram(Application application, long fastCount, long normalCount, long slowCount) {
        HistogramSchema schema = application.getServiceType().getHistogramSchema();

        Histogram histogram = new Histogram(application.getServiceType());
        histogram.addCallCount(schema.getFastSlot().getSlotTime(), fastCount);
        histogram.addCallCount(schema.getNormalSlot().getSlotTime(), normalCount);
        histogram.addCallCount(schema.getSlowSlot().getSlotTime(), slowCount);

        NodeHistogram.Builder builder = NodeHistogram.newBuilder(application, Range.between(0, 1));
        builder.setApplicationHistogram(histogram);
        return builder.build();
    }
}
