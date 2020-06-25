package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeResultTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void getMessage_NON_NULL() throws JsonProcessingException {
        CodeResult result = new CodeResult(1);

        String jsonString = mapper.writeValueAsString(result);
        JsonNode jsonNode = mapper.readTree(jsonString);

        Assert.assertNotNull(jsonNode.get("code"));
        Assert.assertNull(jsonNode.get("result"));
    }
}