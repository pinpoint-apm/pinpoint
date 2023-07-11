package com.navercorp.pinpoint.web.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CodeResultTest {

    private final ObjectMapper mapper = Jackson.newMapper();

    @Test
    public void getMessage_NON_NULL() throws JsonProcessingException {
        CodeResult<?> result = new CodeResult(1);
        checkOutput(result);
    }

    @Test
    public void getMessage_static() throws JsonProcessingException{
        CodeResult<?> result = CodeResult.ok("test message");
        checkOutput(result);
    }

    private void checkOutput(CodeResult<?> codeResult) throws JsonProcessingException {
        String jsonString = mapper.writeValueAsString(codeResult);
        JsonNode jsonNode = mapper.readTree(jsonString);
        Assertions.assertNotNull(jsonNode.get("code"));
        Assertions.assertNull(jsonNode.get("result"));
    }
}