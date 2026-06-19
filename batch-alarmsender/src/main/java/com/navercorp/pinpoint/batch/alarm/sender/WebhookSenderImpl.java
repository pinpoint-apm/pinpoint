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
package com.navercorp.pinpoint.batch.alarm.sender;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmCheckerInterface;
import com.navercorp.pinpoint.batch.alarm.checker.PinotAlarmCheckerInterface;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.PinotAlarmWebhookPayload;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.UserGroup;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.UserMember;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.WebhookPayload;
import com.navercorp.pinpoint.user.service.UserService;
import com.navercorp.pinpoint.user.vo.User;
import com.navercorp.pinpoint.web.webhook.model.Webhook;
import com.navercorp.pinpoint.web.webhook.service.WebhookService;
import com.navercorp.pinpoint.web.webhook.support.WebhookUrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    public void sendWebhook(AlarmCheckerInterface checker, int sequenceCount) {
        String userGroupId = checker.getUserGroupId();

        List<UserMember> userMembers = userService.selectUserByUserGroupId(userGroupId)
            .stream()
            .map(WebhookSenderImpl::newUser)
            .collect(Collectors.toList());

        UserGroup userGroup = new UserGroup(userGroupId, userMembers);

        WebhookPayload webhookPayload = webhookPayloadFactory.newPayload(checker, sequenceCount, userGroup);

        List<Webhook> webhookSendInfoList = webhookService.selectWebhookByRuleId(checker.getRuleId());

        sendWebhooks(webhookPayload, webhookSendInfoList, checker);
        logger.info("Finished sending webhooks for checker : {}", checker);
    }

    @Override
    public void sendWebhook(PinotAlarmCheckerInterface checker, int index) {
        String userGroupId = checker.getUserGroupId(index);

        List<UserMember> userMembers = userService.selectUserByUserGroupId(userGroupId)
                .stream()
                .map(WebhookSenderImpl::newUser)
                .collect(Collectors.toList());

        UserGroup userGroup = new UserGroup(userGroupId, userMembers);

        PinotAlarmWebhookPayload webhookPayload = webhookPayloadFactory.newPayload(checker, index, userGroup);

        List<Webhook> webhookSendInfoList = webhookService.selectWebhookByPinotAlarmRuleId(checker.getRuleId(index));

        sendWebhooks(webhookPayload, webhookSendInfoList, checker);
        logger.info("Finished sending webhooks for checker : {}", checker);
    }

    private <P, C> void sendWebhooks(P webhookPayload, List<Webhook> webhookSendInfoList, C checker) {
        HttpEntity<P> httpEntity = new HttpEntity<>(webhookPayload, httpHeaders());

        for (Webhook webhook : webhookSendInfoList) {
            try {
                String validatedUrl = WebhookUrlValidator.validateSyntax(webhook.getUrl());
                restTemplate.exchange(validatedUrl, HttpMethod.POST, httpEntity, String.class);
                logger.info("Successfully sent webhook : {}", webhook);
            } catch (IllegalArgumentException e) {
                logger.warn("Webhook url is not valid. Failed Webhook : {} for Checker : {}", webhook, checker, e);
            } catch (RestClientException e) {
                logger.warn("Failed at sending webhook. Failed Webhook : {} for Checker : {}", webhook, checker, e);
            }
        }
    }

    private static HttpHeaders httpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }

    private static UserMember newUser(User user) {
        return new UserMember(user.getUserId(), user.getName(), user.getEmail(), user.getDepartment(), user.getPhoneNumber(), user.getPhoneCountryCode());
    }

}
