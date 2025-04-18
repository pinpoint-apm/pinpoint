package com.navercorp.pinpoint.web.webhook.dao;

import com.navercorp.pinpoint.web.webhook.model.WebhookSendInfo;

import java.util.List;

public interface WebhookSendInfoDao {

    String insertWebhookSendInfo(WebhookSendInfo webhookSendInfo);

    void deleteWebhookSendInfo(WebhookSendInfo webhookSendInfo);

    void deleteWebhookSendInfoByWebhookId(String webhookId);

    void deleteWebhookSendInfoByRuleId(String ruleId);

    void updateWebhookSendInfo(WebhookSendInfo webhookSendInfo);

    List<WebhookSendInfo> selectWebhookSendInfoByApplicationName(String applicationName);

    List<WebhookSendInfo> selectWebhookSendInfoByServiceName(String serviceName);

    List<WebhookSendInfo> selectWebhookSendInfoByWebhookId(String webhookId);

    List<WebhookSendInfo> selectWebhookSendInfoByRuleId(String ruleId);

    WebhookSendInfo selectWebhookSendInfo(String webhookSendInfoId);

}
