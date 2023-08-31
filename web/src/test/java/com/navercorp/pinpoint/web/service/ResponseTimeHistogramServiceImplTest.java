package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeCategory;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerGroupListDataSource;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.link.LinkName;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.service.map.LinkSelector;
import com.navercorp.pinpoint.web.service.map.LinkSelectorFactory;
import com.navercorp.pinpoint.web.service.map.LinkSelectorType;
import com.navercorp.pinpoint.web.service.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
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
        ResponseTime responseTime = new ResponseTime(application.getName(), application.getServiceType(), timestamp);
        HistogramSchema schema = application.getServiceType().getHistogramSchema();
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
    public void selectNodeHistogramDataTes1() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final Application nodeApplication = new Application("user", ServiceType.USER);
        List<Application> toApplications = new ArrayList<>();
        toApplications.add(new Application("callee", ServiceType.STAND_ALONE));

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), eq(LinkDataMapProcessor.NO_OP), any(LinkDataMapProcessor.class)))
                .thenReturn(createTargetLinkSelector(nodeApplication, toApplications, timestamp));

        //User node use toApplications to build nodeHistogramData
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, Collections.emptyList(), toApplications)
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
        List<Application> fromApplications = new ArrayList<>();
        fromApplications.add(new Application("caller1", ServiceType.STAND_ALONE));
        fromApplications.add(new Application("caller2", ServiceType.STAND_ALONE));

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createSourceLinkSelector(fromApplications, nodeApplication, timestamp));

        //UNKNOWN(TERMINAL, ALIAS) node use toApplications to build nodeHistogramData
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, fromApplications, Collections.emptyList())
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
     * WAS ->
     * WAS -> UNKNOWN(target)
     */
    @Test
    public void selectNodeHistogramDataTest3() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final Application nodeApplication = new Application("unknown", ServiceType.UNKNOWN);
        List<Application> fromApplications = new ArrayList<>();
        fromApplications.add(new Application("caller1", ServiceType.STAND_ALONE));
        fromApplications.add(new Application("caller2", ServiceType.STAND_ALONE));

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createSourceLinkSelector(fromApplications, nodeApplication, timestamp));

        //UNKNOWN(TERMINAL, ALIAS) node use toApplications to build nodeHistogramData
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, fromApplications, Collections.emptyList())
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
        List<Application> fromApplications = new ArrayList<>();
        fromApplications.add(new Application("caller1", ServiceType.STAND_ALONE));

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createSourceLinkSelector(fromApplications, nodeApplication, timestamp));

        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, fromApplications, Collections.emptyList())
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
     * WAS ->
     * WAS -> QUEUE(target)
     * WAS ->
     */
    @Test
    public void selectNodeHistogramDataQueueTest1() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        //MESSAGE_BROKER serviceTypeCode 8300 ~ 8799
        final ServiceType queueServiceType = ServiceTypeFactory.of(7999, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application nodeApplication = new Application("cache", queueServiceType);
        List<Application> fromApplications = new ArrayList<>();
        fromApplications.add(new Application("caller1", ServiceType.STAND_ALONE));
        fromApplications.add(new Application("caller2", ServiceType.STAND_ALONE));
        fromApplications.add(new Application("caller3", ServiceType.STAND_ALONE));

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createSourceLinkSelector(fromApplications, nodeApplication, timestamp));

        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, fromApplications, Collections.emptyList())
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = service.selectNodeHistogramData(option);

        logger.debug(nodeHistogramSummary);
        NodeHistogram nodeHistogram = nodeHistogramSummary.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 3);
    }

    /**
     * WAS(out of range) -> QUEUE(target) -> WAS
     * <p>
     * different from serverMapV2
     * if queue has no fromApplications
     * check fromApplication(out of search range) using toApplications(caller link data)
     */
    @Test
    public void selectNodeHistogramDataQueueTest2() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        //MESSAGE_BROKER serviceTypeCode 8300 ~ 8799
        final ServiceType queueServiceType = ServiceTypeFactory.of(7999, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application nodeApplication = new Application("cache", queueServiceType);
        List<Application> hiddenFromApplications = new ArrayList<>();
        hiddenFromApplications.add(new Application("hiddenCaller1", ServiceType.STAND_ALONE));
        List<Application> queueCalleeApplications = new ArrayList<>();
        queueCalleeApplications.add(new Application("Callee1", ServiceType.STAND_ALONE));

        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), eq(LinkDataMapProcessor.NO_OP), any(LinkDataMapProcessor.class)))
                .thenReturn(createSourceLinkSelector(hiddenFromApplications, nodeApplication, timestamp));

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createSourceLinkSelector(hiddenFromApplications, nodeApplication, timestamp));

        //fromApplications out of Search range
        ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption.Builder(nodeApplication, range, Collections.emptyList(), queueCalleeApplications)
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
                .thenReturn(createTargetLinkSelector(fromApplication, List.of(toApplication), timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, range);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    /**
     * WAS -> WAS (link)
     */
    @Test
    public void selectLinkHistogramDataTest2() {
        ResponseTimeHistogramService service = new ResponseTimeHistogramServiceImpl(linkSelectorFactory, serverInstanceDatasourceService, mapResponseDao);

        final Application fromApplication = new Application("WAS1", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("WAS2", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createTargetLinkSelector(fromApplication, List.of(toApplication), timestamp));

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

        final Application fromApplication = new Application("WAS1", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("unknown", ServiceType.UNKNOWN);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createSourceLinkSelector(List.of(fromApplication), toApplication, timestamp));

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
        final Application fromApplication = new Application("WAS1", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("cache", cacheServiceType);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createSourceLinkSelector(List.of(fromApplication), toApplication, timestamp));

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
        final Application fromApplication = new Application("WAS", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("queue", queueServiceType);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createTargetLinkSelector(fromApplication, List.of(toApplication), timestamp));

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

        final ServiceType queueServiceType = ServiceTypeFactory.of(7999, "QUEUE", "QUEUE", ServiceTypeProperty.QUEUE, ServiceTypeProperty.RECORD_STATISTICS);
        final Application fromApplication = new Application("WAS", ServiceType.STAND_ALONE);
        final Application toApplication = new Application("queue", queueServiceType);
        final long timestamp = System.currentTimeMillis();
        final Range range = Range.between(timestamp, timestamp + 60000);

        when(linkSelectorFactory.createLinkSelector(eq(LinkSelectorType.UNIDIRECTIONAL), any(LinkDataMapProcessor.class), eq(LinkDataMapProcessor.NO_OP)))
                .thenReturn(createSourceLinkSelector(List.of(fromApplication), toApplication, timestamp));

        LinkHistogramSummary linkHistogramSummary = service.selectLinkHistogramData(fromApplication, toApplication, range);
        Histogram histogram = linkHistogramSummary.getHistogram();

        logger.debug(linkHistogramSummary);
        Assertions.assertThat(linkHistogramSummary.getLinkName()).isEqualTo(LinkName.of(fromApplication, toApplication));
        Assertions.assertThat(histogram.getHistogramSchema()).isEqualTo(BaseHistogramSchema.NORMAL_SCHEMA);
        assertHistogramValues(histogram, 1);
    }

    private LinkSelector createTargetLinkSelector(Application application, List<Application> toApplication, long timestamp) {
        return new LinkSelector() {
            @Override
            public LinkDataDuplexMap select(List<Application> sourceApplications, Range range, int callerSearchDepth, int calleeSearchDepth) {
                LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
                for (Application toApplication : toApplication) {
                    linkDataDuplexMap.addTargetLinkData(createLinkData(application, toApplication, timestamp));
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

    private LinkSelector createSourceLinkSelector(List<Application> fromApplications, Application application, long timestamp) {
        return new LinkSelector() {
            @Override
            public LinkDataDuplexMap select(List<Application> sourceApplications, Range range, int callerSearchDepth, int calleeSearchDepth) {
                LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();
                for (Application fromApplication : fromApplications) {
                    linkDataDuplexMap.addSourceLinkData(createLinkData(fromApplication, application, timestamp));
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
        final ServiceType sourceServiceType = fromApplication.getServiceType();
        final ServiceType destinationServiceType = toApplication.getServiceType();
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
