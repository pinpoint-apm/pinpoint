package com.navercorp.pinpoint.web.webhook.model;

import java.util.Objects;

public class Webhook {
    private String webhookId;
    private String alias;
    private String url;
    private String applicationName;
    private String serviceName;

    public Webhook() {}

    public Webhook(String webhookId, String alias, String url, String applicationName, String serviceName) {
        this.webhookId = webhookId;
        this.alias = alias;
        this.url = url;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
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

    /**
     * @deprecated Since 3.1.0. Use {@link #getApplicationName()} instead.
     */
    @Deprecated
    public String getApplicationId() {
        return getApplicationName();
    }

    /**
     * @deprecated Since 3.1.0. Use {@link #setApplicationName(String)} instead.
     */
    @Deprecated
    public void setApplicationId(String applicationName) {
        this.setApplicationName(applicationName);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "Webhook{" +
                "webhookId='" + webhookId + '\'' +
                "alias='" + alias + '\'' +
                "url='" + url + '\'' +
                "applicationName='" + applicationName + '\'' +
                "serviceName='" + serviceName + '\'' +
                '}';
    }
}
