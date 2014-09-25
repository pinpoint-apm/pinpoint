package com.nhn.pinpoint.web.view;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.ServerBuilder;
import com.nhn.pinpoint.web.applicationmap.ServerInstanceList;
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
        ServerBuilder builder = new ServerBuilder();
        AgentInfoBo agentInfoBo = new AgentInfoBo();
        agentInfoBo.setAgentId("agentId");
        agentInfoBo.setServiceType(ServiceType.TOMCAT);
        agentInfoBo.setHostname("testcomputer");
        HashSet<AgentInfoBo> set = new HashSet<AgentInfoBo>();
        set.add(agentInfoBo);
        builder.addAgentInfo(set);
        ServerInstanceList serverInstanceList = builder.build();
        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();
        String json = objectWriter.writeValueAsString(serverInstanceList);
        logger.debug(json);
    }
}
