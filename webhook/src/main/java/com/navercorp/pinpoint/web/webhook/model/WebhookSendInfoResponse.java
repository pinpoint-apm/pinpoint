package com.navercorp.pinpoint.web.webhook.model;


import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;

import java.util.Objects;

public class WebhookSendInfoResponse extends SimpleResponse {
    private final String webhookSendInfoId;

    public WebhookSendInfoResponse(Result result, String webhookSendInfoId) {
        super(result);
        this.webhookSendInfoId = Objects.requireNonNull(webhookSendInfoId, "webhookSendInfoId");
    }

    public String getWebhookSendInfoId() {
        return webhookSendInfoId;
    }
}
