package com.navercorp.pinpoint.web.response;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.StringNode;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateUserGroupResponseTest {
    private final ObjectMapper mapper = Jackson.newMapper();

    @Test
    public void testCreatedMessage() throws JacksonException {
        CreateUserGroupResponse result = new CreateUserGroupResponse(Result.SUCCESS, "12345");
        checkOutput(result);
    }

    private void checkOutput(CreateUserGroupResponse codeResult) throws JacksonException {
        String jsonString = mapper.writeValueAsString(codeResult);
        JsonNode jsonNode = mapper.readTree(jsonString);
        Assertions.assertEquals(StringNode.valueOf(Result.SUCCESS.name()), jsonNode.get("result"));
        Assertions.assertNull(jsonNode.get("message"));
        Assertions.assertEquals(StringNode.valueOf("12345"), jsonNode.get("number"));
    }
}
