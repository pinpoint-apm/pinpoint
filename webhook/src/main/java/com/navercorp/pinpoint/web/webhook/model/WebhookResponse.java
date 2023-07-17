package com.navercorp.pinpoint.web.webhook.model;


import com.navercorp.pinpoint.common.server.response.SuccessResponse;

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
