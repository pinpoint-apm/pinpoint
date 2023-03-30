package com.navercorp.pinpoint.web.webhook.model;

import com.navercorp.pinpoint.web.response.SuccessResponse;

import java.util.Objects;

public class WebhookSendInfoResponse extends SuccessResponse {
    private final String webhookSendInfoId;

    public WebhookSendInfoResponse(String result, String webhookSendInfoId) {
        super(result);
        this.webhookSendInfoId = Objects.requireNonNull(webhookSendInfoId, "webhookSendInfoId");
    }

    public String getWebhookSendInfoId() {
        return webhookSendInfoId;
    }
}
