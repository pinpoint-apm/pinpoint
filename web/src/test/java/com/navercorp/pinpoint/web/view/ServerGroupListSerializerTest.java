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
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.web.applicationmap.ServerGroupListTest;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.view.ServerGroupListView;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

/**
 * @author emeroad
 */
public class ServerGroupListSerializerTest {
    Logger logger = LogManager.getLogger(this.getClass());

    HyperLinkFactory hyperLinkFactory = new HyperLinkFactory(List.of());

    ObjectMapper mapper = Jackson.newMapper();

    @Test
    public void testSerialize() throws Exception {
        AgentAndStatus agentInfo = ServerGroupListTest.createAgentInfo("agentId1", "testHost");
        Set<AgentAndStatus> agentInfoSet = Set.of(agentInfo);

        ServerBuilder builder = new ServerBuilder();
        builder.addAgentInfo(agentInfoSet);

        ServerGroupList serverGroupList = builder.build();
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        String json = objectWriter.writeValueAsString(new ServerGroupListView(serverGroupList, hyperLinkFactory));
        logger.debug("{}", json);
    }

}
