/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.service.map;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.TestTraceUtils;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.service.ApplicationFactory;
import com.navercorp.pinpoint.web.service.DefaultApplicationFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseHistograms;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author HyunGil Jeong
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class FilteredMapBuilderTest {

    private static final Random RANDOM = new Random();

    private static final int VERSION = 0;

    // Mocked
    private ServiceTypeRegistryService registry = TestTraceUtils.mockServiceTypeRegistryService();

    private ApplicationFactory applicationFactory;

    @Before
    public void setUp() {
        this.applicationFactory = new DefaultApplicationFactory(registry);
    }

    /**
     * USER -> ROOT_APP -> APP_A -> CACHE
     */
    @Test
    public void twoTier() {
        // Given
        final Range range = new Range(1, 200000);
        final FilteredMapBuilder builder = new FilteredMapBuilder(applicationFactory, registry, range, VERSION);

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
        builder.addTransaction(Arrays.asList(rootSpan, appASpan));
        FilteredMap filteredMap = builder.build();

        // Then
        LinkDataDuplexMap linkDataDuplexMap = filteredMap.getLinkDataDuplexMap();
        LinkDataMap sourceLinkDataMap = linkDataDuplexMap.getSourceLinkDataMap();
        assertSourceLinkData(sourceLinkDataMap,
                "ROOT_APP", registry.findServiceType(TestTraceUtils.USER_TYPE_CODE),
                "ROOT_APP", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE));
        assertSourceLinkData(sourceLinkDataMap,
                "ROOT_APP", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE),
                "www.foo.com/bar", registry.findServiceType(TestTraceUtils.RPC_TYPE_CODE));
        assertSourceLinkData(sourceLinkDataMap,
                "APP_A", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE),
                "CacheName", registry.findServiceType(TestTraceUtils.CACHE_TYPE_CODE));
        LinkDataMap targetLinkDataMap = linkDataDuplexMap.getTargetLinkDataMap();
        assertTargetLinkData(targetLinkDataMap,
                "ROOT_APP", registry.findServiceType(TestTraceUtils.USER_TYPE_CODE),
                "ROOT_APP", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE));
        assertTargetLinkData(targetLinkDataMap,
                "ROOT_APP", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE),
                "APP_A", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE));

        ResponseHistograms responseHistograms = filteredMap.getResponseHistograms();
        Application rootApplication = new Application("ROOT_APP", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE));
        List<ResponseTime> rootAppResponseTimes = responseHistograms.getResponseTimeList(rootApplication);
        Assert.assertNotNull(rootAppResponseTimes);
        Assert.assertEquals(1, rootAppResponseTimes.size());
        Application applicationA = new Application("APP_A", registry.findServiceType(TestTraceUtils.TEST_STAND_ALONE_TYPE_CODE));
        List<ResponseTime> appAResponseTimes = responseHistograms.getResponseTimeList(applicationA);
        Assert.assertNotNull(appAResponseTimes);
        Assert.assertEquals(1, appAResponseTimes.size());
    }

    private void assertSourceLinkData(LinkDataMap sourceLinkDataMap, String fromApplicationName, ServiceType fromServiceType, String toApplicationName, ServiceType toServiceType) {
        LinkKey linkKey = new LinkKey(fromApplicationName, fromServiceType, toApplicationName, toServiceType);
        LinkData sourceLinkData = sourceLinkDataMap.getLinkData(linkKey);
        String assertMessage = String.format("%s[%s] to %s[%s] source link data does not exist", fromApplicationName, fromServiceType.getName(), toApplicationName, toServiceType.getName());
        Assert.assertNotNull(assertMessage, sourceLinkData);
    }

    private void assertTargetLinkData(LinkDataMap targetLinkDataMap, String fromApplicationName, ServiceType fromServiceType, String toApplicationName, ServiceType toServiceType) {
        LinkKey linkKey = new LinkKey(fromApplicationName, fromServiceType, toApplicationName, toServiceType);
        LinkData targetLinkData = targetLinkDataMap.getLinkData(linkKey);
        String assertMessage = String.format("%s[%s] from %s[%s] target link data does not exist", toApplicationName, toServiceType.getName(), fromApplicationName, fromServiceType.getName());
        Assert.assertNotNull(assertMessage, targetLinkData);
    }
}
