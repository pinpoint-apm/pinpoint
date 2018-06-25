/*
 * Copyright 2014 NAVER Corp.
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
import com.navercorp.pinpoint.web.service.UserGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author minwoo.jung
 */
public class AlarmMessageSenderImpl implements AlarmMessageSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserGroupService userGroupService;

    @Value("#{batchProps['mail.user']}")
    private String senderMailUser;

    @Value("#{batchProps['mail.pass']}")
    private String senderMailPass;

    @Value("#{batchProps['mail.smtp']}")
    private String senderMailSmtp;

    @Value("#{batchProps['mail.copy.user']}")
    private String mailCopyUser;

    @Override
    public void sendSms(AlarmChecker checker, int sequenceCount) {
        /**
         * 获取接受提醒的分组手机号码
         */
        List<String> receivers = userGroupService.selectPhoneNumberOfMember(checker.getuserGroupId());
        if (receivers.size() == 0) {
            return;
        }

//        for (String message : checker.getSmsMessage()) {
//            logger.info("send SMS : {}", message);
//            // TODO Implement logic for sending SMS
//        }
    }

    @Override
    public void sendEmail(AlarmChecker checker, int sequenceCount) {
        //获取接受者邮件
        List<String> receivers = userGroupService.selectEmailOfMember(checker.getuserGroupId());

        if (receivers.size() == 0) {
            return;
        }

        String message = checker.getEmailMessage();
        String emails = receivers.stream().collect(Collectors.joining(","));
        logger.info("send mail message {}, senders {}", message, emails);
        try {
            TransportEmail.entity(senderMailSmtp, senderMailUser, senderMailPass, emails, mailCopyUser, "服务告警通知", message, null)
                    .send();
        } catch (Exception e) {
            logger.error("send mail waring failure message = {}", e.getMessage());
        }
    }

}
