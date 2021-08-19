/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.dao.memory;

import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.AlarmDao;
import com.navercorp.pinpoint.web.dao.WebhookDao;
import com.navercorp.pinpoint.web.vo.Webhook;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class MemoryWebhookDao implements WebhookDao {
    private final Map<String, Webhook> webhooks = new ConcurrentHashMap<>();
    private final AtomicInteger webhookIdGenerator = new AtomicInteger();

    private final AlarmDao alarmDao;

    public MemoryWebhookDao(AlarmDao alarmDao) {
        this.alarmDao = Objects.requireNonNull(alarmDao, "alarmDao");
    }

    @Override
    public String insertWebhook(Webhook webhook) {
        String webhookId = String.valueOf(webhookIdGenerator.getAndIncrement());
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
        List<Webhook> selectedWebhooks = new LinkedList<>();
        for (Webhook webhook : webhooks.values()) {
            if (applicationId.equals(webhook.getApplicationId())) {
                selectedWebhooks.add(webhook);
            }
        }
        return selectedWebhooks;
    }

    @Override
    public List<Webhook> selectWebhookByServiceName(String serviceName) {
        List<Webhook> selectedWebhooks = new LinkedList<>();
        for (Webhook webhook : webhooks.values()) {
            if (serviceName.equals(webhook.getServiceName())) {
                selectedWebhooks.add(webhook);
            }
        }
        return selectedWebhooks;
    }

    @Override
    public List<Webhook> selectWebhookByRuleId(String ruleId) {
        List<Webhook> selectedWebhooks = new LinkedList<>();
        for (Webhook webhook : webhooks.values()) {
            List<Rule> rules = alarmDao.selectRuleByApplicationId(webhook.getApplicationId());
            for (Rule rule : rules) {
                if (rule.getRuleId().equals(ruleId)) {
                    selectedWebhooks.add(webhook);
                }
            }
        }

        return selectedWebhooks;
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
