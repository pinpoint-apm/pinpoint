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

import com.navercorp.pinpoint.web.alarm.collector.MapStatisticsCallerDataCollector;
import com.navercorp.pinpoint.web.alarm.collector.MapStatisticsCallerDataCollector.DataCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

/**
 * @author minwoo.jung
 */
@Deprecated
public class TotalCountToCalleeChecker extends LongValueAlarmChecker {

    public TotalCountToCalleeChecker(MapStatisticsCallerDataCollector dataCollector, Rule rule) {
        super(rule, "", dataCollector);
    }

    @Override
    protected Long getDetectedValue() {
        String calleeName = rule.getNotes();
        return ((MapStatisticsCallerDataCollector)dataCollector).getCount(calleeName, DataCategory.TOTAL_COUNT);
    }

}
