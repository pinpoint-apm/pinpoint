/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.web.webhook.dao.memory;

import com.navercorp.pinpoint.web.webhook.dao.WebhookDao;
import com.navercorp.pinpoint.web.webhook.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.webhook.model.Webhook;
import com.navercorp.pinpoint.web.webhook.model.WebhookSendInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryWebhookSendInfoDao implements WebhookSendInfoDao {
    private final ConcurrentMap<String, WebhookSendInfo> webhookSendInfos = new ConcurrentHashMap<>();
    private final AtomicLong webhookIdGenerator = new AtomicLong();

    private final WebhookDao webhookDao;

    public MemoryWebhookSendInfoDao(WebhookDao webhookDao) {
        this.webhookDao = Objects.requireNonNull(webhookDao, "webhookDao");
    }

    private String nextWebhookId() {
        return String.valueOf(webhookIdGenerator.getAndIncrement());
    }

    @Override
    public String insertWebhookSendInfo(WebhookSendInfo webhookSendInfo) {
        String webhookSendInfoId = nextWebhookId();
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
        List<WebhookSendInfo> selectedWebhookSendInfos = new ArrayList<>();
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
        List<WebhookSendInfo> selectedWebhookSendInfos = new ArrayList<>();
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
        List<WebhookSendInfo> selectedWebhookSendInfos = new ArrayList<>();
        for (WebhookSendInfo webhookSendInfo : webhookSendInfos.values()) {
            if (webhookId.equals(webhookSendInfo.getWebhookId())) {
                selectedWebhookSendInfos.add(webhookSendInfo);
            }
        }
        return selectedWebhookSendInfos;
    }

    @Override
    public List<WebhookSendInfo> selectWebhookSendInfoByRuleId(String ruleId) {
        List<WebhookSendInfo> selectedWebhookSendInfos = new ArrayList<>();
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
