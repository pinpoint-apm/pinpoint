package com.navercorp.pinpoint.realtime.serde;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClusterTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserialize() throws IOException {

        Map<String, Object> clusterKeyMap = Map.of(
                "serviceName", "service1",
                "applicationName", "app1",
                "agentId", "agentId1",
                "startTimestamp", 1234L
        );

        String json = mapper.writeValueAsString(clusterKeyMap);

        JsonFactory factory = mapper.getFactory();
        JsonParser parser = factory.createParser(json);
        parser.nextToken();

        ClusterKeyDeserializer deserializer = new ClusterKeyDeserializer();
        ClusterKey clusterKey = deserializer.deserialize(parser, null);

        assertNotNull(clusterKey);
        assertEquals("service1", clusterKey.getServiceName());
        assertEquals("app1", clusterKey.getApplicationName());
        assertEquals("agentId1", clusterKey.getAgentId());
        assertEquals(1234, clusterKey.getStartTimestamp());
    }
}