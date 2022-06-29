/*
 * Copyright 2022 NAVER Corp.
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

import com.navercorp.pinpoint.batch.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;

/**
 * @author youngjin.kim2
 */
public class ApdexScoreChecker extends LongValueAlarmChecker {

    public ApdexScoreChecker(ResponseTimeDataCollector dataCollector, Rule rule) {
        super(rule, "%", dataCollector);
    }

    @Override
    protected Long getDetectedValue() {
        final ResponseTimeDataCollector dataCollector = (ResponseTimeDataCollector) this.dataCollector;

        final long satisfiedCount = dataCollector.getFastCount();
        final long toleratingCount = dataCollector.getNormalCount();
        final long totalSamples = dataCollector.getTotalCount();
        final double score = (new ApdexScore(satisfiedCount, toleratingCount, totalSamples)).getApdexScore();

        return (long) (score * 100.0);
    }

    @Override
    protected boolean decideResult(Long value) {
        return value <= rule.getThreshold();
    }

}
