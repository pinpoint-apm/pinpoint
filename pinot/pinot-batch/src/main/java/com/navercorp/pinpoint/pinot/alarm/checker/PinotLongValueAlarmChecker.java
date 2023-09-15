/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.pinot.alarm.checker;

import com.navercorp.pinpoint.pinot.alarm.collector.PinotDataCollector;
import com.navercorp.pinpoint.pinot.alarm.condition.AlarmCondition;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmRule;

import java.math.BigDecimal;
import java.util.List;

public class PinotLongValueAlarmChecker extends PinotAlarmChecker<Long> {

    public PinotLongValueAlarmChecker(List<PinotAlarmRule> rules, String unit, PinotDataCollector dataCollector, AlarmCondition<Long> alarmCondition) {
        super(rules, unit, dataCollector, alarmCondition);
    }

    @Override
    protected boolean decideResult(BigDecimal threshold, Long value) {
        return alarmCondition.isConditionMet(threshold, value);
    }
    
}
