package com.navercorp.pinpoint.web.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleResponseTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testStaticOk() throws JsonProcessingException {
        Response result = SimpleResponse.ok();
        JsonNode jsonNode = checkOutput(result);
        Assertions.assertEquals(TextNode.valueOf(Result.SUCCESS.name()), jsonNode.get("result"));
        Assertions.assertNull(jsonNode.get("message"));
    }

    @Test
    public void testStaticOkWithMessage() throws JsonProcessingException {
        Response result = SimpleResponse.ok("Test Message");
        checkOutput(result);
        JsonNode jsonNode = checkOutput(result);
        Assertions.assertEquals(TextNode.valueOf(Result.SUCCESS.name()), jsonNode.get("result"));
        Assertions.assertEquals(TextNode.valueOf("Test Message"), jsonNode.get("message"));
    }

    private JsonNode checkOutput(Response codeResult) throws JsonProcessingException {
        String jsonString = mapper.writeValueAsString(codeResult);
        return mapper.readTree(jsonString);
    }
}
