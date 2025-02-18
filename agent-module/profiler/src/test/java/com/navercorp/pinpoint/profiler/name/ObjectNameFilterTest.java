package com.navercorp.pinpoint.profiler.name;

import com.navercorp.pinpoint.profiler.name.v1.IdValidatorV1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ObjectNameFilterTest {

    IdValidator validator = new IdValidatorV1();

    @org.junit.jupiter.api.Test
    void testFilter() {

        Map<String, String> map = new HashMap<>();
        map.put("pinpoint.agentId", "agentId");

        List<AgentProperties> propertiesList = new ArrayList<>();
        propertiesList.add(new AgentProperties(IdSourceType.SYSTEM, map::get));


        ObjectNameFilter filter = new ObjectNameFilter(propertiesList);

        ObjectNameProperty agentIdProperty = filter.resolve(AgentProperties::getAgentId, validator::validateAgentId);
        assertEquals("agentId", agentIdProperty.getValue());

        ObjectNameProperty agentNameProperty = filter.resolve(AgentProperties::getAgentName, validator::validateAgentName);
        assertNull(agentNameProperty.getValue());
    }

}