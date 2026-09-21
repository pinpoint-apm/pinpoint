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
import com.navercorp.pinpoint.alarm.evaluation.MetricQueryService;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import com.navercorp.pinpoint.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reads what an application served out of the map statistics.
 *
 * <p>The counts come from the same histograms the server map builds its nodes from, so a rule
 * and the screen an operator checks it against cannot disagree about what happened.
 *
 * <p>Rates are computed over the whole window rather than averaged per slot: a minute with one
 * slow request out of one is not five percent of an hour that had twenty, and averaging the
 * per-slot rates would say it was.
 */
public class ApplicationResponseMetricQueryService implements MetricQueryService {

    private static final Logger logger =
            LogManager.getLogger(ApplicationResponseMetricQueryService.class);

    private final MapResponseDao mapResponseDao;
    private final RuleApplicationResolver applicationResolver;

    public ApplicationResponseMetricQueryService(MapResponseDao mapResponseDao,
                                                 RuleApplicationResolver applicationResolver) {
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
        this.applicationResolver = Objects.requireNonNull(applicationResolver, "applicationResolver");
    }

    @Override
    public AlarmDataSource getDataSource() {
        return CoreAlarmDataSource.APPLICATION_RESPONSE;
    }

    @Override
    public MetricQueryResult query(AlarmRuleV2 rule, List<AlarmCondition> conditions,
                                   List<AlarmFilter> filters, AlarmState state) {
        // One clock read for the whole rule, so the range reported to the notification is the
        // one that was queried rather than a per-leaf blend.
        long nowMs = System.currentTimeMillis();
        Application application = applicationResolver.resolve(rule);

        Map<MetricQueryKey, Double> results = new LinkedHashMap<>();
        Map<Integer, ResponseSummary> summaryByWindow = new HashMap<>();
        MetricQueryResult.QueriedRange range = null;

        for (AlarmCondition leaf : conditions) {
            MetricQueryKey key = MetricQueryKey.from(leaf);
            if (results.containsKey(key)) {
                continue;
            }
            int windowSec = windowSecOf(rule, leaf);
            ResponseSummary summary = summaryByWindow.computeIfAbsent(windowSec,
                    sec -> read(application, nowMs, sec));

            Double value = valueOf(leaf.getMetric(), summary);
            if (value == null) {
                logger.warn("Unknown {} metric='{}', rule_id={}, skipping",
                        getDataSource().name(), leaf.getMetric(), rule.getId());
                continue;
            }
            results.put(key, value);
            range = MetricQueryResult.QueriedRange.union(range,
                    new MetricQueryResult.QueriedRange(nowMs - windowSec * 1000L, nowMs));
        }
        return MetricQueryResult.of(results, range);
    }

    private ResponseSummary read(Application application, long nowMs, int windowSec) {
        Range range = Range.between(nowMs - windowSec * 1000L, nowMs);
        ApplicationResponse response =
                mapResponseDao.selectApplicationResponse(application, new TimeWindow(range));
        return ResponseSummary.of(response.getApplicationHistograms());
    }

    /** Apdex is this data source's alone; the rest are the same arithmetic as out-call. */
    private Double valueOf(String metric, ResponseSummary summary) {
        if ("apdex_score".equals(metric)) {
            return summary.apdexScore();
        }
        return summary.valueOf(metric);
    }

}
