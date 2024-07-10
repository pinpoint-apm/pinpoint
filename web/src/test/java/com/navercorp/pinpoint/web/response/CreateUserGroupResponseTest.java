package com.navercorp.pinpoint.web.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateUserGroupResponseTest {
    private final ObjectMapper mapper = Jackson.newMapper();

    @Test
    public void testCreatedMessage() throws JsonProcessingException {
        CreateUserGroupResponse result = new CreateUserGroupResponse(Result.SUCCESS, "12345");
        checkOutput(result);
    }

    private void checkOutput(CreateUserGroupResponse codeResult) throws JsonProcessingException {
        String jsonString = mapper.writeValueAsString(codeResult);
        JsonNode jsonNode = mapper.readTree(jsonString);
        Assertions.assertEquals(TextNode.valueOf(Result.SUCCESS.name()), jsonNode.get("result"));
        Assertions.assertNull(jsonNode.get("message"));
        Assertions.assertEquals(TextNode.valueOf("12345"), jsonNode.get("number"));
    }
}
