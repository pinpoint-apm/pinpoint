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
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.service.WebhookService;
import com.navercorp.pinpoint.web.vo.User;
import com.navercorp.pinpoint.web.vo.Webhook;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.batch.core.StepExecution;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author hyeran.lee
 */
public class WebhookSenderImpl implements WebhookSender {
    
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final UserService userService;
    private final RestTemplate restTemplate;

    private final WebhookPayloadFactory webhookPayloadFactory;
    private final WebhookService webhookService;

    public WebhookSenderImpl(WebhookPayloadFactory webhookPayloadFactory,
                             UserService userService,
                             RestTemplate restTemplate,
                             WebhookService webhookService) {
        this.webhookPayloadFactory = Objects.requireNonNull(webhookPayloadFactory, "webhookPayloadFactory");

        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate");
        this.userService = Objects.requireNonNull(userService, "userService");
        this.webhookService = Objects.requireNonNull(webhookService, "webhookService");
    }


    @Override
    public void sendWebhook(AlarmChecker<?> checker, int sequenceCount, StepExecution stepExecution) {
        Rule rule = checker.getRule();
        String userGroupId = rule.getUserGroupId();
            
        List<UserMember> userMembers = userService.selectUserByUserGroupId(userGroupId)
            .stream()
            .map(WebhookSenderImpl::newUser)
            .collect(Collectors.toList());

        UserGroup userGroup = new UserGroup(userGroupId, userMembers);

        WebhookPayload webhookPayload = webhookPayloadFactory.newPayload(checker, sequenceCount, userGroup);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        List<Webhook> webhookSendInfoList = webhookService.selectWebhookByRuleId(rule.getRuleId());
        for (Webhook webhook : webhookSendInfoList) {
            try {
                HttpEntity<WebhookPayload> httpEntity = new HttpEntity<>(webhookPayload, httpHeaders);
                restTemplate.exchange(webhook.getUrl(), HttpMethod.POST, httpEntity, String.class);
                logger.info("Successfully sent webhook : {}", webhook);
            } catch (RestClientException e) {
                logger.warn("Failed at sending webhook. Failed Webhook : {} for Rule : {}", webhook, rule, e);
            }
        }
        logger.info("Finished sending webhooks for rule : {}", rule);

    }

    private static UserMember newUser(User user) {
        return new UserMember(user.getUserId(), user.getName(), user.getEmail(), user.getDepartment(), user.getPhoneNumber(), user.getPhoneCountryCode());
    }
}
