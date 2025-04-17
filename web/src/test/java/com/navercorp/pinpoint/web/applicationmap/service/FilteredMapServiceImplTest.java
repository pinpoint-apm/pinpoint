/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.service;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.util.json.JsonField;
import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowDownSampler;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.TestTraceUtils;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilderFactory;
import com.navercorp.pinpoint.web.applicationmap.FilterMapWithScatter;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInfoAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.AgentInfoServerGroupListDataSource;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.view.ResponseTimeViewModel;
import com.navercorp.pinpoint.web.applicationmap.view.TimeCount;
import com.navercorp.pinpoint.web.applicationmap.view.TimeHistogramViewModel;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.ServerInstanceDatasourceService;
import com.navercorp.pinpoint.web.view.id.AgentNameView;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(MockitoExtension.class)
public class FilteredMapServiceImplTest {

    private static final Random RANDOM = new Random();

    @AutoClose("shutdown")
    private static final Executor executor = Executors.newFixedThreadPool(8);

    @Mock
    private TraceDao traceDao;

    @Mock
    private ApplicationTraceIndexDao applicationTraceIndexDao;

    @Mock
    private ApplicationFactory applicationFactory;

    @Mock
    private ServerInstanceDatasourceService serverInstanceDatasourceService;

    // Mocked
    private final ServiceTypeRegistryService registry = TestTraceUtils.mockServiceTypeRegistryService();

    TimeHistogramFormat format = TimeHistogramFormat.V1;

    @Spy
    private ApplicationMapBuilderFactory applicationMapBuilderFactory = new ApplicationMapBuilderFactory(
            new NodeHistogramAppenderFactory(executor),
            new ServerInfoAppenderFactory(executor)
    );

    private FilteredMapService filteredMapService;

    @BeforeEach
    public void init() {
        when(applicationFactory.createApplication(anyString(), anyInt()))
                .thenAnswer(invocation -> {
                    String applicationName = invocation.getArgument(0);
                    ServiceType serviceType = registry.findServiceType(invocation.getArgument(1));
                    return new Application(applicationName, serviceType);
                });
        when(applicationFactory.createApplication(anyString(), any(ServiceType.class)))
                .thenAnswer(invocation -> {
                    String applicationName = invocation.getArgument(0);
                    ServiceType serviceType = invocation.getArgument(1);
                    return new Application(applicationName, serviceType);
                });
        lenient().when(applicationFactory.createApplicationByTypeName(anyString(), anyString()))
                .thenAnswer(invocation -> {
                    String applicationName = invocation.getArgument(0);
                    ServiceType serviceType = registry.findServiceTypeByName(invocation.getArgument(1));
                    return new Application(applicationName, serviceType);
                });

        when(serverInstanceDatasourceService.getServerGroupListDataSource())
                .thenAnswer(invocation -> {
                    AgentInfoService agentInfoService = mock(AgentInfoService.class);
                    return new AgentInfoServerGroupListDataSource(agentInfoService);
                });

        filteredMapService = new FilteredMapServiceImpl(traceDao, applicationTraceIndexDao,
                registry, applicationFactory, serverInstanceDatasourceService, Optional.empty(), applicationMapBuilderFactory);

    }

    /**
     * USER -> ROOT_APP -> APP_A -> CACHE
     */
    @Test
    public void twoTier() {
        // Given
        Range originalRange = Range.between(1000, 2000);
        Range scanRange = Range.between(1000, 2000);
        final TimeWindow timeWindow = new TimeWindow(originalRange, TimeWindowDownSampler.SAMPLER);

        // root app span
        long rootSpanId = RANDOM.nextLong();
        long rootSpanStartTime = 1000L;
        long rootSpanCollectorAcceptTime = 1210L;
        int rootSpanElapsed = 200;
        SpanBo rootSpan = new TestTraceUtils.SpanBuilder("ROOT_APP", "root-agent")
                .spanId(rootSpanId)
                .startTime(rootSpanStartTime)
                .collectorAcceptTime(rootSpanCollectorAcceptTime)
                .elapsed(rootSpanElapsed)
                .build();
        // app A span
        long appASpanId = RANDOM.nextLong();
        long appASpanStartTime = 1020L;
        long appASpanCollectorAcceptTime = 1090L;
        int appASpanElapsed = 160;
        SpanBo appASpan = new TestTraceUtils.SpanBuilder("APP_A", "app-a")
                .spanId(appASpanId)
                .parentSpan(rootSpan)
                .startTime(appASpanStartTime)
                .collectorAcceptTime(appASpanCollectorAcceptTime)
                .elapsed(appASpanElapsed)
                .build();
        // root app -> app A rpc span event
        SpanEventBo rootRpcSpanEvent = new TestTraceUtils.RpcSpanEventBuilder("www.foo.com/bar", 10, 190)
                .nextSpanId(appASpanId)
                .build();
        rootSpan.addSpanEvent(rootRpcSpanEvent);
        // app A -> cache span event
        int cacheStartElapsed = 20;
        int cacheEndElapsed = 130;
        SpanEventBo appACacheSpanEvent = new TestTraceUtils.CacheSpanEventBuilder("CacheName", "1.1.1.1", cacheStartElapsed, cacheEndElapsed).build();
        appASpan.addSpanEvent(appACacheSpanEvent);

        when(traceDao.selectAllSpans(anyList(), isNull())).thenReturn(List.of(List.of(rootSpan, appASpan)));

        // When
        final FilteredMapServiceOption option = new FilteredMapServiceOption.Builder(Collections.emptyList(), originalRange, 1, 1, Filter.acceptAllFilter(), 0).build();
        FilterMapWithScatter filterMapWithScatter = filteredMapService.selectApplicationMapWithScatterData(option);
        ApplicationMap applicationMap = filterMapWithScatter.getApplicationMap();

        // Then
        Collection<Node> nodes = applicationMap.getNodes();
        assertThat(nodes).hasSize(4);
        for (Node node : nodes) {
            Application application = node.getApplication();
            if (application.getName().equals("ROOT_APP") && application.getServiceType().getCode() == TestTraceUtils.USER_TYPE_CODE) {
                // USER node
                NodeHistogram nodeHistogram = node.getNodeHistogram();
                // histogram
                Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
                assertHistogram(applicationHistogram, 1, 0, 0, 0, 0);
                Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
                Assertions.assertTrue(agentHistogramMap.isEmpty());
                // time histogram
                HistogramSchema histogramSchema = node.getServiceType().getHistogramSchema();
                List<TimeCount> expectedTimeCounts = List.of(
                        new TimeCount(timeWindow.refineTimestamp(rootSpanCollectorAcceptTime), 1)
                );

                List<TimeHistogramViewModel> applicationTimeHistogram = getApplicationTimeHistogram(nodeHistogram, format);
                assertTimeHistogram(applicationTimeHistogram, histogramSchema.getFastSlot(), expectedTimeCounts);
                JsonFields<AgentNameView, List<TimeHistogramViewModel>> agentTimeHistogram = getAgentTimeHistogram(nodeHistogram);
//                AgentResponseTimeViewModelList agentTimeHistogram = agentTimeHistogram;
                Assertions.assertTrue(agentTimeHistogram.isEmpty());
            } else if (application.getName().equals("ROOT_APP") && application.getServiceType().getCode() == TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE) {
                // ROOT_APP node
                NodeHistogram nodeHistogram = node.getNodeHistogram();
                // histogram
                Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
                assertHistogram(applicationHistogram, 1, 0, 0, 0, 0);
                Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
                assertAgentHistogram(agentHistogramMap, "root-agent", 1, 0, 0, 0, 0);
                // time histogram
                HistogramSchema histogramSchema = node.getServiceType().getHistogramSchema();
                List<TimeCount> expectedTimeCounts = List.of(
                        new TimeCount(timeWindow.refineTimestamp(rootSpanCollectorAcceptTime), 1)
                );
                List<TimeHistogramViewModel> applicationTimeHistogram = getApplicationTimeHistogram(nodeHistogram, format);
                assertTimeHistogram(applicationTimeHistogram, histogramSchema.getFastSlot(), expectedTimeCounts);
                JsonFields<AgentNameView, List<TimeHistogramViewModel>> agentTimeHistogram = getAgentTimeHistogram(nodeHistogram);
//                AgentResponseTimeViewModelList agentTimeHistogram = nodeHistogram.getAgentTimeHistogram(TimeHistogramFormat.V1);
                assertAgentTimeHistogram(agentTimeHistogram, "root-agent", histogramSchema.getFastSlot(), expectedTimeCounts);
            } else if (application.getName().equals("APP_A") && application.getServiceType().getCode() == TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE) {
                // APP_A node
                NodeHistogram nodeHistogram = node.getNodeHistogram();
                // histogram
                Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
                assertHistogram(applicationHistogram, 1, 0, 0, 0, 0);
                Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
                assertAgentHistogram(agentHistogramMap, "app-a", 1, 0, 0, 0, 0);
                // time histogram
                HistogramSchema histogramSchema = node.getServiceType().getHistogramSchema();
                List<TimeCount> expectedTimeCounts = List.of(
                        new TimeCount(timeWindow.refineTimestamp(appASpanCollectorAcceptTime), 1)
                );
                List<TimeHistogramViewModel> applicationTimeHistogram = getApplicationTimeHistogram(nodeHistogram, format);
                assertTimeHistogram(applicationTimeHistogram, histogramSchema.getFastSlot(), expectedTimeCounts);
                JsonFields<AgentNameView, List<TimeHistogramViewModel>> agentTimeHistogram = getAgentTimeHistogram(nodeHistogram);
//                AgentResponseTimeViewModelList agentTimeHistogram = nodeHistogram.getAgentTimeHistogram(TimeHistogramFormat.V1);
                assertAgentTimeHistogram(agentTimeHistogram, "app-a", histogramSchema.getFastSlot(), expectedTimeCounts);
            } else if (application.getName().equals("CacheName") && application.getServiceType().getCode() == TestTraceUtils.CACHE_TYPE_CODE) {
                // CACHE node
                NodeHistogram nodeHistogram = node.getNodeHistogram();
                // histogram
                Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
                assertHistogram(applicationHistogram, 0, 1, 0, 0, 0);
                Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
                assertAgentHistogram(agentHistogramMap, "1.1.1.1", 0, 1, 0, 0, 0);
                // time histogram
                HistogramSchema histogramSchema = node.getServiceType().getHistogramSchema();
                List<TimeCount> expectedTimeCounts = List.of(
                        new TimeCount(timeWindow.refineTimestamp(appASpanStartTime + cacheStartElapsed), 1)
                );
                List<TimeHistogramViewModel> applicationTimeHistogram = getApplicationTimeHistogram(nodeHistogram, format);
                assertTimeHistogram(applicationTimeHistogram, histogramSchema.getNormalSlot(), expectedTimeCounts);
                JsonFields<AgentNameView, List<TimeHistogramViewModel>> agentTimeHistogram = getAgentTimeHistogram(nodeHistogram);
//                AgentResponseTimeViewModelList agentTimeHistogram = nodeHistogram.getAgentTimeHistogram(TimeHistogramFormat.V1);
                assertAgentTimeHistogram(agentTimeHistogram, "1.1.1.1", histogramSchema.getNormalSlot(), expectedTimeCounts);
            } else {
                fail("Unexpected node : " + node);
            }
        }

        Collection<Link> links = applicationMap.getLinks();
        assertThat(links).hasSize(3);
        for (Link link : links) {
            Application fromApplication = link.getFrom().getApplication();
            Application toApplication = link.getTo().getApplication();
            if ((fromApplication.getName().equals("ROOT_APP") && fromApplication.getServiceType().getCode() == TestTraceUtils.USER_TYPE_CODE) &&
                    (toApplication.getName().equals("ROOT_APP") && toApplication.getServiceType().getCode() == TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE)) {
                // histogram
                Histogram histogram = link.getHistogram();
                assertHistogram(histogram, 1, 0, 0, 0, 0);
                // time histogram
                List<TimeHistogramViewModel> linkApplicationTimeSeriesHistogram = link.getLinkApplicationTimeSeriesHistogram(format);
                HistogramSchema targetHistogramSchema = toApplication.getServiceType().getHistogramSchema();
                List<TimeCount> expectedTimeCounts = List.of(
                        new TimeCount(timeWindow.refineTimestamp(rootSpanCollectorAcceptTime), 1)
                );
                assertTimeHistogram(linkApplicationTimeSeriesHistogram, targetHistogramSchema.getFastSlot(), expectedTimeCounts);
            } else if ((fromApplication.getName().equals("ROOT_APP") && fromApplication.getServiceType().getCode() == TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE) &&
                    (toApplication.getName().equals("APP_A") && toApplication.getServiceType().getCode() == TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE)) {
                // histogram
                Histogram histogram = link.getHistogram();
                assertHistogram(histogram, 1, 0, 0, 0, 0);
                // time histogram
                List<TimeHistogramViewModel> linkApplicationTimeSeriesHistogram = link.getLinkApplicationTimeSeriesHistogram(format);
                HistogramSchema targetHistogramSchema = toApplication.getServiceType().getHistogramSchema();
                List<TimeCount> expectedTimeCounts = List.of(
                        new TimeCount(timeWindow.refineTimestamp(appASpanCollectorAcceptTime), 1)
                );
                assertTimeHistogram(linkApplicationTimeSeriesHistogram, targetHistogramSchema.getFastSlot(), expectedTimeCounts);
            } else if ((fromApplication.getName().equals("APP_A") && fromApplication.getServiceType().getCode() == TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE) &&
                    (toApplication.getName().equals("CacheName") && toApplication.getServiceType().getCode() == TestTraceUtils.CACHE_TYPE_CODE
                    )) {
                // histogram
                Histogram histogram = link.getHistogram();
                assertHistogram(histogram, 0, 1, 0, 0, 0);
                // time histogram
                List<TimeHistogramViewModel> linkApplicationTimeSeriesHistogram = link.getLinkApplicationTimeSeriesHistogram(format);
                HistogramSchema targetHistogramSchema = toApplication.getServiceType().getHistogramSchema();
                List<TimeCount> expectedTimeCounts = List.of(
                        new TimeCount(timeWindow.refineTimestamp(appASpanStartTime + cacheStartElapsed), 1)
                );
                assertTimeHistogram(linkApplicationTimeSeriesHistogram, targetHistogramSchema.getNormalSlot(), expectedTimeCounts);
            } else {
                fail("Unexpected link : " + link);
            }
        }
    }

    private List<TimeHistogramViewModel> getApplicationTimeHistogram(NodeHistogram nodeHistogram, TimeHistogramFormat format) {
        ApplicationTimeHistogram applicationTimeHistogram = nodeHistogram.getApplicationTimeHistogram();
        return applicationTimeHistogram.createViewModel(format);
    }

    private JsonFields<AgentNameView, List<TimeHistogramViewModel>> getAgentTimeHistogram(NodeHistogram nodeHistogram) {
        AgentTimeHistogram factory = nodeHistogram.getAgentTimeHistogram();
        return factory.createViewModel(TimeHistogramFormat.V1);
    }

    private void assertHistogram(Histogram histogram, int fastCount, int normalCount, int slowCount, int verySlowCount, int totalErrorCount) {
        Assertions.assertEquals(fastCount, histogram.getFastCount());
        Assertions.assertEquals(normalCount, histogram.getNormalCount());
        Assertions.assertEquals(slowCount, histogram.getSlowCount());
        Assertions.assertEquals(verySlowCount, histogram.getVerySlowCount());
        Assertions.assertEquals(totalErrorCount, histogram.getTotalErrorCount());
    }

    private void assertTimeHistogram(List<TimeHistogramViewModel> histogramList, HistogramSlot histogramSlot, List<TimeCount> expectedTimeCounts) {
        if (CollectionUtils.isEmpty(histogramList)) {
            fail("Checked against empty histogramList.");
        }
        String slotName = histogramSlot.getSlotName();
        for (TimeHistogramViewModel timeViewModel : histogramList) {
            ResponseTimeViewModel histogram = (ResponseTimeViewModel) timeViewModel;

            if (histogram.getColumnName().equals(slotName)) {
                for (TimeCount expectedTimeCount : expectedTimeCounts) {
                    boolean expectedTimeCountExists = false;
                    for (TimeCount actualTimeCount : histogram.getColumnValue()) {
                        if (expectedTimeCount.time() == actualTimeCount.time()) {
                            expectedTimeCountExists = true;
                            Assertions.assertEquals(expectedTimeCount.count(), actualTimeCount.count(), "TimeCount mismatch for slot : " + slotName);
                            break;
                        }
                    }
                    if (!expectedTimeCountExists) {
                        fail("Expected TimeCount for " + slotName + " not found, time : " + expectedTimeCount.time() + ", count : " + expectedTimeCount.count());
                    }
                }
                return;
            }
        }
        fail("Expected " + slotName + " but had none.");
    }

    private void assertAgentHistogram(Map<String, Histogram> agentHistogramMap, String agentId, int fastCount, int normalCount, int slowCount, int verySlowCount, int totalErrorCount) {
        Histogram agentHistogram = agentHistogramMap.get(agentId);
        if (agentHistogram != null) {
            assertHistogram(agentHistogram, fastCount, normalCount, slowCount, verySlowCount, totalErrorCount);
            return;
        }
        fail("Histogram not found for agent : " + agentId);
    }

    private void assertAgentTimeHistogram(JsonFields<AgentNameView, List<TimeHistogramViewModel>> histogramList, String agentId, HistogramSlot histogramSlot, List<TimeCount> expectedTimeCounts) {
        for (JsonField<AgentNameView, List<TimeHistogramViewModel>> field : histogramList) {
            if (agentId.equals(field.name().agentName())) {
                assertTimeHistogram(field.value(), histogramSlot, expectedTimeCounts);
                return;
            }
        }
        fail("Time histogram not found for agent : " + agentId);
    }
}
