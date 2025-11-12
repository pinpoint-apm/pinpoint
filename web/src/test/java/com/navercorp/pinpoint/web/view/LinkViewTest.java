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

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.nodes.AgentServerGroupListWriter;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.service.AlertViewService;
import com.navercorp.pinpoint.web.applicationmap.view.AgentLinkView;
import com.navercorp.pinpoint.web.applicationmap.view.ApplicationTimeSeriesHistogramLinkView;
import com.navercorp.pinpoint.web.applicationmap.view.LinkView;
import com.navercorp.pinpoint.web.applicationmap.view.NodeView;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class LinkViewTest {

    final Logger logger = LogManager.getLogger(this.getClass());

    final ObjectMapper MAPPER = mapper();


    static ObjectMapper mapper() {
        ObjectMapper mapper = Jackson.newMapper();

        SimpleModule module = new SimpleModule();
        AlertViewService alertViewService = new AlertViewService();
        AgentServerGroupListWriter agentServerGroupListWriter = new AgentServerGroupListWriter();
        module.addSerializer(NodeView.class, new NodeView.NodeViewSerializer(alertViewService, agentServerGroupListWriter));
        module.addSerializer(LinkView.class, new LinkView.LinkViewSerializer(alertViewService, agentServerGroupListWriter));
        mapper.registerModule(module);

        return mapper;
    }


    @Test
    public void testSerializeV1() throws JsonProcessingException {
        TimeHistogramFormat version = TimeHistogramFormat.V3;
        LinkView linkView = newLinkView(version);

        ObjectWriter objectWriter = MAPPER.writerWithDefaultPrettyPrinter();
        String s = objectWriter.writeValueAsString(linkView);

        logger.debug("{}", s);
    }

    @Test
    public void testSerializeV2() throws JsonProcessingException {
        TimeHistogramFormat version = TimeHistogramFormat.V3;
        LinkView linkView = newLinkView(version);

        ObjectWriter objectWriter = MAPPER.writerWithDefaultPrettyPrinter();
        String s = objectWriter.writeValueAsString(linkView);

        logger.debug("{}", s);
    }


    @Test
    public void testSerializeV3() throws JsonProcessingException {
        TimeHistogramFormat version = TimeHistogramFormat.V3;
        LinkView linkView = newLinkView(version);

        ObjectWriter objectWriter = MAPPER.writerWithDefaultPrettyPrinter();
        String s = objectWriter.writeValueAsString(linkView);

        logger.debug("{}", s);
    }

    private LinkView newLinkView(TimeHistogramFormat version) {
        Node node1 = new Node(new Application("test1", ServiceType.STAND_ALONE));
        Node node2 = new Node(new Application("test1", ServiceType.STAND_ALONE));

        Link link = new Link(LinkDirection.IN_LINK, node1, node2, Range.between(0, 1));
        return new LinkView(link, ApplicationTimeSeriesHistogramLinkView.emptyView(), AgentLinkView.emptyView());
    }
}
