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

import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.vo.AlarmCondition;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * That the numbers match what the v1 alarm reported for the same responses.
 *
 * <p>A rule moved from the v1 screen to this one has to fire on the same traffic, so these
 * assert the arithmetic the v1 collector did rather than merely that a number comes back:
 * slow counts both slow buckets, a rate is taken over the window instead of averaged across
 * slots, and apdex counts the fast bucket as satisfied and the normal one as tolerating.
 *
 * <p>The responses are recorded through the histogram the map statistics actually store, so
 * the slot boundaries are the real ones rather than a stub's idea of them.
 */
class ApplicationResponseMetricQueryServiceTest {

    private static final ServiceType TEST_TYPE = ServiceType.TEST_STAND_ALONE;
    private static final Service TEST_SERVICE = new Service("svc", 42);
    /** What the deployable's resolver does: all three parts come off the rule. */
    private static final RuleApplicationResolver RESOLVER =
            rule -> new Application(TEST_SERVICE, rule.getApplicationName(), TEST_TYPE);
    private static final int FAST_MS = 100;
    private static final int NORMAL_MS = 2_000;
    private static final int SLOW_MS = 4_000;
    private static final int VERY_SLOW_MS = 10_000;

    @Test
    void slowCountIsBothSlowBuckets() {
        MetricQueryResult result = query(List.of("slow_count"), builder -> {
            record(builder, 4, SLOW_MS, false);
            record(builder, 6, VERY_SLOW_MS, false);
            record(builder, 90, FAST_MS, false);
        });

        assertEquals(10.0, value(result, "slow_count"));
    }

    @Test
    void ratesAreTakenOverTheWindowNotAveragedPerSlot() {
        // One slot with 1 of 1 slow, another with 0 of 99. Averaging the two slot rates would
        // say 50%; over the window it is 1%.
        MetricQueryResult result = query(List.of("slow_rate"), builder -> {
            builder.addResponseTime("agent", 0L, SLOW_MS, false);
            for (int i = 0; i < 99; i++) {
                builder.addResponseTime("agent", 60_000L, FAST_MS, false);
            }
        });

        assertEquals(1.0, value(result, "slow_rate"));
    }

    @Test
    void errorCountAndRateComeFromTheErrorTotal() {
        MetricQueryResult result = query(List.of("error_count", "error_rate"), builder -> {
            record(builder, 25, FAST_MS, true);
            record(builder, 75, FAST_MS, false);
        });

        assertEquals(25.0, value(result, "error_count"));
        assertEquals(25.0, value(result, "error_rate"));
    }

    @Test
    void apdexCountsFastAsSatisfiedAndNormalAsTolerating() {
        // (60 + 20/2) / 100 = 0.7, reported on the 0-100 scale the rates use.
        MetricQueryResult result = query(List.of("apdex_score"), builder -> {
            record(builder, 60, FAST_MS, false);
            record(builder, 20, NORMAL_MS, false);
            record(builder, 20, SLOW_MS, false);
        });

        assertEquals(70.0, value(result, "apdex_score"));
    }

    @Test
    void aRateOfNothingIsZeroRatherThanUndefined() {
        MetricQueryResult result = query(List.of("slow_rate", "error_rate", "total_count"),
                builder -> { });

        assertEquals(0.0, value(result, "slow_rate"));
        assertEquals(0.0, value(result, "error_rate"));
        assertEquals(0.0, value(result, "total_count"));
    }

    @Test
    void anUnknownMetricIsSkippedRatherThanFailingTheWholeRule() {
        MetricQueryResult result = query(List.of("slow_count", "no_such_metric"),
                builder -> record(builder, 3, SLOW_MS, false));

        assertEquals(3.0, value(result, "slow_count"));
        assertFalse(result.isEmpty());
        assertEquals(1, result.values().size());
    }

    @Test
    void theRangeReportedIsTheWindowThatWasQueried() {
        MetricQueryResult result = query(List.of("total_count"),
                builder -> record(builder, 1, FAST_MS, false));

        MetricQueryResult.QueriedRange range = result.range();
        assertEquals(300_000L, range.toMs() - range.fromMs());
    }

    // ---- helpers ----

    private static void record(ApplicationResponse.Builder builder, int times,
                               int elapsedMs, boolean error) {
        for (int i = 0; i < times; i++) {
            builder.addResponseTime("agent", 0L, elapsedMs, error);
        }
    }

    // The map keys its rows by service uid, so reading a rule against the default service
    // when the rule names another one finds an empty histogram and calls it healthy.
    @Test
    void theRuleServiceReachesTheMapQuery() {
        AtomicReference<Application> queried = new AtomicReference<>();
        MapResponseDao dao = (application, timeWindow) -> {
            queried.set(application);
            return ApplicationResponse.newBuilder(application).build();
        };

        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(1L);
        rule.setApplicationName("app");
        rule.setServiceName("svc");
        rule.setCheckIntervalSec(300);

        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric("total_count");
        leaf.setWindowSec(300);

        new ApplicationResponseMetricQueryService(dao, RESOLVER)
                .query(rule, List.of(leaf), List.of(), null);

        assertEquals(TEST_SERVICE.getServiceUid(), queried.get().getService().getServiceUid());
    }

    private MetricQueryResult query(List<String> metrics,
                                    Consumer<ApplicationResponse.Builder> responses) {
        MapResponseDao dao = (application, timeWindow) -> {
            ApplicationResponse.Builder builder = ApplicationResponse.newBuilder(application);
            responses.accept(builder);
            return builder.build();
        };
        ApplicationResponseMetricQueryService service =
                new ApplicationResponseMetricQueryService(dao, RESOLVER);

        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(1L);
        rule.setApplicationName("app");
        rule.setCheckIntervalSec(300);

        List<AlarmCondition> conditions = metrics.stream().map(metric -> {
            AlarmCondition leaf = new AlarmCondition();
            leaf.setType(AlarmCondition.Type.LEAF);
            leaf.setMetric(metric);
            leaf.setWindowSec(300);
            return leaf;
        }).toList();

        return service.query(rule, conditions, List.of(), null);
    }

    private Double value(MetricQueryResult result, String metric) {
        return result.values().entrySet().stream()
                .filter(entry -> metric.equals(entry.getKey().metric()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no value for " + metric));
    }
}
