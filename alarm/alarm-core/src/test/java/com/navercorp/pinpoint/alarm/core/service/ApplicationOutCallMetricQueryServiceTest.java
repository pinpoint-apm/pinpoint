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
import com.navercorp.pinpoint.alarm.vo.AlarmFilter;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.trace.HistogramSchemas;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * That a rule can watch more than one callee, and that a callee it never called reads as zero.
 *
 * <p>The v1 alarm carried the callee in the rule's notes field, so a rule watched exactly one
 * and a second callee meant a second rule. Here it is a filter, which is what makes the other
 * shapes below expressible at all.
 */
class ApplicationOutCallMetricQueryServiceTest {

    private static final ServiceType CALLER = ServiceType.TEST_STAND_ALONE;
    private static final Service TEST_SERVICE = new Service("svc", 42);
    /** What the deployable's resolver does: all three parts come off the rule. */
    private static final RuleApplicationResolver RESOLVER =
            rule -> new Application(TEST_SERVICE, rule.getApplicationName(), CALLER);
    private static final ServiceType CALLEE = ServiceType.TEST_STAND_ALONE;
    private static final short FAST_SLOT =
            HistogramSchemas.NORMAL_SCHEMA.getFastSlot().getSlotTime();
    private static final short SLOW_SLOT =
            HistogramSchemas.NORMAL_SCHEMA.getSlowSlot().getSlotTime();

    @Test
    void oneCalleeIsSelectedByAnEqualsFilter() {
        MetricQueryResult result = query(List.of("slow_count"),
                List.of(new AlarmFilter("callee", AlarmFilter.Op.EQ, "db")),
                links -> {
                    addCall(links, "db", SLOW_SLOT, 7);
                    addCall(links, "cache", SLOW_SLOT, 99);
                });

        assertEquals(7.0, value(result, "slow_count"));
    }

    @Test
    void withNoCalleeFilterEveryCallIsMeasured() {
        MetricQueryResult result = query(List.of("total_count"), List.of(),
                links -> {
                    addCall(links, "db", FAST_SLOT, 3);
                    addCall(links, "cache", FAST_SLOT, 4);
                });

        assertEquals(7.0, value(result, "total_count"));
    }

    @Test
    void aCalleeThatWasNeverCalledReadsAsZeroRatherThanAsNoData() {
        MetricQueryResult result = query(List.of("slow_count", "slow_rate"),
                List.of(new AlarmFilter("callee", AlarmFilter.Op.EQ, "gone")),
                links -> addCall(links, "db", SLOW_SLOT, 5));

        assertEquals(0.0, value(result, "slow_count"));
        assertEquals(0.0, value(result, "slow_rate"));
    }

    @Test
    void aRateIsTakenAcrossTheCalleesTheFilterAccepted() {
        // db is 1 of 1 slow, cache 0 of 99. Over the two it is 1%, not the 50% that averaging
        // the two callees' rates would give.
        MetricQueryResult result = query(List.of("slow_rate"), List.of(),
                links -> {
                    addCall(links, "db", SLOW_SLOT, 1);
                    addCall(links, "cache", FAST_SLOT, 99);
                });

        assertEquals(1.0, value(result, "slow_rate"));
    }

    @Test
    void aNotEqualsFilterMeasuresEveryOtherCallee() {
        MetricQueryResult result = query(List.of("total_count"),
                List.of(new AlarmFilter("callee", AlarmFilter.Op.NEQ, "cache")),
                links -> {
                    addCall(links, "db", FAST_SLOT, 3);
                    addCall(links, "cache", FAST_SLOT, 40);
                    addCall(links, "search", FAST_SLOT, 5);
                });

        assertEquals(8.0, value(result, "total_count"));
    }

    // ---- helpers ----

    private static void addCall(LinkDataMap links, String callee, short slotTime, long count) {
        links.addLinkData(
                new Application("caller", CALLER), "caller-agent",
                new Application(callee, CALLEE), callee + "-agent",
                0L, slotTime, count);
    }

    // The map keys its rows by service uid, so reading a rule against the default service
    // when the rule names another one finds no out calls and calls it healthy.
    @Test
    void theRuleServiceReachesTheMapQuery() {
        AtomicReference<Application> queried = new AtomicReference<>();
        MapOutLinkDao dao = (application, timeWindow) -> {
            queried.set(application);
            return new LinkDataMap();
        };

        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(1L);
        rule.setApplicationName("caller");
        rule.setServiceName("svc");
        rule.setCheckIntervalSec(300);

        AlarmCondition leaf = new AlarmCondition();
        leaf.setType(AlarmCondition.Type.LEAF);
        leaf.setMetric("total_count");
        leaf.setWindowSec(300);

        new ApplicationOutCallMetricQueryService(dao, RESOLVER)
                .query(rule, List.of(leaf), List.of(), null);

        assertEquals(TEST_SERVICE.getServiceUid(), queried.get().getService().getServiceUid());
    }

    private MetricQueryResult query(List<String> metrics, List<AlarmFilter> filters,
                                    java.util.function.Consumer<LinkDataMap> calls) {
        MapOutLinkDao dao = (application, timeWindow) -> {
            LinkDataMap links = new LinkDataMap();
            calls.accept(links);
            return links;
        };
        ApplicationOutCallMetricQueryService service =
                new ApplicationOutCallMetricQueryService(dao, RESOLVER);

        AlarmRuleV2 rule = new AlarmRuleV2();
        rule.setId(1L);
        rule.setApplicationName("caller");
        rule.setCheckIntervalSec(300);

        List<AlarmCondition> conditions = metrics.stream().map(metric -> {
            AlarmCondition leaf = new AlarmCondition();
            leaf.setType(AlarmCondition.Type.LEAF);
            leaf.setMetric(metric);
            leaf.setWindowSec(300);
            return leaf;
        }).toList();

        return service.query(rule, conditions, filters, null);
    }

    private Double value(MetricQueryResult result, String metric) {
        return result.values().entrySet().stream()
                .filter(entry -> metric.equals(entry.getKey().metric()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no value for " + metric));
    }
}
