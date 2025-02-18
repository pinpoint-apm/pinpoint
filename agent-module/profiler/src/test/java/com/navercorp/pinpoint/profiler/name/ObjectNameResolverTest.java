package com.navercorp.pinpoint.profiler.name;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ObjectNameResolverTest {

    @Test
    void resolve() {

        Map<String, String> map = new HashMap<>();
        map.put("pinpoint.agentId", "agentId-1");
        map.put("pinpoint.agentName", "agentName-1");
        map.put("pinpoint.applicationName", "applicationName-1");

        ObjectNameResolverBuilder builder = new ObjectNameResolverBuilder();
        builder.addProperties(IdSourceType.SYSTEM, map::get);
        ObjectNameResolver resolver = builder.buildV1();

        ObjectName objectName = resolver.resolve();
        assertEquals("agentId-1", objectName.getAgentId());
        assertEquals("agentName-1", objectName.getAgentName());
        assertEquals("applicationName-1", objectName.getApplicationName());
        assertNull(objectName.getServiceName());
    }

}