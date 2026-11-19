package com.navercorp.pinpoint.web.response;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.StringNode;
import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleResponseTest {
    private final JsonMapper mapper = new JsonMapper();

    @Test
    public void testStaticOk() {
        Response result = SimpleResponse.ok();
        JsonNode jsonNode = checkOutput(result);
        Assertions.assertEquals(StringNode.valueOf(Result.SUCCESS.name()), jsonNode.get("result"));
        Assertions.assertNull(jsonNode.get("message"));
    }

    @Test
    public void testStaticOkWithMessage() {
        Response result = SimpleResponse.ok("Test Message");
        checkOutput(result);
        JsonNode jsonNode = checkOutput(result);
        Assertions.assertEquals(StringNode.valueOf(Result.SUCCESS.name()), jsonNode.get("result"));
        Assertions.assertEquals(StringNode.valueOf("Test Message"), jsonNode.get("message"));
    }

    private JsonNode checkOutput(Response codeResult) {
        String jsonString = mapper.writeValueAsString(codeResult);
        return mapper.readTree(jsonString);
    }
}
