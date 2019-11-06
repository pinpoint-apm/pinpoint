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
import com.navercorp.pinpoint.web.alarm.vo.Rule;

/**
 * @author minwoo.jung
 */
public class AlarmMailTemplate {

    private static final String LINE_FEED = "<br>";
    private static final String LINK_FORMAT = "<a href=\"%s\" >%s</a>";

    private final String pinpointUrl;
    private final AlarmChecker checker;
    private final String batchEnv;
    private final int sequenceCount;

    public AlarmMailTemplate(AlarmChecker checker, String pinpointUrl, String batchEnv, int sequenceCount) {
        this.checker = checker;
        this.pinpointUrl =pinpointUrl;
        this.batchEnv = batchEnv;
        this.sequenceCount = sequenceCount;
    }
    
    public String createSubject() {
        Rule rule = checker.getRule();
        return String.format("[PINPOINT-" + batchEnv + "] %s Alarm for %s Service. #%d", rule.getCheckerName(), rule.getApplicationId(), sequenceCount);
    }

    public String createBody() {
        Rule rule = checker.getRule();

        StringBuilder body = new StringBuilder();
        body.append("<strong>").append(createSubject()).append("</strong>");
        body.append(LINE_FEED);
        body.append(LINE_FEED);
        body.append(String.format("Rule : %s", rule.getCheckerName()));
        body.append(LINE_FEED);
        body.append(checker.getEmailMessage());
        body.append(String.format(LINK_FORMAT, pinpointUrl, pinpointUrl));
        
        return body.toString();
    }
}
