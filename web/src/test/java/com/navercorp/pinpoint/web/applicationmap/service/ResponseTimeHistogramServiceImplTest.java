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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeCategory;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerGroupListDataSource;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.link.LinkKey;
import com.navercorp.pinpoint.web.applicationmap.link.LinkName;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelector;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelectorFactory;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelectorType;
import com.navercorp.pinpoint.web.applicationmap.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.service.ServerInstanceDatasourceService;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResponseTimeHistogramServiceImplTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private LinkSelectorFactory linkSelectorFactory;

    private ServerInstanceDatasourceService serverInstanceDatasourceService;

    private MapResponseDao mapResponseDao;

    @BeforeEach
    public void SetUp() {
        mapResponseDao = mock(MapResponseDao.class);
        serverInstanceDatasourceService = mock(ServerInstanceDatasourceService.class);
        linkSelectorFactory = mock(LinkSelectorFactory.class);

        when(serverInstanceDatasourceService.getServerGroupListDataSource()).thenReturn(new ServerGroupListDataSource() {
            @Override
            public ServerGroupList createServerGroupList(Node node, Instant timestamp) {
                return ServerGroupList.empty();
            }
        });
    }

    /**
     * empty WAS(target)
     */
    @Test
    public void selectNodeHistogramEmptyWASDataTest() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final Application nodeApplication = new Application("WAS", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(mapResponseDao.selectResponseTime(eq(nodeApplication), any(Range.class))).thenReturn(Collections.emptyList());

        //WAS node does not use fromApplications or toApplications to build nodeHistogramData
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, Collections.emptyList(), Collections.emptyList())
                .setUseStatisticsAgentState(true)
                .build();

        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 0);
    }

    /**
     * WAS(target)
     */
    @Test
    public void selectNodeHistogramWASDataTest() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final Application nodeApplication = new Application("WAS", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(mapResponseDao.selectResponseTime(eq(nodeApplication), any(Range.class))).thenReturn(List.of(createResponseTime(nodeApplication, timestamp)));

        //WAS node does not use fromApplications or toApplications to build nodeHistogramData
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, Collections.emptyList(), Collections.emptyList())
                .setUseStatisticsAgentState(true)
                .build();

        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    private ResponseTime createResponseTime(Application application, long timestamp) {
        ResponseTime responseTime = new ResponseTime(application.name(), application.serviceType(), timestamp);
        HistogramSchema schema = application.serviceType().getHistogramSchema();
        responseTime.addResponseTime("agentId", schema.getFastSlot().getSlotTime(), 1L);
        responseTime.addResponseTime("agentId", schema.getNormalSlot().getSlotTime(), 2L);
        responseTime.addResponseTime("agentId", schema.getSlowSlot().getSlotTime(), 3L);
        responseTime.addResponseTime("agentId", schema.getErrorSlot().getSlotTime(), 4L);
        responseTime.addResponseTime("agentId", schema.getSumStatSlot().getSlotTime(), 1000L);
        responseTime.addResponseTime("agentId", schema.getMaxStatSlot().getSlotTime(), 2000L);
        return responseTime;
    }

    /**
     * USER(target) -> WAS
     */
    @Test
    public void selectNodeHistogramDataTest1() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final Application nodeApplication = new Application("user", ServiceType.USER);
        final Application toApplication = new Application("was1", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        //User node use toApplications(was1) to build histogramData
        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), eq(LinkDataMapProcessor.NO_OP), any(LinkDataMapProcessor.class)))
                .thenReturn(createCalleeLinkSelector(List.of(new LinkKey(nodeApplication, toApplication)), timestamp));

        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, Collections.emptyList(), List.of(toApplication))
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS -> UNKNOWN(target)
     */
    @Test
    public void selectNodeHistogramDataTest2() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final Application nodeApplication = new Application("unknown", ServiceType.UNKNOWN);
        final Application was = new Application("was", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(was, nodeApplication)), timestamp));

        //UNKNOWN(TERMINAL, ALIAS) node use fromApplications to build nodeHistogramData
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, List.of(was), Collections.emptyList())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS1 ->
     * WAS2 -> UNKNOWN(target)
     */
    @Test
    public void selectNodeHistogramDataTest3() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final Application nodeApplication = new Application("unknown", ServiceType.UNKNOWN);
        final Application was1 = new Application("was1", ServiceType.STAND_ALONE);
        final Application was2 = new Application("was2", ServiceType.STAND_ALONE);


        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(
                        new LinkKey(was1, nodeApplication),
                        new LinkKey(was2, nodeApplication)
                ), timestamp));

        //UNKNOWN(TERMINAL, ALIAS) node use toApplications to build nodeHistogramData
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, List.of(was1, was2), Collections.emptyList())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        //two WAS node -> UNKNOWN node
        assertHistogramValues(histogram, 2);
    }


    /**
     * WAS -> CACHE_LIBRARY(target)
     */
    @Test
    public void selectNodeHistogramDataCacheTest() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        //CACHE_LIBRARY serviceTypeCode 8000 ~ 8299
        ServiceType cacheServiceType = ServiceTypeFactory.of(8299, "CACHE", "CACHE", ServiceTypeProperty.TERMINAL, ServiceTypeProperty.RECORD_STATISTICS);
        final Application nodeApplication = new Application("cache", cacheServiceType);
        final Application was = new Application("was", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(was, nodeApplication)), timestamp));

        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, List.of(was), Collections.emptyList())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        //CACHE Node's Histogram schema should be FAST_SCHEMA
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.FAST_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS -> QUEUE(target)
     */
    @Test
    public void selectNodeHistogramDataQueueTest1() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        //MESSAGE_BROKER serviceTypeCode 8300 ~ 8799
        final ServiceType queueServiceType = ServiceTypeFactory.of(8799, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application nodeApplication = new Application("queue", queueServiceType);
        final Application was1 = new Application("was1", ServiceType.STAND_ALONE);
        final Application was2 = new Application("was2", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), any(LinkDataMapProcessor.class)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(was1, nodeApplication)), timestamp));

        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, List.of(was1, was2), Collections.emptyList())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS(out of range) -> QUEUE(target) -> ...
     * <p>
     * ignore Application not in range of serverMap search range
     */
    @Test
    public void selectNodeHistogramDataQueueTest2() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        //MESSAGE_BROKER serviceTypeCode 8300 ~ 8799
        final ServiceType queueServiceType = ServiceTypeFactory.of(8799, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application nodeApplication = new Application("queue", queueServiceType);
        final Application was = new Application("was", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        //with no source Application do not scan
        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), any(LinkDataMapProcessor.class)))
                .thenThrow(new IllegalStateException("no scan for QUEUE node with empty sourceApplications"));

        //fromApplications out of Search range
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, Collections.emptyList(), Collections.emptyList())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 0);
    }

    /**
     * WAS1(out of range)   ->
     * ...      -> WAS2     -> QUEUE(target)
     * <p>
     * might happen in bidirectional search
     * ignore Application not in range of serverMap search range
     */
    @Test
    public void selectNodeHistogramDataQueueTest3() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        //MESSAGE_BROKER serviceTypeCode 8300 ~ 8799
        final ServiceType queueServiceType = ServiceTypeFactory.of(8799, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application nodeApplication = new Application("queue", queueServiceType);
        final Application was1 = new Application("was1", ServiceType.STAND_ALONE);
        final Application was2 = new Application("was2", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), any(LinkDataMapProcessor.class)))
                .thenReturn(createCallerLinkSelector(List.of(
                        new LinkKey(was1, nodeApplication)
                ), timestamp));

        //fromApplications out of Search range
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, List.of(was2), Collections.emptyList())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }


    /**
     * USER -> WAS
     */
    @Test
    public void selectLinkHistogramDataTest1() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final Application fromApplication = new Application("user", ServiceType.USER);
        final Application toApplication = new Application("WAS", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);


        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), eq(LinkDataMapProcessor.NO_OP), any(LinkDataMapProcessor.class)))
                .thenReturn(createCalleeLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, range);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS1 -> WAS2 (link)
     */
    @Test
    public void selectLinkHistogramDataTest2() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final Application fromApplication = new Application("was1", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("was2", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, range);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS -> UNKNOWN (link)
     */
    @Test
    public void selectLinkHistogramDataTest3() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final Application fromApplication = new Application("was", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("unknown", ServiceType.UNKNOWN);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, range);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS -> CACHE (link)
     */
    @Test
    public void selectLinkHistogramDataCacheTest() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        //CACHE_LIBRARY serviceTypeCode 8000 ~ 8299
        ServiceType cacheServiceType = ServiceTypeFactory.of(8299, "CACHE", "CACHE", ServiceTypeProperty.TERMINAL, ServiceTypeProperty.RECORD_STATISTICS);
        final Application fromApplication = new Application("was", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("cache", cacheServiceType);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, range);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        //link's target node Histogram schema(CACHE) should be FAST_SCHEMA
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.FAST_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS -> QUEUE (link)
     */
    @Test
    public void selectLinkHistogramDataQueueTest1() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final ServiceType queueServiceType = ServiceTypeFactory.of(7999, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application fromApplication = new Application("was", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("queue", queueServiceType);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), any(LinkDataMapProcessor.class)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, range);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * QUEUE -> WAS (link)
     */
    @Test
    public void selectLinkHistogramDataQueueTest2() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        //MESSAGE_BROKER serviceTypeCode 8300 ~ 8799
        final ServiceType queueServiceType = ServiceTypeFactory.of(8799, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application fromApplication = new Application("queue", queueServiceType);
        final Application toApplication = new Application("was", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), any(LinkDataMapProcessor.class)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, range);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    private LinkSelector createCallerLinkSelector(List<LinkKey> linkKeys, long timestamp) {
        return new LinkSelector() {
            @Override
            public LinkDataDuplexMap select(List<Application> sourceApplications, Range range, int callerSearchDepth, int calleeSearchDepth) {
                if (calleeSearchDepth > 0) {
                    throw new IllegalArgumentException("use UNIDIRECTIONAL link selector only. calleeSearchDepth: " + calleeSearchDepth);
                }

                LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
                for (LinkKey linkKey : linkKeys) {
                    linkDataDuplexMap.addTargetLinkData(createLinkData(linkKey.getFrom(), linkKey.getTo(), timestamp));
                }
                return linkDataDuplexMap;
            }

            @Override
            public LinkDataDuplexMap select(List<Application> sourceApplications, Range range, int callerSearchDepth, int calleeSearchDepth, boolean timeAggregated) {
                if (timeAggregated) {
                    throw new IllegalArgumentException("link histogram data require timeSeries data");
                }
                return select(sourceApplications, range, callerSearchDepth, calleeSearchDepth);
            }
        };
    }

    private LinkSelector createCalleeLinkSelector(List<LinkKey> linkKeys, long timestamp) {
        return new LinkSelector() {
            @Override
            public LinkDataDuplexMap select(List<Application> sourceApplications, Range range, int callerSearchDepth, int calleeSearchDepth) {
                if (callerSearchDepth > 0) {
                    throw new IllegalArgumentException("use UNIDIRECTIONAL link selector only. callerSearchDepth: " + callerSearchDepth);
                }

                LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
                for (LinkKey linkKey : linkKeys) {
                    linkDataDuplexMap.addSourceLinkData(createLinkData(linkKey.getFrom(), linkKey.getTo(), timestamp));
                }
                return linkDataDuplexMap;
            }

            @Override
            public LinkDataDuplexMap select(List<Application> sourceApplications, Range range, int callerSearchDepth, int calleeSearchDepth, boolean timeAggregated) {
                if (timeAggregated) {
                    throw new IllegalArgumentException("link histogram data require timeSeries data");
                }
                return select(sourceApplications, range, callerSearchDepth, calleeSearchDepth);
            }
        };
    }

    private LinkData createLinkData(Application fromApplication, Application toApplication, long timestamp) {
        LinkData linkData = new LinkData(fromApplication, toApplication);
        final ServiceType sourceServiceType = fromApplication.serviceType();
        final ServiceType destinationServiceType = toApplication.serviceType();
        final HistogramSchema schema = ServiceTypeCategory.findCategory(destinationServiceType.getCode()).getHistogramSchema();

        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getFastSlot().getSlotTime(), 1L);
        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getNormalSlot().getSlotTime(), 2L);
        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getSlowSlot().getSlotTime(), 3L);
        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getErrorSlot().getSlotTime(), 4L);
        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getSumStatSlot().getSlotTime(), 1000L);
        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getMaxStatSlot().getSlotTime(), 2000L);

        return linkData;
    }

    private void assertHistogramValues(Histogram histogram, long number) {
        Assertions.assertThat(histogram.getFastCount()).isEqualTo(number * 1L);
        Assertions.assertThat(histogram.getNormalCount()).isEqualTo(number * 2L);
        Assertions.assertThat(histogram.getSlowCount()).isEqualTo(number * 3L);
        Assertions.assertThat(histogram.getErrorCount()).isEqualTo(number * 4L);
        Assertions.assertThat(histogram.getSumElapsed()).isEqualTo(number * 1000L);
        if (number == 0L) {
            Assertions.assertThat(histogram.getMaxElapsed()).isEqualTo(0L);
        } else {
            Assertions.assertThat(histogram.getMaxElapsed()).isEqualTo(2000L);
        }
    }
}
