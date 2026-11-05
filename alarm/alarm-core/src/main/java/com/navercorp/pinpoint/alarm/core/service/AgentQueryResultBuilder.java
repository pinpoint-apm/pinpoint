/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.core.service;

import com.navercorp.pinpoint.alarm.core.vo.CoreAlarmDataSource;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryKey;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult.Detail;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult.Details;
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult.QueriedRange;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.common.timeseries.time.Range;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The result of a query whose detail lines name agents. Each line links to the inspector of its
 * agent, and a metric keeps {@value #MAX_LINES} lines at most, so one application with many
 * agents does not flood the message. The message counts the rest.
 */
final class AgentQueryResultBuilder {

    static final int MAX_LINES = 10;

    private final Map<MetricQueryKey, Double> values = new LinkedHashMap<>();
    private final Map<MetricQueryKey, Details> details = new LinkedHashMap<>();
    private QueriedRange range;

    static Detail agentLine(AlarmRuleV2 rule, String agentId, String text, Range window) {
        return new Detail(text, agentId,
                CoreAlarmDataSource.agentInspectorPath(rule, agentId, window.getFrom(), window.getTo()));
    }

    boolean has(MetricQueryKey key) {
        return values.containsKey(key);
    }

    void put(MetricQueryKey key, double value, Range window) {
        values.put(key, value);
        range = QueriedRange.union(range, new QueriedRange(window.getFrom(), window.getTo()));
    }

    void put(MetricQueryKey key, double value, Range window, String summary, List<Detail> lines) {
        put(key, value, window);
        details.put(key, new Details(summary, lines.subList(0, Math.min(lines.size(), MAX_LINES)),
                Math.max(0, lines.size() - MAX_LINES)));
    }

    MetricQueryResult result() {
        return new MetricQueryResult(values, details, range);
    }
}
