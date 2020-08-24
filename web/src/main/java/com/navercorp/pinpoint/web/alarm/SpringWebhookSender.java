/*
 * Copyright 2020 NAVER Corp.
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
package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.web.alarm.checker.AgentChecker;
import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.vo.WebhookPayload;
import com.navercorp.pinpoint.web.batch.BatchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Objects;

public class SpringWebhookSender implements WebhookSender {
    
    private static final String CHECKER_TYPE = "CheckerType";
    private static final String AGENT_CHECKER = "AgentChecker";
    private static final String ALARM_CHECKER = "AlarmChecker";
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BatchConfiguration batchConfiguration;
    private final RestTemplate springRestTemplate;
    private final String webhookReceiverUrl;
    private final boolean webhookEnable;
    
    public SpringWebhookSender(BatchConfiguration batchConfiguration, RestTemplate springRestTemplate) {
        Objects.requireNonNull(batchConfiguration, "batchConfiguration");
        Objects.requireNonNull(springRestTemplate, "springRestTemplate");
        
        this.batchConfiguration = batchConfiguration;
        this.webhookReceiverUrl = batchConfiguration.getWebhookReceiverUrl();
        this.webhookEnable = batchConfiguration.getWebhookEnable();
        this.springRestTemplate = springRestTemplate;
    }
    
    @Override
    public void triggerWebhook(AlarmChecker checker, int sequenceCount, StepExecution stepExecution) {
        if (webhookReceiverUrl.isEmpty()) {
            return;
        }
        if (!webhookEnable) {
            return;
        }
        
        try {
            WebhookPayload webhookPayload = new WebhookPayload(checker, batchConfiguration, sequenceCount);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            if (checker instanceof AgentChecker) {
                httpHeaders.set(CHECKER_TYPE, AGENT_CHECKER);
            } else {
                httpHeaders.set(CHECKER_TYPE, ALARM_CHECKER);
            }
            
            HttpEntity<WebhookPayload> httpEntity = new HttpEntity<>(webhookPayload, httpHeaders);
            springRestTemplate.exchange(new URI(webhookReceiverUrl), HttpMethod.POST, httpEntity, String.class);
            logger.info("send webhook : {}", checker.getRule());
        } catch (Exception e) {
            logger.error("can't send webhook. {}", checker.getRule(), e);
        }
    }
}
