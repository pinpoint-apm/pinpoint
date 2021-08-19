package com.navercorp.pinpoint.web.dao.memory;

import com.navercorp.pinpoint.web.dao.WebhookDao;
import com.navercorp.pinpoint.web.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.vo.Webhook;
import com.navercorp.pinpoint.web.vo.WebhookSendInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryWebhookSendInfoDao implements WebhookSendInfoDao {
    private final Map<String, WebhookSendInfo> webhookSendInfos = new ConcurrentHashMap<>();
    private final AtomicInteger webhookSendInfoIdGenerator = new AtomicInteger();

    private final WebhookDao webhookDao;

    public MemoryWebhookSendInfoDao(WebhookDao webhookDao) {
        this.webhookDao = Objects.requireNonNull(webhookDao, "webhookDao");
    }

    @Override
    public String insertWebhookSendInfo(WebhookSendInfo webhookSendInfo) {
        String webhookSendInfoId = String.valueOf(webhookSendInfoIdGenerator.getAndIncrement());
        webhookSendInfo.setWebhookSendInfoId(webhookSendInfoId);
        webhookSendInfos.put(webhookSendInfoId, webhookSendInfo);
        return webhookSendInfoId;
    }

    @Override
    public void deleteWebhookSendInfo(WebhookSendInfo webhookSendInfo) {
        webhookSendInfos.remove(webhookSendInfo.getWebhookSendInfoId());
    }

    @Override
    public void deleteWebhookSendInfoByWebhookId(String webhookId) {
        for (WebhookSendInfo webhookSendInfo : webhookSendInfos.values()) {
            if (webhookId.equals(webhookSendInfo.getWebhookId())) {
                webhookSendInfos.remove(webhookSendInfo.getWebhookSendInfoId());
            }
        }
    }

    @Override
    public void deleteWebhookSendInfoByRuleId(String ruleId) {
        for (WebhookSendInfo webhookSendInfo : webhookSendInfos.values()) {
            if (ruleId.equals(webhookSendInfo.getRuleId())) {
                webhookSendInfos.remove(webhookSendInfo.getWebhookSendInfoId());
            }
        }
    }

    @Override
    public void updateWebhookSendInfo(WebhookSendInfo webhookSendInfo) {
        webhookSendInfos.put(webhookSendInfo.getWebhookSendInfoId(), webhookSendInfo);
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByApplicationId(String applicationId) {
        List<WebhookSendInfo> selectedWebhookSendInfos = new LinkedList<>();
        List<Webhook> webhooks = webhookDao.selectWebhookByApplicationId(applicationId);
        for (Webhook webhook : webhooks) {
            for (WebhookSendInfo webhookSendInfo : webhookSendInfos.values()) {
                if (webhook.getWebhookId().equals(webhookSendInfo.getWebhookId())) {
                    selectedWebhookSendInfos.add(webhookSendInfo);
                }
            }
        }
        return selectedWebhookSendInfos;
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByServiceName(String serviceName) {
        List<WebhookSendInfo> selectedWebhookSendInfos = new LinkedList<>();
        List<Webhook> webhooks = webhookDao.selectWebhookByServiceName(serviceName);
        for (Webhook webhook : webhooks) {
            for (WebhookSendInfo webhookSendInfo : webhookSendInfos.values()) {
                if (webhook.getWebhookId().equals(webhookSendInfo.getWebhookId())) {
                    selectedWebhookSendInfos.add(webhookSendInfo);
                }
            }
        }
        return selectedWebhookSendInfos;
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByWebhookId(String webhookId) {
        List<WebhookSendInfo> selectedWebhookSendInfos = new LinkedList<>();
        for (WebhookSendInfo webhookSendInfo : webhookSendInfos.values()) {
            if (webhookId.equals(webhookSendInfo.getWebhookId())) {
                selectedWebhookSendInfos.add(webhookSendInfo);
            }
        }
        return selectedWebhookSendInfos;
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByRuleId(String ruleId) {
        List<WebhookSendInfo> selectedWebhookSendInfos = new LinkedList<>();
        for (WebhookSendInfo webhookSendInfo : webhookSendInfos.values()) {
            if (ruleId.equals(webhookSendInfo.getRuleId())) {
                selectedWebhookSendInfos.add(webhookSendInfo);
            }
        }
        return selectedWebhookSendInfos;
    }

    @Override
    public WebhookSendInfo selectWebhookSendInfo(String webhookSendInfoId) {
        for (WebhookSendInfo webhookSendInfo : webhookSendInfos.values()) {
            if (webhookSendInfoId.equals(webhookSendInfo.getWebhookSendInfoId())) {
                return webhookSendInfo;
            }
        }
        return null;
    }
}
