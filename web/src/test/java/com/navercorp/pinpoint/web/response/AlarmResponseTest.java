package com.navercorp.pinpoint.web.response;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.StringNode;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AlarmResponseTest {
    private final JsonMapper mapper = Jackson.newMapper();

    @Test
    public void testCreatedMessage() {
        AlarmResponse result = new AlarmResponse(Result.SUCCESS, "12345");
        checkOutput(result);
    }

    private void checkOutput(AlarmResponse codeResult) {
        String jsonString = mapper.writeValueAsString(codeResult);
        JsonNode jsonNode = mapper.readTree(jsonString);
        Assertions.assertEquals(StringNode.valueOf(Result.SUCCESS.name()), jsonNode.get("result"));
        Assertions.assertNull(jsonNode.get("message"));
        Assertions.assertEquals(StringNode.valueOf("12345"), jsonNode.get("ruleId"));
    }
}
