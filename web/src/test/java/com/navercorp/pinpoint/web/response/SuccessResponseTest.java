package com.navercorp.pinpoint.web.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SuccessResponseTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testStaticOk() throws JsonProcessingException {
        Response result = SuccessResponse.ok();;
        JsonNode jsonNode = checkOutput(result);
        Assertions.assertEquals(TextNode.valueOf("SUCCESS"), jsonNode.get("result"));
        Assertions.assertNull(jsonNode.get("message"));
    }

    @Test
    public void testStaticOkWithMessage() throws JsonProcessingException {
        Response result = SuccessResponse.ok("Test Message");;
        checkOutput(result);
        JsonNode jsonNode = checkOutput(result);
        Assertions.assertEquals(TextNode.valueOf("SUCCESS"), jsonNode.get("result"));
        Assertions.assertEquals(TextNode.valueOf("Test Message"), jsonNode.get("message"));
    }

    private JsonNode checkOutput(Response codeResult) throws JsonProcessingException {
        String jsonString = mapper.writeValueAsString(codeResult);
        return mapper.readTree(jsonString);
    }
}
