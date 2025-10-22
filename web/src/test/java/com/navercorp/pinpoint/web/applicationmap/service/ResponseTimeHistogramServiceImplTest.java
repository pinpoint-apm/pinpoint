/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.service;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchemas;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeCategory;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.web.applicationmap.appender.server.DefaultServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerGroupListDataSource;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResponseTimeHistogramServiceImplTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Mock
    private LinkSelectorFactory linkSelectorFactory;

    @Mock
    private ServerInstanceDatasourceService serverInstanceDatasourceService;

    @Mock
    private MapAgentResponseDao mapAgentResponseDao;

    @Mock
    private MapResponseDao mapResponseDao;

    private NodeHistogramService nodeHistogramService;

    @BeforeEach
    public void SetUp() {

        ServerGroupListDataSource dataSource = new ServerGroupListDataSource() {
            @Override
            public ServerGroupList createServerGroupList(Node node, long timestamp) {
                return ServerGroupList.empty();
            }
        };

        lenient().when(serverInstanceDatasourceService.getGroupServerFactory(anyBoolean()))
                .thenReturn(new DefaultServerGroupListFactory(dataSource));

        nodeHistogramService = new NodeHistogramServiceImpl(mapAgentResponseDao, mapResponseDao);
    }

    private @NotNull ResponseTimeHistogramService newResponseService() {
        return new ResponseTimeHistogramServiceImpl(linkSelectorFactory, nodeHistogramService, serverInstanceDatasourceService, mapAgentResponseDao);
    }

    /**
     * empty WAS(target)
     */
    @Test
    public void selectNodeHistogramEmptyWASDataTest() {
        ResponseTimeHistogramService service = newResponseService();

        final Application nodeApplication = new Application("WAS", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        TimeWindow timeWindow = new TimeWindow(range);

        when(mapAgentResponseDao.selectResponseTime(eq(nodeApplication), any(TimeWindow.class))).thenReturn(List.of());

        //WAS node does not use fromApplications or toApplications to build nodeHistogramData
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, timeWindow, List.of(), List.of())
                .setUseStatisticsAgentState(true)
                .build();

        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug("{}", nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 0);
    }

    /**
     * WAS(target)
     */
    @Test
    public void selectNodeHistogramWASDataTest() {
        ResponseTimeHistogramService service = newResponseService();

        final Application nodeApplication = new Application("WAS", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        TimeWindow timeWindow = new TimeWindow(range);

        when(mapAgentResponseDao.selectResponseTime(eq(nodeApplication), any(TimeWindow.class))).thenReturn(List.of(createResponseTime(nodeApplication, timestamp)));

        //WAS node does not use fromApplications or toApplications to build nodeHistogramData
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, timeWindow, List.of(), List.of())
                .setUseStatisticsAgentState(true)
                .build();

        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug("${}", nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    private ResponseTime createResponseTime(Application application, long timestamp) {
        ResponseTime.Builder responseTimeBuilder = ResponseTime.newBuilder(application.getName(), application.getServiceType(), timestamp);
        HistogramSchema schema = application.getServiceType().getHistogramSchema();
        responseTimeBuilder.addResponseTime("agentId", schema.getFastSlot().getSlotTime(), 1L);
        responseTimeBuilder.addResponseTime("agentId", schema.getNormalSlot().getSlotTime(), 2L);
        responseTimeBuilder.addResponseTime("agentId", schema.getSlowSlot().getSlotTime(), 3L);
        responseTimeBuilder.addResponseTime("agentId", schema.getSlowErrorSlot().getSlotTime(), 4L);
        responseTimeBuilder.addResponseTime("agentId", schema.getSumStatSlot().getSlotTime(), 1000L);
        responseTimeBuilder.addResponseTime("agentId", schema.getMaxStatSlot().getSlotTime(), 2000L);
        return responseTimeBuilder.build();
    }

    /**
     * USER(target) -> WAS
     */
    @Test
    public void selectNodeHistogramDataTest1() {
        ResponseTimeHistogramService service = newResponseService();

        final Application nodeApplication = new Application("user", ServiceType.USER);
        final Application toApplication = new Application("was1", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        TimeWindow timeWindow = new TimeWindow(range);

        //User node use toApplications(was1) to build histogramData
        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), eq(LinkDataMapProcessor.NO_OP), any(LinkDataMapProcessor.class)))
                .thenReturn(createCalleeLinkSelector(List.of(new LinkKey(nodeApplication, toApplication)), timestamp));

        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, timeWindow, List.of(), List.of(toApplication))
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS -> UNKNOWN(target)
     */
    @Test
    public void selectNodeHistogramDataTest2() {
        ResponseTimeHistogramService service = newResponseService();

        final Application nodeApplication = new Application("unknown", ServiceType.UNKNOWN);
        final Application was = new Application("was", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        TimeWindow timeWindow = new TimeWindow(range);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(was, nodeApplication)), timestamp));

        //UNKNOWN(TERMINAL, ALIAS) node use fromApplications to build nodeHistogramData
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, timeWindow, List.of(was), List.of())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS1 ->
     * WAS2 -> UNKNOWN(target)
     */
    @Test
    public void selectNodeHistogramDataTest3() {
        ResponseTimeHistogramService service = newResponseService();

        final Application nodeApplication = new Application("unknown", ServiceType.UNKNOWN);
        final Application was1 = new Application("was1", ServiceType.STAND_ALONE);
        final Application was2 = new Application("was2", ServiceType.STAND_ALONE);


        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        TimeWindow timeWindow = new TimeWindow(range);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(
                        new LinkKey(was1, nodeApplication),
                        new LinkKey(was2, nodeApplication)
                ), timestamp));

        //UNKNOWN(TERMINAL, ALIAS) node use toApplications to build nodeHistogramData
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, timeWindow, List.of(was1, was2), List.of())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        //two WAS node -> UNKNOWN node
        assertHistogramValues(histogram, 2);
    }


    /**
     * WAS -> CACHE_LIBRARY(target)
     */
    @Test
    public void selectNodeHistogramDataCacheTest() {
        ResponseTimeHistogramService service = newResponseService();

        //CACHE_LIBRARY serviceTypeCode 8000 ~ 8299
        ServiceType cacheServiceType = ServiceTypeFactory.of(8299, "CACHE", "CACHE", ServiceTypeProperty.TERMINAL, ServiceTypeProperty.RECORD_STATISTICS);
        final Application nodeApplication = new Application("cache", cacheServiceType);
        final Application was = new Application("was", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        TimeWindow timeWindow = new TimeWindow(range);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(was, nodeApplication)), timestamp));

        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, timeWindow, List.of(was), List.of())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        //CACHE Node's Histogram schema should be FAST_SCHEMA
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.FAST_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS -> QUEUE(target)
     */
    @Test
    public void selectNodeHistogramDataQueueTest1() {
        ResponseTimeHistogramService service = newResponseService();

        //MESSAGE_BROKER serviceTypeCode 8300 ~ 8799
        final ServiceType queueServiceType = ServiceTypeFactory.of(8799, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application nodeApplication = new Application("queue", queueServiceType);
        final Application was1 = new Application("was1", ServiceType.STAND_ALONE);
        final Application was2 = new Application("was2", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        TimeWindow timeWindow = new TimeWindow(range);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), any(LinkDataMapProcessor.class)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(was1, nodeApplication)), timestamp));

        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, timeWindow, List.of(was1, was2), List.of())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS(out of range) -> QUEUE(target) -> ...
     * <p>
     * ignore Application not in range of serverMap search range
     */
    @Test
    public void selectNodeHistogramDataQueueTest2() {
        ResponseTimeHistogramService service = newResponseService();

        //MESSAGE_BROKER serviceTypeCode 8300 ~ 8799
        final ServiceType queueServiceType = ServiceTypeFactory.of(8799, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application nodeApplication = new Application("queue", queueServiceType);
        final Application was = new Application("was", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        TimeWindow timeWindow = new TimeWindow(range);

        //with no source Application do not scan
//        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), any(LinkDataMapProcessor.class)))
//                .thenThrow(new IllegalStateException("no scan for QUEUE node with empty sourceApplications"));

        //fromApplications out of Search range
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, timeWindow, List.of(), List.of())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
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
        ResponseTimeHistogramService service = newResponseService();

        //MESSAGE_BROKER serviceTypeCode 8300 ~ 8799
        final ServiceType queueServiceType = ServiceTypeFactory.of(8799, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application nodeApplication = new Application("queue", queueServiceType);
        final Application was1 = new Application("was1", ServiceType.STAND_ALONE);
        final Application was2 = new Application("was2", ServiceType.STAND_ALONE);

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        TimeWindow timeWindow = new TimeWindow(range);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), any(LinkDataMapProcessor.class)))
                .thenReturn(createCallerLinkSelector(List.of(
                        new LinkKey(was1, nodeApplication)
                ), timestamp));

        //fromApplications out of Search range
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, timeWindow, List.of(was2), List.of())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }


    /**
     * USER -> WAS
     */
    @Test
    public void selectLinkHistogramDataTest1() {
        ResponseTimeHistogramService service = newResponseService();

        final Application fromApplication = new Application("user", ServiceType.USER);
        final Application toApplication = new Application("WAS", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        final TimeWindow timeWindow = new TimeWindow(range);


        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), eq(LinkDataMapProcessor.NO_OP), any(LinkDataMapProcessor.class)))
                .thenReturn(createCalleeLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, timeWindow);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }



    /**
     * WAS1 -> WAS2 (link)
     */
    @Test
    public void selectLinkHistogramDataTest2() {
        ResponseTimeHistogramService service = newResponseService();

        final Application fromApplication = new Application("was1", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("was2", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        final TimeWindow timeWindow = new TimeWindow(range);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, timeWindow);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS -> UNKNOWN (link)
     */
    @Test
    public void selectLinkHistogramDataTest3() {
        ResponseTimeHistogramService service = newResponseService();

        final Application fromApplication = new Application("was", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("unknown", ServiceType.UNKNOWN);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        final TimeWindow timeWindow = new TimeWindow(range);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, timeWindow);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS -> CACHE (link)
     */
    @Test
    public void selectLinkHistogramDataCacheTest() {
        ResponseTimeHistogramService service = newResponseService();

        //CACHE_LIBRARY serviceTypeCode 8000 ~ 8299
        ServiceType cacheServiceType = ServiceTypeFactory.of(8299, "CACHE", "CACHE", ServiceTypeProperty.TERMINAL, ServiceTypeProperty.RECORD_STATISTICS);
        final Application fromApplication = new Application("was", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("cache", cacheServiceType);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        final TimeWindow timeWindow = new TimeWindow(range);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, timeWindow);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        //link's target node Histogram schema(CACHE) should be FAST_SCHEMA
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.FAST_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS -> QUEUE (link)
     */
    @Test
    public void selectLinkHistogramDataQueueTest1() {
        ResponseTimeHistogramService service = newResponseService();

        final ServiceType queueServiceType = ServiceTypeFactory.of(7999, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application fromApplication = new Application("was", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("queue", queueServiceType);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        final TimeWindow timeWindow = new TimeWindow(range);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), any(LinkDataMapProcessor.class)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, timeWindow);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * QUEUE -> WAS (link)
     */
    @Test
    public void selectLinkHistogramDataQueueTest2() {
        ResponseTimeHistogramService service = newResponseService();

        //MESSAGE_BROKER serviceTypeCode 8300 ~ 8799
        final ServiceType queueServiceType = ServiceTypeFactory.of(8799, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application fromApplication = new Application("queue", queueServiceType);
        final Application toApplication = new Application("was", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);
        final TimeWindow timeWindow = new TimeWindow(range);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), any(LinkDataMapProcessor.class)))
                .thenReturn(createCallerLinkSelector(List.of(new LinkKey(fromApplication, toApplication)), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, timeWindow);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(HistogramSchemas.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    private LinkSelector createCallerLinkSelector(List<LinkKey> linkKeys, long timestamp) {
        return new LinkSelector() {
            @Override
            public LinkDataDuplexMap select(List<Application> sourceApplications, TimeWindow timeWindow, int callerSearchDepth, int calleeSearchDepth) {
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
            public LinkDataDuplexMap select(List<Application> sourceApplications, TimeWindow timeWindow, int callerSearchDepth, int calleeSearchDepth, boolean timeAggregated) {
                if (timeAggregated) {
                    throw new IllegalArgumentException("link histogram data require timeSeries data");
                }
                return select(sourceApplications, timeWindow, callerSearchDepth, calleeSearchDepth);
            }
        };
    }

    private LinkSelector createCalleeLinkSelector(List<LinkKey> linkKeys, long timestamp) {
        return new LinkSelector() {
            @Override
            public LinkDataDuplexMap select(List<Application> sourceApplications, TimeWindow timeWindow, int callerSearchDepth, int calleeSearchDepth) {
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
            public LinkDataDuplexMap select(List<Application> sourceApplications, TimeWindow timeWindow, int callerSearchDepth, int calleeSearchDepth, boolean timeAggregated) {
                if (timeAggregated) {
                    throw new IllegalArgumentException("link histogram data require timeSeries data");
                }
                return select(sourceApplications, timeWindow, callerSearchDepth, calleeSearchDepth);
            }
        };
    }

    private LinkData createLinkData(Application fromApplication, Application toApplication, long timestamp) {
        LinkData linkData = new LinkData(fromApplication, toApplication);
        final ServiceType sourceServiceType = fromApplication.getServiceType();
        final ServiceType destinationServiceType = toApplication.getServiceType();
        final HistogramSchema schema = ServiceTypeCategory.findCategory(destinationServiceType.getCode()).getHistogramSchema();

        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getFastSlot().getSlotTime(), 1L);
        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getNormalSlot().getSlotTime(), 2L);
        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getSlowSlot().getSlotTime(), 3L);
        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getSlowErrorSlot().getSlotTime(), 4L);
        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getSumStatSlot().getSlotTime(), 1000L);
        linkData.addLinkData("sourceAgentId", sourceServiceType, "destinationAgentId", destinationServiceType, timestamp, schema.getMaxStatSlot().getSlotTime(), 2000L);

        return linkData;
    }

    private void assertHistogramValues(Histogram histogram, long number) {
        Assertions.assertThat(histogram.getFastCount()).isEqualTo(number * 1L);
        Assertions.assertThat(histogram.getNormalCount()).isEqualTo(number * 2L);
        Assertions.assertThat(histogram.getSlowCount()).isEqualTo(number * 3L);
        Assertions.assertThat(histogram.getSlowErrorCount()).isEqualTo(number * 4L);
        Assertions.assertThat(histogram.getSumElapsed()).isEqualTo(number * 1000L);
        if (number == 0L) {
            Assertions.assertThat(histogram.getMaxElapsed()).isEqualTo(0L);
        } else {
            Assertions.assertThat(histogram.getMaxElapsed()).isEqualTo(2000L);
        }
    }
}
