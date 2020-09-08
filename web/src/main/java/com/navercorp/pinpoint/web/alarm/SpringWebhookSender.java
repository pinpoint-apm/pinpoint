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

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.vo.UserGroupMemberPayload;
import com.navercorp.pinpoint.web.alarm.vo.UserMember;
import com.navercorp.pinpoint.web.alarm.vo.WebhookPayload;
import com.navercorp.pinpoint.web.batch.BatchConfiguration;
import com.navercorp.pinpoint.web.service.UserGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpringWebhookSender implements WebhookSender {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BatchConfiguration batchConfiguration;
    private final UserGroupService userGroupService;
    private final RestTemplate springRestTemplate;
    private final String webhookReceiverUrl;
    private final boolean webhookEnable;
    
    public SpringWebhookSender(BatchConfiguration batchConfiguration, UserGroupService userGroupService, RestTemplate springRestTemplate) {
        Objects.requireNonNull(batchConfiguration, "batchConfiguration");
        Objects.requireNonNull(springRestTemplate, "springRestTemplate");
        Objects.requireNonNull(userGroupService, "userGroupService");
        
        this.userGroupService = userGroupService;
        this.batchConfiguration = batchConfiguration;
        this.webhookReceiverUrl = batchConfiguration.getWebhookReceiverUrl();
        this.webhookEnable = batchConfiguration.isWebhookEnable();
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
            List<UserMember> userMembers = userGroupService.selectMember(checker.getRule().getUserGroupId())
                    .stream()
                    .map((UserMember::new))
                    .collect(Collectors.toList());
            UserGroupMemberPayload userGroupMemberPayload = new UserGroupMemberPayload(checker.getRule().getUserGroupId(), userMembers);
            
            WebhookPayload webhookPayload = new WebhookPayload(checker, batchConfiguration, sequenceCount, userGroupMemberPayload);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<WebhookPayload> httpEntity = new HttpEntity<>(webhookPayload, httpHeaders);
            springRestTemplate.exchange(new URI(webhookReceiverUrl), HttpMethod.POST, httpEntity, String.class);
            logger.info("send webhook : {}", checker.getRule());
        } catch (Exception e) {
            logger.error("can't send webhook. {}", checker.getRule(), e);
        }
    }
}
