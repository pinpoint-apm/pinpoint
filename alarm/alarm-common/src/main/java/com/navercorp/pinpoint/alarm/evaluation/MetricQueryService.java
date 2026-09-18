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
package com.navercorp.pinpoint.alarm.evaluation;

import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.validation.FilterKeyValidator;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmState;

import java.util.List;

/**
 * Queries metric values for condition evaluation.
 * <p>
 * One implementation per data source, each querying its own storage and
 * returning the metric values the conditions are evaluated against.
 */
public interface MetricQueryService {

    /**
     * @return the data source this service handles
     */
    AlarmDataSource getDataSource();

    /**
     * Queries metric values for the given conditions.
     *
     * @param rule       the alarm rule (for serviceName/applicationName context)
     * @param conditions all leaf conditions extracted from the condition tree
     * @param filters    the filters to apply as WHERE clauses
     * @param state      the current alarm state (provides lastCheckedAt for trigger-based metrics)
     * @return aggregated values per metric query key, with optional detail lines, and the
     *         time range the queries covered -- a notification links to that range, so it
     *         must be the range actually queried, and null only when no query ran
     */
    MetricQueryResult query(AlarmRuleV2 rule, List<AlarmCondition> conditions,
                            List<AlarmFilter> filters, AlarmState state);

    default List<AlarmFilter> validateFilterKeys(List<AlarmFilter> filters) {
        return FilterKeyValidator.validateFilterKeys(getDataSource(), filters);
    }
}
