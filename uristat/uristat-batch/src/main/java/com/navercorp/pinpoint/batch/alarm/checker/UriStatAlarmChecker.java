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

import com.navercorp.pinpoint.batch.alarm.condition.AlarmCondition;
import com.navercorp.pinpoint.batch.alarm.condition.AlarmConditionFactory;
import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmRule;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Function;

/**
 * @author minwoo.jung
 */
public enum UriStatAlarmChecker {

    TOTAL_COUNT("TOTAL COUNT", AlarmConditionFactory::getLongAlarmCondition),
    FAILURE_COUNT("FAILURE COUNT", AlarmConditionFactory::getLongAlarmCondition),
    APDEX("APDEX", AlarmConditionFactory::getDoubleAlarmCondition),
    AVG_RESPONSE_MS("AVG RESPONSE", AlarmConditionFactory::getDoubleAlarmCondition),
    MAX_RESPONES_MS("MAX RESPONSE", AlarmConditionFactory::getLongAlarmCondition);

    private static final Set<UriStatAlarmChecker> CHECKER_CATEGORIES = EnumSet.allOf(UriStatAlarmChecker.class);
    
    public static UriStatAlarmChecker getValue(String checkerName) {
        for (UriStatAlarmChecker category : CHECKER_CATEGORIES) {
            if (category.checkerName.equalsIgnoreCase(checkerName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown CheckerCategory : " + checkerName);
    }

    private final String checkerName;
    private final Function<PinotAlarmRule, AlarmCondition> alarmConditionGetter;

    UriStatAlarmChecker(String checkerName, Function<PinotAlarmRule, AlarmCondition> alarmConditionGetter) {
        this.checkerName = checkerName;
        this.alarmConditionGetter = alarmConditionGetter;
    }

    public Function<PinotAlarmRule, AlarmCondition> getAlarmConditionGetter() {
        return alarmConditionGetter;
    }
}
