package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.web.vo.WebhookSendInfo;

import java.util.List;

public interface WebhookSendInfoDao {

    String insertWebhookSendInfo(WebhookSendInfo webhookSendInfo);

    void deleteWebhookSendInfo(WebhookSendInfo webhookSendInfo);

    void deleteWebhookSendInfoByWebhookId(String webhookId);

    void deleteWebhookSendInfoByRuleId(String ruleId);

    void updateWebhookSendInfo(WebhookSendInfo webhookSendInfo);

    List<WebhookSendInfo> selectWebhookSendInfoByApplicationId(String applicationId);

    List<WebhookSendInfo> selectWebhookSendInfoByServiceName(String serviceName);

    List<WebhookSendInfo> selectWebhookSendInfoByWebhookId(String webhookId);

    List<WebhookSendInfo> selectWebhookSendInfoByRuleId(String ruleId);

    WebhookSendInfo selectWebhookSendInfo(String webhookSendInfoId);

}
