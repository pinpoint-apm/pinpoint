package com.navercorp.pinpoint.web.response;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CodeResultTest {

    private final ObjectMapper mapper = Jackson.newMapper();

    @Test
    public void getMessage_NON_NULL() throws JacksonException {
        CodeResult<?> result = new CodeResult(1);
        checkOutput(result);
    }

    @Test
    public void getMessage_static() throws JacksonException{
        CodeResult<?> result = CodeResult.ok("test message");
        checkOutput(result);
    }

    private void checkOutput(CodeResult<?> codeResult) throws JacksonException {
        String jsonString = mapper.writeValueAsString(codeResult);
        JsonNode jsonNode = mapper.readTree(jsonString);
        Assertions.assertNotNull(jsonNode.get("code"));
        Assertions.assertNull(jsonNode.get("result"));
    }
}