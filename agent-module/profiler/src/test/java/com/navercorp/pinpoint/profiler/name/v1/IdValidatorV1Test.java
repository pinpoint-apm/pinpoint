package com.navercorp.pinpoint.profiler.name.v1;

import com.navercorp.pinpoint.profiler.name.AgentProperties;
import com.navercorp.pinpoint.profiler.name.IdSourceType;
import com.navercorp.pinpoint.profiler.name.IdValidator;
import com.navercorp.pinpoint.profiler.name.ObjectNameProperty;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class IdValidatorV1Test {

    @Test
    void validateAgentName_invalid() {

        Map<String, String> map = new HashMap<>();
        map.put("pinpoint.agentName", "===");

        AgentProperties properties = new AgentProperties(IdSourceType.SYSTEM, map::get);
        ObjectNameProperty agentName = properties.getAgentName();

        IdValidator validator = new IdValidatorV1();

        Assertions.assertFalse(validator.validateAgentName(agentName));
    }

    @Test
    void validateAgentName_255() {

        Map<String, String> map = new HashMap<>();
        map.put("pinpoint.agentName", StringUtils.repeat("a", 255));

        AgentProperties properties = new AgentProperties(IdSourceType.SYSTEM, map::get);
        ObjectNameProperty agentName = properties.getAgentName();

        IdValidator validator = new IdValidatorV1();

        Assertions.assertTrue(validator.validateAgentName(agentName));
    }

    @Test
    void validateAgentName_256() {

        Map<String, String> map = new HashMap<>();
        map.put("pinpoint.agentName", StringUtils.repeat("a", 256));

        AgentProperties properties = new AgentProperties(IdSourceType.SYSTEM, map::get);
        ObjectNameProperty agentName = properties.getAgentName();

        IdValidator validator = new IdValidatorV1();

        Assertions.assertFalse(validator.validateAgentName(agentName));
    }
}