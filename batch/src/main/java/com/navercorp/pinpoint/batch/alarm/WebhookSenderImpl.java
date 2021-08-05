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
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.vo.User;
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
    private final UserService userService;
    private final RestTemplate restTemplate;

    private final WebhookPayloadFactory webhookPayloadFactory;
    private final String webhookReceiverUrl;


    public WebhookSenderImpl(WebhookPayloadFactory webhookPayloadFactory,
                             UserService userService,
                             RestTemplate restTemplate,
                             String webhookReceiverUrl) {
        this.webhookPayloadFactory = Objects.requireNonNull(webhookPayloadFactory, "webhookPayloadFactory");

        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate");
        this.userService = Objects.requireNonNull(userService, "userService");
        // TODO Target address must be changed as a user configuration.
        this.webhookReceiverUrl = webhookReceiverUrl;
    }


    @Override
    public void sendWebhook(AlarmChecker<?> checker, int sequenceCount, StepExecution stepExecution) {
        try {
            String userGroupId = checker.getRule().getUserGroupId();
            
            List<UserMember> userMembers = userService.selectUserByUserGroupId(userGroupId)
                    .stream()
                    .map(WebhookSenderImpl::newUser)
                    .collect(Collectors.toList());

            UserGroup userGroup = new UserGroup(userGroupId, userMembers);

            WebhookPayload webhookPayload = webhookPayloadFactory.newPayload(checker, sequenceCount, userGroup);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<WebhookPayload> httpEntity = new HttpEntity<>(webhookPayload, httpHeaders);
            restTemplate.exchange(new URI(webhookReceiverUrl), HttpMethod.POST, httpEntity, String.class);
            logger.info("send webhook : {}", checker.getRule());
        } catch (Exception e) {
            logger.error("can't send webhook. {}", checker.getRule(), e);
        }
    }

    private static UserMember newUser(User user) {
        return new UserMember(user.getUserId(), user.getName(), user.getEmail(), user.getDepartment(), user.getPhoneNumber(), user.getPhoneCountryCode());
    }
}
