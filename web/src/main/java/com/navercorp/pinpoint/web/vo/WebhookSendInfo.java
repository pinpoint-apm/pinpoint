package com.navercorp.pinpoint.web.vo;

public class WebhookSendInfo {
    private String webhookSendInfoId;
    private String webhookId;
    private String ruleId;

    public WebhookSendInfo() {}

    public WebhookSendInfo(String webhookSendInfoId, String webhookId, String ruleId) {
        this.webhookSendInfoId = webhookSendInfoId;
        this.webhookId = webhookId;
        this.ruleId = ruleId;
    }

    public String getWebhookSendInfoId() {
        return webhookSendInfoId;
    }

    public void setWebhookSendInfoId(String webhookSendInfoId) {
        this.webhookSendInfoId = webhookSendInfoId;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WebhookSendInfo{");
        sb.append("webhookSendInfoId='").append(webhookSendInfoId).append('\'');
        sb.append("webhookId='").append(webhookId).append('\'');
        sb.append("ruleId='").append(ruleId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
