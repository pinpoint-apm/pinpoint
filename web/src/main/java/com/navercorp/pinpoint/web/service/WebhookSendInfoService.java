package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.vo.WebhookSendInfo;

import java.util.List;

public interface WebhookSendInfoService {

    String insertWebhookSendInfo(WebhookSendInfo webhookSendInfo);

    void deleteWebhookSendInfo(WebhookSendInfo webhookSendInfo);

    void updateWebhookSendInfo(WebhookSendInfo webhookSendInfo);

    void deleteWebhookSendInfoByWebhookId(String webhookId);

    void deleteWebhookSendInfoByRuleId(String ruleId);

    List<WebhookSendInfo> selectWebhookSendInfoByApplicationId(String applicationId);

    List<WebhookSendInfo> selectWebhookSendInfoByServiceName(String serviceName);

    List<WebhookSendInfo> selectWebhookSendInfoByRuleId(String ruleId);

    List<WebhookSendInfo> selectWebhookSendInfoByWebhookId(String webhookId);

}
