package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.vo.Webhook;

import java.util.List;

public interface WebhookService {

    String insertWebhook(Webhook webhook);

    void deleteWebhook(Webhook webhook);

    void updateWebhook(Webhook webhook);

    void deleteWebhookByApplicationId(String applicationId);

    void deleteWebhookByServiceName(String serviceName);

    List<Webhook> selectWebhookByApplicationId(String applicationId);

    List<Webhook> selectWebhookByServiceName(String serviceName);

    List<Webhook> selectWebhookByRuleId(String ruleId);
}
