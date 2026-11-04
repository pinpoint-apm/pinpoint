package com.navercorp.pinpoint.web.response;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.StringNode;
import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleResponseTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testStaticOk() throws JacksonException {
        Response result = SimpleResponse.ok();
        JsonNode jsonNode = checkOutput(result);
        Assertions.assertEquals(StringNode.valueOf(Result.SUCCESS.name()), jsonNode.get("result"));
        Assertions.assertNull(jsonNode.get("message"));
    }

    @Test
    public void testStaticOkWithMessage() throws JacksonException {
        Response result = SimpleResponse.ok("Test Message");
        checkOutput(result);
        JsonNode jsonNode = checkOutput(result);
        Assertions.assertEquals(StringNode.valueOf(Result.SUCCESS.name()), jsonNode.get("result"));
        Assertions.assertEquals(StringNode.valueOf("Test Message"), jsonNode.get("message"));
    }

    private JsonNode checkOutput(Response codeResult) throws JacksonException {
        String jsonString = mapper.writeValueAsString(codeResult);
        return mapper.readTree(jsonString);
    }
}
