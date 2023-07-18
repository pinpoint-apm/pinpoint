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

package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.batch.alarm.collector.DataCollector;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.AlarmCheckerDetectedValue;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.CheckerDetectedValue;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author koo.taejin
 * @author minwoo.jung
 * @author Jongjin.Bae
 */
public abstract class AlarmChecker<T> implements AlarmCheckerInterface {

    protected final Logger logger = LogManager.getLogger(this.getClass());

    protected final Rule rule;
    protected final String unit;
    protected final DataCollector dataCollector;

    protected boolean detected = false;

    protected AlarmChecker(Rule rule, String unit, DataCollector dataCollector) {
        this.rule = Objects.requireNonNull(rule, "rule");
        this.unit = Objects.requireNonNull(unit, "unit");
        this.dataCollector = dataCollector;
    }
    
    public boolean isDetected() {
        return detected;
    }
    
    public Rule getRule() {
        return rule;
    }
    
    public boolean isSMSSend() {
        return rule.isSmsSend();
    }
    
    public boolean isEmailSend() {
        return rule.isEmailSend();
    }
    
    public boolean isWebhookSend() {
        return rule.isWebhookSend();
    }
    
    public String getUserGroupId() {
        return rule.getUserGroupId();
    }

    public String getRuleId() {
        return rule.getRuleId();
    }
    
    public String getUnit() {
        return unit;
    }

    protected abstract boolean decideResult(T value);

    public void check() {
        dataCollector.collect();
        detected = decideResult(getDetectedValue());
        logger.info("{} result is {} for application ({}). value is {}. (threshold : {}).", this.getClass().getSimpleName(), detected, rule.getApplicationId(), getDetectedValue(), rule.getThreshold());
    }
    
    public List<String> getSmsMessage() {
        List<String> messages = new ArrayList<>();
        messages.add(String.format("[PINPOINT Alarm - %s] %s is %s%s (Threshold : %s%s)", rule.getApplicationId(), rule.getCheckerName(), getDetectedValue(), unit, rule.getThreshold(), unit));
        return messages;
    }

    @Override
    public String getEmailMessage(String pinpointUrl, String applicationId, String serviceType, String currentTime) {
        return String.format("%s value is %s%s during the past 5 mins.(Threshold : %s%s)<br>", rule.getCheckerName(), getDetectedValue(), unit, rule.getThreshold(), unit);
    }
    
    protected abstract T getDetectedValue();
    
    public CheckerDetectedValue getCheckerDetectedValue() {
        return new AlarmCheckerDetectedValue<>(getDetectedValue());
    }
    
    public abstract String getCheckerType();

    public String toString() {
        final StringBuilder sb = new StringBuilder("AlarmChecker {");
        sb.append("rule=").append(rule);
        sb.append('}');
        return sb.toString();
    }
    
}
