package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ApplicationGroupTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getApplicationList() throws JsonProcessingException {
        Application app1 = new Application("test1", ServiceType.TEST);
        Application app2 = new Application("test2", ServiceType.TEST);
        ApplicationGroup group = new ApplicationGroup(Arrays.asList(app1, app2));
        String json = mapper.writeValueAsString(group);

        TypeReference<List<Map<String, String>>> ref = new TypeReference<List<Map<String, String>>>() {};
        List<Map<String, String>> list = mapper.convertValue(group, ref);

        logger.debug("json:{}", json);

        Map<String, String> ele1 = list.get(0);
        Assertions.assertEquals("test1", ele1.get("applicationName"));
        Assertions.assertEquals("TEST", ele1.get("serviceType"));
        Assertions.assertEquals("5", ele1.get("code"));

    }
}
