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

package com.navercorp.pinpoint.web.applicationmap.map;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.SpanOwner;
import com.navercorp.pinpoint.common.server.bo.TraceSourceType;
import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.server.util.UserNodeUtils;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.TestTraceUtils;
import com.navercorp.pinpoint.web.applicationmap.link.LinkKey;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.component.DefaultApplicationFactory;
import com.navercorp.pinpoint.web.service.ServiceModelResolver;
import com.navercorp.pinpoint.web.util.ServiceTypeRegistryMockFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseHistograms;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author HyunGil Jeong
 */
@ExtendWith(MockitoExtension.class)
public class FilteredMapBuilderTest {

    private static final Random RANDOM = new Random();

    // Mocked
    private final ServiceTypeRegistryService registry = TestTraceUtils.mockServiceTypeRegistryService();

    private ApplicationFactory applicationFactory;

    @BeforeEach
    public void setUp() {
        this.applicationFactory = new DefaultApplicationFactory(registry, mock(ServiceModelResolver.class));
    }

    /**
     * USER -> ROOT_APP -> APP_A -> CACHE
     */
    @Test
    public void twoTier() {
        // Given
        final Range range = Range.between(1, 200000);
        TimeWindow timeWindow = new TimeWindow(range);
        final FilteredMapBuilder builder = new FilteredMapBuilder(applicationFactory, registry, timeWindow);

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

        // When
        builder.addTransaction(List.of(rootSpan, appASpan));
        FilteredMap filteredMap = builder.build();

        // Then
        LinkDataDuplexMap linkDataDuplexMap = filteredMap.getLinkDataDuplexMap();
        LinkDataMap sourceLinkDataMap = linkDataDuplexMap.getSourceLinkDataMap();
        assertSourceLinkData(sourceLinkDataMap,
                newUserNode("ROOT_APP"), registry.findServiceType(TestTraceUtils.USER_TYPE_CODE),
                "ROOT_APP", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE));
        assertSourceLinkData(sourceLinkDataMap,
                "ROOT_APP", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE),
                "www.foo.com/bar", registry.findServiceType(TestTraceUtils.RPC_TYPE_CODE));
        assertSourceLinkData(sourceLinkDataMap,
                "APP_A", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE),
                "CacheName", registry.findServiceType(TestTraceUtils.CACHE_TYPE_CODE));
        LinkDataMap targetLinkDataMap = linkDataDuplexMap.getTargetLinkDataMap();
        assertTargetLinkData(targetLinkDataMap,
                newUserNode("ROOT_APP"), registry.findServiceType(TestTraceUtils.USER_TYPE_CODE),
                "ROOT_APP", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE));
        assertTargetLinkData(targetLinkDataMap,
                "ROOT_APP", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE),
                "APP_A", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE));

        ResponseHistograms responseHistograms = filteredMap.getResponseHistograms();
        Application rootApplication = new Application("ROOT_APP", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE));
        List<ResponseTime> rootAppResponseTimes = responseHistograms.getResponseTimeList(rootApplication);

        assertThat(rootAppResponseTimes).hasSize(1);

        Application applicationA = new Application("APP_A", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE));
        List<ResponseTime> appAResponseTimes = responseHistograms.getResponseTimeList(applicationA);

        assertThat(appAResponseTimes).hasSize(1);
    }

    private String newUserNode(String nodeName) {
        return UserNodeUtils.newUserNodeName(nodeName, TestTraceUtils.TEST_STAND_ALONE_TYPE);
    }

    private void assertSourceLinkData(LinkDataMap sourceLinkDataMap, String fromApplicationName, ServiceType fromServiceType, String toApplicationName, ServiceType toServiceType) {
        LinkKey linkKey = LinkKey.of(fromApplicationName, fromServiceType, toApplicationName, toServiceType);
        LinkData sourceLinkData = sourceLinkDataMap.getLinkData(linkKey);
        String assertMessage = String.format("%s[%s] to %s[%s] source link data does not exist", fromApplicationName, fromServiceType.getName(), toApplicationName, toServiceType.getName());
        Assertions.assertNotNull(sourceLinkData, assertMessage);
    }

    private void assertTargetLinkData(LinkDataMap targetLinkDataMap, String fromApplicationName, ServiceType fromServiceType, String toApplicationName, ServiceType toServiceType) {
        LinkKey linkKey = LinkKey.of(fromApplicationName, fromServiceType, toApplicationName, toServiceType);
        LinkData targetLinkData = targetLinkDataMap.getLinkData(linkKey);
        String assertMessage = String.format("%s[%s] from %s[%s] target link data does not exist", toApplicationName, toServiceType.getName(), fromApplicationName, fromServiceType.getName());
        Assertions.assertNotNull(targetLinkData, assertMessage);
    }

    // =======================================================================
    // OTel Span.Link → upstream/downstream link data across transactions
    // =======================================================================

    private static final String UPSTREAM_TRACE_ID = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static final String DOWNSTREAM_TRACE_ID = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";
    private static final long UPSTREAM_SPAN_ID = 100L;

    private static ServiceTypeRegistryService otelRegistry() {
        ServiceTypeRegistryMockFactory mockFactory = new ServiceTypeRegistryMockFactory();
        mockFactory.addServiceTypeMock(ServiceType.UNKNOWN);
        mockFactory.addServiceTypeMock(ServiceType.USER);
        mockFactory.addServiceTypeMock(ServiceType.OPENTELEMETRY_SERVER);
        mockFactory.addServiceTypeMock(ServiceType.OPENTELEMETRY_INTERNAL);
        return mockFactory.createMockServiceTypeRegistryService();
    }

    private static SpanBo otelRootSpan(String applicationName, String agentId, String traceIdHex, long spanId) {
        SpanOwner owner = new SpanOwner();
        owner.setAgentId(agentId);
        owner.setApplicationName(applicationName);
        SpanBo span = new SpanBo(TraceSourceType.OPENTELEMETRY, owner);
        span.setApplicationServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode());
        span.setServiceType(ServiceType.OPENTELEMETRY_SERVER.getCode());
        span.setTransactionId(OtelServerTraceId.of(traceIdHex));
        span.setSpanId(spanId);
        span.setParentSpanId(-1);
        span.setTraceTime(0, 1000L, 100);
        span.setCollectorAcceptTime(1100L);
        return span;
    }

    // Raw collector wire shape of the OPENTELEMETRY_LINK annotation (spanId as decimal string)
    private static AnnotationBo linkAnnotation(String targetTraceIdHex, long targetSpanId) {
        return AnnotationBo.of(AnnotationKey.OPENTELEMETRY_LINK.getCode(),
                "{\"traceId\":\"" + targetTraceIdHex + "\",\"spanId\":\"" + targetSpanId + "\"}");
    }

    private static SpanEventBo linkSpanEvent(String targetTraceIdHex, long targetSpanId) {
        SpanEventBo event = new SpanEventBo();
        event.setServiceType(ServiceType.OPENTELEMETRY_INTERNAL.getCode());
        event.addAnnotation(linkAnnotation(targetTraceIdHex, targetSpanId));
        return event;
    }

    private void assertOtelLinkData(FilteredMap filteredMap) {
        LinkDataDuplexMap linkDataDuplexMap = filteredMap.getLinkDataDuplexMap();
        LinkKey linkKey = LinkKey.of("APP_UP", ServiceType.OPENTELEMETRY_SERVER, "APP_DOWN", ServiceType.OPENTELEMETRY_SERVER);
        Assertions.assertNotNull(linkDataDuplexMap.getSourceLinkDataMap().getLinkData(linkKey),
                "APP_UP -> APP_DOWN OTel link source data does not exist");
        Assertions.assertNotNull(linkDataDuplexMap.getTargetLinkDataMap().getLinkData(linkKey),
                "APP_UP -> APP_DOWN OTel link target data does not exist");
    }

    private FilteredMap buildOtelLinkMap(SpanBo upstream, SpanBo downstream) {
        TimeWindow timeWindow = new TimeWindow(Range.between(1, 200000));
        ServiceTypeRegistryService otelRegistry = otelRegistry();
        ApplicationFactory otelApplicationFactory = new DefaultApplicationFactory(otelRegistry, mock(ServiceModelResolver.class));
        FilteredMapBuilder builder = new FilteredMapBuilder(otelApplicationFactory, otelRegistry, timeWindow);
        builder.addTransactions(List.of(List.of(upstream), List.of(downstream)));
        return builder.build();
    }

    @Test
    public void otelLink_onDownstreamSpan() {
        SpanBo upstream = otelRootSpan("APP_UP", "agent-up", UPSTREAM_TRACE_ID, UPSTREAM_SPAN_ID);
        SpanBo downstream = otelRootSpan("APP_DOWN", "agent-down", DOWNSTREAM_TRACE_ID, 200L);
        downstream.addAnnotation(linkAnnotation(UPSTREAM_TRACE_ID, UPSTREAM_SPAN_ID));

        assertOtelLinkData(buildOtelLinkMap(upstream, downstream));
    }

    @Test
    public void otelLink_onDownstreamSpanEvent() {
        // link carried by an OTel sub-span (child SpanEvent) — e.g. exporters that attach
        // cross-trace links to child spans only; must index to the owning SpanBo
        SpanBo upstream = otelRootSpan("APP_UP", "agent-up", UPSTREAM_TRACE_ID, UPSTREAM_SPAN_ID);
        SpanBo downstream = otelRootSpan("APP_DOWN", "agent-down", DOWNSTREAM_TRACE_ID, 200L);
        downstream.addSpanEvent(linkSpanEvent(UPSTREAM_TRACE_ID, UPSTREAM_SPAN_ID));

        assertOtelLinkData(buildOtelLinkMap(upstream, downstream));
    }

    @Test
    public void otelLink_onDownstreamSpanChunkEvent() {
        // split arrival: the link-bearing sub-span was stored as an orphan SpanChunk
        SpanBo upstream = otelRootSpan("APP_UP", "agent-up", UPSTREAM_TRACE_ID, UPSTREAM_SPAN_ID);
        SpanBo downstream = otelRootSpan("APP_DOWN", "agent-down", DOWNSTREAM_TRACE_ID, 200L);
        SpanChunkBo chunk = new SpanChunkBo(TraceSourceType.OPENTELEMETRY);
        chunk.addSpanEventBoList(List.of(linkSpanEvent(UPSTREAM_TRACE_ID, UPSTREAM_SPAN_ID)));
        downstream.addSpanChunkBo(chunk);

        assertOtelLinkData(buildOtelLinkMap(upstream, downstream));
    }
}
