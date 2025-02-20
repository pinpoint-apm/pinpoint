package com.navercorp.pinpoint.profiler.name.v4;

import com.navercorp.pinpoint.profiler.name.AgentProperties;
import com.navercorp.pinpoint.profiler.name.IdSourceType;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import com.navercorp.pinpoint.profiler.name.ObjectNameResolver;
import com.navercorp.pinpoint.profiler.name.ObjectNameValidationFailedException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ObjectNameResolverV4Test {

    @Test
    void resolve() {

        Map<String, String> map = new HashMap<>();
        map.put("pinpoint.agentName", "agentName");
        map.put("pinpoint.applicationName", "applicationName");
        map.put("pinpoint.serviceName", "serviceName");
        map.put("pinpoint.apikey", "api-test");


        List<AgentProperties> list = new ArrayList<>();
        list.add(new AgentProperties(IdSourceType.SYSTEM, map::get));

        ObjectNameResolver resolver = new ObjectNameResolverV4(list);
        ObjectName objectName = resolver.resolve();

        assertNotNull(objectName.getAgentId());
        assertEquals("agentName", objectName.getAgentName());
        assertEquals("applicationName", objectName.getApplicationName());
        assertEquals("serviceName", objectName.getServiceName());
        assertEquals("api-test", ((ObjectNameV4)objectName).getApiKey());

    }


    @Test
    void resolve_fail() {

        Map<String, String> map = new HashMap<>();
        map.put("pinpoint.agentName", "agentName");
        map.put("pinpoint.applicationName", "applicationName");
//        map.put("pinpoint.serviceName", "serviceName");

        List<AgentProperties> list = new ArrayList<>();
        list.add(new AgentProperties(IdSourceType.SYSTEM, map::get));

        ObjectNameResolverV4 resolver = new ObjectNameResolverV4(list);
        Assertions.assertThrows(ObjectNameValidationFailedException.class, resolver::resolve);
    }

    @Test
    void resolve_fail_limit() {

        Map<String, String> map = new HashMap<>();
        map.put("pinpoint.agentName", "agentName");
        map.put("pinpoint.applicationName", StringUtils.repeat("a", 256));
//        map.put("pinpoint.serviceName", "serviceName");

        List<AgentProperties> list = new ArrayList<>();
        list.add(new AgentProperties(IdSourceType.SYSTEM, map::get));

        ObjectNameResolverV4 resolver = new ObjectNameResolverV4(list);
        Assertions.assertThrows(ObjectNameValidationFailedException.class, resolver::resolve);
    }
}