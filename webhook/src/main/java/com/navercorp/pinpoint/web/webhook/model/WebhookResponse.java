package com.navercorp.pinpoint.web.webhook.model;


import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;

import java.util.Objects;

public class WebhookResponse extends SimpleResponse {
    private final String webhookId;

    public WebhookResponse(Result result, String webhookId) {
        super(result);
        this.webhookId = Objects.requireNonNull(webhookId, "webhookId");
    }

    public String getWebhookId() {
        return webhookId;
    }
}
