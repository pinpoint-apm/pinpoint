/*
 * Copyright 2018 NAVER Corp.
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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author minwoo.jung
 */
public class EmptySmsSender implements SmsSender {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void sendSms(AlarmCheckerInterface checker, int sequenceCount) {
        logger.info("can not send sms message.");
    }

    @Override
    public void sendSms(PinotAlarmCheckerInterface checker, int index) {
        logger.info("can not send sms message.");
    }
}
