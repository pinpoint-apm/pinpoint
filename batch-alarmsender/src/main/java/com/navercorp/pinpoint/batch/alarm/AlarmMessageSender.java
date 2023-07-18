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

package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmCheckerInterface;
import com.navercorp.pinpoint.batch.alarm.checker.PinotAlarmCheckerInterface;

/**
 * @author minwoo.jung
 */
public interface AlarmMessageSender {
    void sendSms(AlarmCheckerInterface checker, int sequenceCount);
    void sendEmail(AlarmCheckerInterface checker, int sequenceCount);
    void sendWebhook(AlarmCheckerInterface checker, int sequenceCount);
    void sendSms(PinotAlarmCheckerInterface checker, int sequenceCount);
    void sendEmail(PinotAlarmCheckerInterface checker, int sequenceCount);
    void sendWebhook(PinotAlarmCheckerInterface checker, int sequenceCount);
}
