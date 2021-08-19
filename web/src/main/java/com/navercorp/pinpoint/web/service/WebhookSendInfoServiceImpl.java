package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.vo.WebhookSendInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = {Exception.class})
public class WebhookSendInfoServiceImpl implements WebhookSendInfoService{
    private final WebhookSendInfoDao webhookSendInfoDao;

    public WebhookSendInfoServiceImpl(WebhookSendInfoDao webhookSendInfoDao) {
        this.webhookSendInfoDao = Objects.requireNonNull(webhookSendInfoDao, "webhookSendInfoDao");
    }

    @Override
    public String insertWebhookSendInfo(WebhookSendInfo webhookSendInfo) {
        return webhookSendInfoDao.insertWebhookSendInfo(webhookSendInfo);
    }

    @Override
    public void deleteWebhookSendInfo(WebhookSendInfo webhookSendInfo) {
        webhookSendInfoDao.deleteWebhookSendInfo(webhookSendInfo);
    }

    @Override
    public void updateWebhookSendInfo(WebhookSendInfo webhookSendInfo) {
        webhookSendInfoDao.updateWebhookSendInfo(webhookSendInfo);
    }

    @Override
    public void deleteWebhookSendInfoByWebhookId(String webhookId) {
        webhookSendInfoDao.deleteWebhookSendInfoByWebhookId(webhookId);
    }

    @Override
    public void deleteWebhookSendInfoByRuleId(String ruleId) {
        webhookSendInfoDao.deleteWebhookSendInfoByRuleId(ruleId);
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByApplicationId(String applicationId) {
        return webhookSendInfoDao.selectWebhookSendInfoByApplicationId(applicationId);
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByServiceName(String serviceName) {
        return webhookSendInfoDao.selectWebhookSendInfoByServiceName(serviceName);
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByRuleId(String ruleId) {
        return webhookSendInfoDao.selectWebhookSendInfoByRuleId(ruleId);
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByWebhookId(String webhookId) {
        return webhookSendInfoDao.selectWebhookSendInfoByWebhookId(webhookId);
    }
}
