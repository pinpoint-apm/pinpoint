/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.link.CreateType;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class LinkSerializerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testSerialize() throws Exception {
        AgentHistogramList list = new AgentHistogramList();
        AgentHistogram histogram = new AgentHistogram(new Application("test", ServiceType.STAND_ALONE));
        list.addAgentHistogram(histogram);
        Node node1 = new Node(new Application("test1", ServiceType.STAND_ALONE));
        Node node2 = new Node(new Application("test1", ServiceType.STAND_ALONE));

        Link link = new Link(CreateType.Source, node1, node2, new Range(0, 1));
        ObjectWriter objectWriter = MAPPER.writerWithDefaultPrettyPrinter();
        String s = objectWriter.writeValueAsString(link);

        logger.debug(s);
    }
}
