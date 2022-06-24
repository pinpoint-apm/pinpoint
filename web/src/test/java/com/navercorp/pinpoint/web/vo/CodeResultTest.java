package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.response.CodeResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CodeResultTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void getMessage_NON_NULL() throws JsonProcessingException {
        CodeResult result = new CodeResult(1);

        String jsonString = mapper.writeValueAsString(result);
        JsonNode jsonNode = mapper.readTree(jsonString);

        Assertions.assertNotNull(jsonNode.get("code"));
        Assertions.assertNull(jsonNode.get("result"));
    }
}