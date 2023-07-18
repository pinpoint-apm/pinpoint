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

package com.navercorp.pinpoint.batch.alarm.sender;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmCheckerInterface;
import com.navercorp.pinpoint.web.vo.RuleInterface;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class AlarmMailTemplate {

    private static final String LINE_FEED = "<br>";
    private static final String LINK_FORMAT = "<a href=\"%s\" >pinpoint site</a>";
    private static final String SCATTER_CHART_LINK_FORMAT = "<a href=\"%s/main/%s@%s/5m/%s\" >scatter chart of %s</a>";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");

    private final String pinpointUrl;
    private final AlarmCheckerInterface checker;
    private final String batchEnv;
    private final int sequenceCount;

    public AlarmMailTemplate(AlarmCheckerInterface checker, String pinpointUrl, String batchEnv, int sequenceCount) {
        this.checker = Objects.requireNonNull(checker, "checker");
        this.pinpointUrl = Objects.requireNonNull(pinpointUrl, "pinpointUrl");
        this.batchEnv = Objects.requireNonNull(batchEnv, "batchEnv");
        this.sequenceCount = sequenceCount;
    }

    public String createSubject() {
        RuleInterface rule = checker.getRule();
        return String.format("[PINPOINT-%s] %s Alarm for %s Service. #%d", batchEnv, rule.getCheckerName(), rule.getApplicationId(), sequenceCount);
    }

    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        return FORMATTER.format(now);
    }

    public String createBody() {
        RuleInterface rule = checker.getRule();
        return newBody(createSubject(), rule.getCheckerName(), rule.getApplicationId(), rule.getServiceType(), getCurrentTime());
    }

    private String newBody(String subject, String rule, String applicationId, String serviceType, String currentTime) {
        StringBuilder body = new StringBuilder();
        body.append("<strong>").append(subject).append("</strong>");
        body.append(LINE_FEED);
        body.append(LINE_FEED);
        body.append(String.format("Rule : %s", rule));
        body.append(LINE_FEED);
        body.append(checker.getEmailMessage(pinpointUrl, applicationId, serviceType, currentTime));
        body.append(String.format(LINK_FORMAT, pinpointUrl));
        body.append(LINE_FEED);
        body.append(String.format(SCATTER_CHART_LINK_FORMAT, pinpointUrl, applicationId, serviceType, currentTime, applicationId));

        return body.toString();
    }
}
