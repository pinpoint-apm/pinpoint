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

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.web.applicationmap.ServerBuilder;
import com.navercorp.pinpoint.web.applicationmap.ServerInstanceList;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * @author emeroad
 */
public class ServerInstanceListSerializerTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSerialize() throws Exception {
        AgentInfoBo.Builder agentInfoBuilder = new AgentInfoBo.Builder();
        agentInfoBuilder.agentId("agentId");
        agentInfoBuilder.serviceType(ServiceType.TOMCAT);
        agentInfoBuilder.hostName("testcomputer");

        AgentInfoBo agentInfoBo = agentInfoBuilder.build();

        HashSet<AgentInfoBo> set = new HashSet<AgentInfoBo>();
        set.add(agentInfoBo);

        ServerBuilder builder = new ServerBuilder(null);
        builder.addAgentInfo(set);
        ServerInstanceList serverInstanceList = builder.build();
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        String json = objectWriter.writeValueAsString(serverInstanceList);
        logger.debug(json);
    }
}
