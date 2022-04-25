package com.navercorp.pinpoint.web.response;

import java.util.Objects;

public class WebhookResponse extends SuccessResponse {
    private final String webhookId;
    public WebhookResponse(String result, String webhookId) {
        super(result);
        this.webhookId = Objects.requireNonNull(webhookId, "webhookId");
    }

    public String getWebhookId() {
        return webhookId;
    }
}
