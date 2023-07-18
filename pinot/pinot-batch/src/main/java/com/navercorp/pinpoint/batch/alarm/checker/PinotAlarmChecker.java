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

import com.navercorp.pinpoint.batch.alarm.collector.PinotDataCollector;
import com.navercorp.pinpoint.batch.alarm.condition.AlarmCondition;
import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmRule;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.WebhookPayload;
import com.navercorp.pinpoint.common.server.util.time.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class PinotAlarmChecker<T extends Number> implements PinotAlarmCheckerInterface {
    private final static String MENU_URL = "urlStatistic";
    private final static long SLOT_INTERVAL_FIVE_MIN = 300000;

    protected final Logger logger = LogManager.getLogger(this.getClass());

    protected final List<PinotAlarmRule> rules;
    protected final String serviceName;
    protected final String applicationName;
    protected final String target;
    protected final String unit;
    protected final PinotDataCollector<T> dataCollector;
    protected final AlarmCondition<T> alarmCondition;
    protected T collectedValue;
    protected final boolean[] alarmDetected;


    protected PinotAlarmChecker(List<PinotAlarmRule> rules, String unit, PinotDataCollector<T> dataCollector, AlarmCondition<T> alarmCondition) {
        this.rules = Objects.requireNonNull(rules, "rules");
        this.serviceName = rules.get(0).getServiceName();
        this.applicationName = rules.get(0).getApplicationName();
        this.target = rules.get(0).getTarget();
        this.unit = Objects.requireNonNull(unit, "unit");
        this.dataCollector = Objects.requireNonNull(dataCollector, "dataCollector");
        this.alarmCondition = Objects.requireNonNull(alarmCondition, "alarmCondition");
        this.alarmDetected = new boolean[rules.size()];
        validateRules();
    }

    private void validateRules() {
        for (PinotAlarmRule rule : rules) {
            if (!rule.getServiceName().equals(serviceName)) {
                throw new RuntimeException("Single PinotAlarmChecker should have rules with the same service names.");
            }
            if (!rule.getApplicationName().equals(applicationName)) {
                throw new RuntimeException("Single PinotAlarmChecker should have rules with the same application names.");
            }
            if (!rule.getTarget().equals(target)) {
                throw new RuntimeException("Single PinotAlarmChecker should have rules with the same target.");
            }
        }
    }

    public String getMenuUrl() {
        return MENU_URL;
    }

    public String getRuleId(int index) {
        return rules.get(index).getId();
    }

    public String getRule(int index) {
        return rules.get(index).toString();
    }

    public T getCollectedValue() {
        return collectedValue;
    }

    public List<PinotAlarmRule> getRules() {
        return rules;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getTarget() {
        return target;
    }

    public String getAlarmConditionText(int index) {
        return rules.get(index).getCondition();
    }

    public String getCheckerName(int index) {
        return rules.get(index).getCheckerName();
    }

    public String getNotes(int index) {
        return rules.get(index).getNotes();
    }

    public AlarmCondition getAlarmCondition() {
        return alarmCondition;
    }

    public BigDecimal getThreshold(int index) {
        return rules.get(index).getThreshold();
    }
    public boolean isSMSSend(int index) {
        return rules.get(index).isSmsSend();
    }
    
    public boolean isEmailSend(int index) {
        return rules.get(index).isEmailSend();
    }
    
    public boolean isWebhookSend(int index) {
        return rules.get(index).isWebhookSend();
    }
    
    public String getUserGroupId(int index) {
        return rules.get(index).getUserGroupId();
    }
    
    public String getUnit() {
        return unit;
    }

    public boolean[] check(long timeSlotEndTime) {
        Range range = Range.newUncheckedRange(timeSlotEndTime - SLOT_INTERVAL_FIVE_MIN, timeSlotEndTime);
        collectedValue = dataCollector.collect(serviceName, applicationName, target, range);

        for (int i = 0; i < rules.size(); i++) {
            PinotAlarmRule rule = rules.get(i);
            BigDecimal threshold = rule.getThreshold();

            boolean result = decideResult(threshold, collectedValue);
            alarmDetected[i] = result;

            logger.info("{} result is {} for application ({}). (threshold : {}, condition : {}, value: {}).",
                    this.getClass().getSimpleName(), result, rule.getApplicationName(),
                    threshold, rule.getCondition(), collectedValue);
        }
        return alarmDetected;
    }

    public boolean[] getAlarmDetected() {
        return alarmDetected;
    }

    public List<String> getSmsMessage(int index) {
        List<String> messages = new ArrayList<>();
        PinotAlarmRule rule = rules.get(index);
        messages.add(String.format("[PINPOINT Alarm - %s] %s %s is %s%s (Condition: %s, Threshold : %s%s)",
                applicationName, target, rule.getCheckerName(), collectedValue, unit,
                rule.getCondition(), rule.getThreshold(), unit));
        return messages;
    }
    
    public String getEmailMessage(int index) {
        PinotAlarmRule rule = rules.get(index);
        return String.format("%s %s %s value is %s%s during the past 5 mins.(Condition: %s, Threshold : %s%s)<br>",
                applicationName, target, rule.getCheckerName(), collectedValue, unit,
                rule.getCondition(), rule.getThreshold(), unit);
    }

    protected abstract boolean decideResult(BigDecimal threshold, T value);

}
