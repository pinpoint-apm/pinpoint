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

package com.navercorp.pinpoint.web.alarm.checker;


import com.navercorp.pinpoint.web.alarm.collector.DataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * @author koo.taejin
 * @author minwoo.jung
 */
public abstract class AlarmChecker<T> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final DataCollector dataCollector;
    protected final Rule rule;
    protected boolean detected = false;
    protected final String unit;
    
    protected AlarmChecker(Rule rule, String unit, DataCollector dataCollector) {
        this.rule = rule;
        this.unit = unit;
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
    
    public String getuserGroupId() {
        return rule.getUserGroupId();
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
        List<String> messages = new LinkedList<>();
        messages.add(String.format("[PINPOINT Alarm - %s] %s is %s%s (Threshold : %s%s)", rule.getApplicationId(), rule.getCheckerName(), getDetectedValue(), unit, rule.getThreshold(), unit));
        return messages;
    }
    
    public String getEmailMessage() {
        return String.format("%s value is %s%s during the past 5 mins.(Threshold : %s%s)<br>", rule.getCheckerName(), getDetectedValue(), unit, rule.getThreshold(), unit);
    }
    
    protected abstract T getDetectedValue();

}
