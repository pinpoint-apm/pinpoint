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
import com.navercorp.pinpoint.web.webhook.model.Webhook;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MemoryWebhookDao implements WebhookDao {
    private final ConcurrentMap<String, Webhook> webhooks = new ConcurrentHashMap<>();
    private final AtomicLong webhookIdGenerator = new AtomicLong();

    public MemoryWebhookDao() {
    }

    private String nextWebhookId() {
        return String.valueOf(webhookIdGenerator.getAndIncrement());
    }

    @Override
    public String insertWebhook(Webhook webhook) {
        String webhookId = nextWebhookId();
        webhook.setWebhookId(webhookId);
        webhooks.put(webhookId, webhook);
        return webhookId;
    }

    @Override
    public void deleteWebhook(Webhook webhook) {
        webhooks.remove(webhook.getWebhookId());
    }

    @Override
    public void updateWebhook(Webhook webhook) {
        webhooks.put(webhook.getWebhookId(), webhook);
    }

    @Override
    public void deleteWebhookByApplicationId(String applicationId) {
        for (Webhook webhook : webhooks.values()) {
            if (applicationId.equals(webhook.getApplicationId())) {
                webhooks.remove(webhook.getWebhookId());
            }
        }
    }

    @Override
    public void deleteWebhookByServiceName(String serviceName) {
        for (Webhook webhook : webhooks.values()) {
            if (serviceName.equals(webhook.getServiceName())) {
                webhooks.remove(webhook.getWebhookId());
            }
        }
    }

    @Override
    public List<Webhook> selectWebhookByApplicationId(String applicationId) {
        List<Webhook> selectedWebhooks = new ArrayList<>();
        for (Webhook webhook : webhooks.values()) {
            if (applicationId.equals(webhook.getApplicationId())) {
                selectedWebhooks.add(webhook);
            }
        }
        return selectedWebhooks;
    }

    @Override
    public List<Webhook> selectWebhookByServiceName(String serviceName) {
        List<Webhook> selectedWebhooks = new ArrayList<>();
        for (Webhook webhook : webhooks.values()) {
            if (serviceName.equals(webhook.getServiceName())) {
                selectedWebhooks.add(webhook);
            }
        }
        return selectedWebhooks;
    }

    @Override
    public List<Webhook> selectWebhookByRuleId(String ruleId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Webhook> selectWebhookByPinotAlarmRuleId(String ruleId) {
        return null;
    }

    @Override
    public Webhook selectWebhook(String webhookId) {
        for (Webhook webhook : webhooks.values()) {
            if (webhook.getWebhookId().equals(webhookId)) {
                return webhook;
            }
        }
        return null;
    }
}
