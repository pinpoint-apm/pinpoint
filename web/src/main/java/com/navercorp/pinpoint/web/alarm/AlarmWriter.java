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

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;

/**
 * @author minwoo.jung
 */
public class AlarmWriter implements ItemWriter<AlarmChecker> {
    
    @Autowired(required=false)
    private AlarmMessageSender alarmMessageSender = new EmptyMessageSender();
    
    @Override
    public void write(List<? extends AlarmChecker> checkers) throws Exception {
        for(AlarmChecker checker : checkers) {
            send(checker);
        }
    }
    
    private void send(AlarmChecker checker) {
        if (!checker.isDetected()) {
            return;
        }
        if (checker.isSMSSend()) {
            alarmMessageSender.sendSms(checker);
        }
        if (checker.isEmailSend()) {
            alarmMessageSender.sendEmail(checker);
        }
    }
}
