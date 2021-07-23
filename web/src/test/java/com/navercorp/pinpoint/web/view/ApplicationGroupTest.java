package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ApplicationGroupTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        Assert.assertEquals("test1", ele1.get("applicationName"));
        Assert.assertEquals("TEST", ele1.get("serviceType"));
        Assert.assertEquals("5", ele1.get("code"));

    }
}
