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

import com.navercorp.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

/**
 * @author minwoo.jung
 */
public class ResponseCountChecker extends LongValueAlarmChecker {
    
    public ResponseCountChecker(ResponseTimeDataCollector dataCollector, Rule rule) {
        super(rule, "", dataCollector);
    }

    @Override
    protected Long getDetectedValue() {
        return ((ResponseTimeDataCollector)dataCollector).getTotalCount();
    }

}
