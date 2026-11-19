package com.navercorp.pinpoint.web.response;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.StringNode;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.web.webhook.model.WebhookResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebhookResponseTest {
    private final JsonMapper mapper = Jackson.newMapper();

    @Test
    public void testCreatedMessage() {
        WebhookResponse result = new WebhookResponse(Result.SUCCESS, "12345");
        checkOutput(result);
    }

    private void checkOutput(WebhookResponse codeResult) {
        String jsonString = mapper.writeValueAsString(codeResult);
        JsonNode jsonNode = mapper.readTree(jsonString);
        Assertions.assertEquals(StringNode.valueOf(Result.SUCCESS.name()), jsonNode.get("result"));
        Assertions.assertNull(jsonNode.get("message"));
        Assertions.assertEquals(StringNode.valueOf("12345"), jsonNode.get("webhookId"));
    }


}
