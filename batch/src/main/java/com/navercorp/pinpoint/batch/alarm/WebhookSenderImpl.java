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
package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.UserGroup;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.UserMember;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.WebhookPayload;
import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.batch.common.BatchConfiguration;
import com.navercorp.pinpoint.web.service.UserService;
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

/**
 * @author hyeran.lee
 */
public class WebhookSenderImpl implements WebhookSender {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BatchConfiguration batchConfiguration;
    private final UserService userService;
    private final RestTemplate springRestTemplate;
    private final String webhookReceiverUrl;
    private final boolean webhookEnable;
    
    public WebhookSenderImpl(BatchConfiguration batchConfiguration, UserService userService, RestTemplate springRestTemplate) {
        Objects.requireNonNull(batchConfiguration, "batchConfiguration");
        Objects.requireNonNull(springRestTemplate, "springRestTemplate");
        Objects.requireNonNull(userService, "userService");
        
        this.userService = userService;
        this.batchConfiguration = batchConfiguration;
        this.webhookReceiverUrl = batchConfiguration.getWebhookReceiverUrl();
        this.webhookEnable = batchConfiguration.isWebhookEnable();
        this.springRestTemplate = springRestTemplate;
    }
    
    @Override
    public void sendWebhook(AlarmChecker checker, int sequenceCount, StepExecution stepExecution) {
        if (!webhookEnable) {
            return;
        }
        if (webhookReceiverUrl.isEmpty()) {
            return;
        }
        try {
            String userGroupId = checker.getRule().getUserGroupId();
            
            List<UserMember> userMembers = userService.selectUserByUserGroupId(userGroupId)
                    .stream()
                    .map(user -> new UserMember(user.getUserId(), user.getName(), user.getEmail(), user.getDepartment(), user.getPhoneNumber(), user.getPhoneCountryCode()))
                    .collect(Collectors.toList());

            UserGroup userGroup = new UserGroup(userGroupId, userMembers);
            
            WebhookPayload webhookPayload = new WebhookPayload(checker, batchConfiguration, sequenceCount, userGroup);
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
