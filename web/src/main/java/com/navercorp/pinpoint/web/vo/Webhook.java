package com.navercorp.pinpoint.web.vo;

public class Webhook {
    private String webhookId;
    private String alias;
    private String url;
    private String applicationId;
    private String serviceName;

    public Webhook() {}

    public Webhook(String webhookId, String alias, String url, String applicationId, String serviceName) {
        this.webhookId = webhookId;
        this.alias = alias;
        this.url = url;
        this.applicationId = applicationId;
        this.serviceName = serviceName;
    }

    public String getWebhookId() {
        return webhookId;
    }

    public void setWebhookId(String webhookId) {
        this.webhookId = webhookId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Webhook{");
        sb.append("webhookId='").append(webhookId).append('\'');
        sb.append("alias='").append(alias).append('\'');
        sb.append("url='").append(url).append('\'');
        sb.append("applicationId='").append(applicationId).append('\'');
        sb.append("serviceName='").append(serviceName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
