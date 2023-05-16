package com.navercorp.pinpoint.web.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AlarmResponseTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testCreatedMessage() throws JsonProcessingException {
        AlarmResponse result = new AlarmResponse("SUCCESS", "12345");
        checkOutput(result);
    }

    private void checkOutput(AlarmResponse codeResult) throws JsonProcessingException {
        String jsonString = mapper.writeValueAsString(codeResult);
        JsonNode jsonNode = mapper.readTree(jsonString);
        Assertions.assertEquals(TextNode.valueOf("SUCCESS"), jsonNode.get("result"));
        Assertions.assertNull(jsonNode.get("message"));
        Assertions.assertEquals(TextNode.valueOf("12345"), jsonNode.get("ruleId"));
    }
}
