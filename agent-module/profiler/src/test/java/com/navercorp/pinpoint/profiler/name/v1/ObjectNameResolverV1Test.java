package com.navercorp.pinpoint.profiler.name.v1;

import com.navercorp.pinpoint.profiler.name.AgentProperties;
import com.navercorp.pinpoint.profiler.name.IdSourceType;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.ObjectNameResolver;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ObjectNameResolverV1Test {

    @Test
    void resolve() {
        Map<String, String> map = new HashMap<>();
        map.put("pinpoint.agentId", "agentId");
        map.put("pinpoint.applicationName", "applicationName");
        map.put("pinpoint.serviceName", "serviceName");

        List<AgentProperties> list = new ArrayList<>();
        list.add(new AgentProperties(IdSourceType.SYSTEM, map::get));

        ObjectNameResolver resolver = new ObjectNameResolverV1(list);
        ObjectName objectName = resolver.resolve();

        assertEquals("agentId", objectName.getAgentId());
        assertEquals("agentId", objectName.getAgentName());
        assertEquals("applicationName", objectName.getApplicationName());
        assertNull(objectName.getServiceName());
    }

    @Test
    void resolve_agentName() {
        Map<String, String> map = new HashMap<>();
        map.put("pinpoint.agentName", "agentName");
        map.put("pinpoint.applicationName", "applicationName");
        map.put("pinpoint.serviceName", "serviceName");

        List<AgentProperties> list = new ArrayList<>();
        list.add(new AgentProperties(IdSourceType.SYSTEM, map::get));

        ObjectNameResolver resolver = new ObjectNameResolverV1(list);
        ObjectName objectName = resolver.resolve();

        assertNotNull(objectName.getAgentId());
        assertEquals("agentName", objectName.getAgentName());
        assertEquals("applicationName", objectName.getApplicationName());
        assertNull(objectName.getServiceName());
    }


}