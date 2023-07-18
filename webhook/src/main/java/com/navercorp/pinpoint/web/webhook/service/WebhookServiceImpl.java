package com.navercorp.pinpoint.web.webhook.service;

import com.navercorp.pinpoint.web.webhook.dao.WebhookDao;
import com.navercorp.pinpoint.web.webhook.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.webhook.model.Webhook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = {Exception.class})
public class WebhookServiceImpl implements WebhookService {
    private final WebhookDao webhookDao;
    private final WebhookSendInfoDao webhookSendInfoDao;

    public WebhookServiceImpl(WebhookDao webhookDao, WebhookSendInfoDao webhookSendInfoDao) {
        this.webhookDao = Objects.requireNonNull(webhookDao, "webhookDao");
        this.webhookSendInfoDao = Objects.requireNonNull(webhookSendInfoDao, "webhookSendInfoDao");
    }

    @Override
    public String insertWebhook(Webhook webhook) {
        return webhookDao.insertWebhook(webhook);
    }

    @Override
    public void deleteWebhook(Webhook webhook) {
        webhookDao.deleteWebhook(webhook);
        webhookSendInfoDao.deleteWebhookSendInfoByWebhookId(webhook.getWebhookId());
    }

    @Override
    public void updateWebhook(Webhook webhook) {
        webhookDao.updateWebhook(webhook);
    }

    @Override
    public void deleteWebhookByApplicationId(String applicationId) {
        webhookDao.deleteWebhookByApplicationId(applicationId);
    }

    @Override
    public void deleteWebhookByServiceName(String serviceName) {
        webhookDao.deleteWebhookByServiceName(serviceName);
    }

    @Override
    public List<Webhook> selectWebhookByApplicationId(String applicationId) {
        return webhookDao.selectWebhookByApplicationId(applicationId);
    }

    @Override
    public List<Webhook> selectWebhookByServiceName(String serviceName) {
        return webhookDao.selectWebhookByServiceName(serviceName);
    }

    @Override
    public List<Webhook> selectWebhookByRuleId(String ruleId) {
        return webhookDao.selectWebhookByRuleId(ruleId);
    }

    @Override
    public List<Webhook> selectWebhookByPinotAlarmRuleId(String ruleId) {
        return webhookDao.selectWebhookByPinotAlarmRuleId(ruleId);
    }
}
