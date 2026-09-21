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
import com.navercorp.pinpoint.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.applicationmap.rawdata.LinkCallData;
import com.navercorp.pinpoint.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reads what happened to the calls an application made to another.
 *
 * <p>Which callee to measure comes from a filter, so one rule can watch several. The v1 alarm
 * carried the callee in the rule's notes field, which meant a rule watched exactly one and
 * watching two meant two rules.
 *
 * <p>A filter naming a callee this application did not call in the window reads as zero rather
 * than as no data: the rule asked about calls that did not happen, and that is an answer.
 */
public class ApplicationOutCallMetricQueryService implements MetricQueryService {

    private static final Logger logger =
            LogManager.getLogger(ApplicationOutCallMetricQueryService.class);

    static final String CALLEE_FILTER_KEY = "callee";

    private final MapOutLinkDao mapOutLinkDao;
    private final RuleApplicationResolver applicationResolver;

    public ApplicationOutCallMetricQueryService(
            MapOutLinkDao mapOutLinkDao,
            RuleApplicationResolver applicationResolver) {
        this.mapOutLinkDao = Objects.requireNonNull(mapOutLinkDao, "mapOutLinkDao");
        this.applicationResolver = Objects.requireNonNull(applicationResolver, "applicationResolver");
    }

    @Override
    public AlarmDataSource getDataSource() {
        return CoreAlarmDataSource.APPLICATION_OUT_CALL;
    }

    @Override
    public MetricQueryResult query(AlarmRuleV2 rule, List<AlarmCondition> conditions,
                                   List<AlarmFilter> filters, AlarmState state) {
        long nowMs = System.currentTimeMillis();
        Application application = applicationResolver.resolve(rule);
        List<AlarmFilter> validated = validateFilterKeys(filters);

        Map<MetricQueryKey, Double> results = new LinkedHashMap<>();
        Map<Integer, Map<String, ResponseSummary>> byWindow = new HashMap<>();
        MetricQueryResult.QueriedRange range = null;

        for (AlarmCondition leaf : conditions) {
            MetricQueryKey key = MetricQueryKey.from(leaf);
            if (results.containsKey(key)) {
                continue;
            }
            int windowSec = windowSecOf(rule, leaf);
            Map<String, ResponseSummary> byCallee = byWindow.computeIfAbsent(windowSec,
                    sec -> read(application, nowMs, sec));

            ResponseSummary summary = select(byCallee, validated);
            Double value = summary.valueOf(leaf.getMetric());
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

    private Map<String, ResponseSummary> read(Application application, long nowMs, int windowSec) {
        Range range = Range.between(nowMs - windowSec * 1000L, nowMs);
        LinkDataMap outLinks = mapOutLinkDao.selectOutLink(application, new TimeWindow(range));

        // Keyed by the callee application, which is on the link rather than on the call data
        // inside it: there the target is the agent or host that answered, so keying by it
        // would make a filter naming an application match nothing and one naming an agent
        // match only the last agent read.
        Map<String, ResponseSummary> byCallee = new HashMap<>();
        for (LinkData linkData : outLinks.getLinkDataList()) {
            String callee = linkData.getToApplication().getApplicationName();
            ResponseSummary summary = byCallee.getOrDefault(callee, ResponseSummary.EMPTY);
            for (LinkCallData callData : linkData.getLinkCallDataMap().getLinkDataList()) {
                summary = add(summary, ResponseSummary.of(callData.getTimeHistogram()));
            }
            byCallee.put(callee, summary);
        }
        return byCallee;
    }

    /**
     * Adds up the callees the filters accept. With no callee filter every call this
     * application made is measured, which is what a rule watching an application's outbound
     * traffic as a whole is asking for.
     */
    private ResponseSummary select(Map<String, ResponseSummary> byCallee,
                                   List<AlarmFilter> filters) {
        List<AlarmFilter> calleeFilters = new ArrayList<>();
        for (AlarmFilter filter : filters) {
            if (CALLEE_FILTER_KEY.equals(filter.getKey())) {
                calleeFilters.add(filter);
            }
        }
        ResponseSummary total = ResponseSummary.EMPTY;
        for (Map.Entry<String, ResponseSummary> entry : byCallee.entrySet()) {
            if (accepts(calleeFilters, entry.getKey())) {
                total = add(total, entry.getValue());
            }
        }
        return total;
    }

    private boolean accepts(List<AlarmFilter> filters, String callee) {
        for (AlarmFilter filter : filters) {
            String value = filter.getValue();
            boolean matches = switch (filter.getOp()) {
                case EQ -> callee.equals(value);
                case NEQ -> !callee.equals(value);
                case CONTAINS -> callee.contains(value);
                case NOT_CONTAINS -> !callee.contains(value);
            };
            if (!matches) {
                return false;
            }
        }
        return true;
    }

    private ResponseSummary add(ResponseSummary left, ResponseSummary right) {
        return new ResponseSummary(
                left.fastCount() + right.fastCount(),
                left.normalCount() + right.normalCount(),
                left.slowCount() + right.slowCount(),
                left.errorCount() + right.errorCount(),
                left.totalCount() + right.totalCount());
    }

}
