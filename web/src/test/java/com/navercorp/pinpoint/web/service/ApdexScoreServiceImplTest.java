package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.time.DateTimeUtils;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApdexScoreServiceImplTest {

    private final Application testApplication = new Application("testApplication", ServiceType.STAND_ALONE);
    private final List<String> agentIdList = List.of("agentId1", "agentId2", "agentId3");

    private ApdexScoreServiceImpl apdexScoreService;
    private Range testRange;

    @BeforeEach
    public void mockResponseDao() {
        Instant endTimestamp = DateTimeUtils.epochMilli().truncatedTo(ChronoUnit.MINUTES);
        testRange = Range.between(endTimestamp.minus(Duration.ofMinutes(5)), endTimestamp);

        List<ResponseTime> responseTimeList = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            Instant timestamp = endTimestamp.minus(Duration.ofMinutes(i));
            responseTimeList.add(createResponseTime(timestamp.toEpochMilli()));
        }

        MapResponseDao mapResponseDao = mock(MapResponseDao.class);
        when(mapResponseDao.selectResponseTime(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        when(mapResponseDao.selectResponseTime(ArgumentMatchers.eq(testApplication), ArgumentMatchers.any())).thenReturn(responseTimeList);

        apdexScoreService = new ApdexScoreServiceImpl(mapResponseDao);
    }

    private ResponseTime createResponseTime(long timeStamp) {
        ResponseTime responseTime = new ResponseTime(testApplication.name(), testApplication.serviceType(), timeStamp);
        for (String agentId : agentIdList) {
            responseTime.addResponseTime(agentId, createTestHistogram(1, 2, 3, 4, 5));
        }
        return responseTime;
    }

    private Histogram createTestHistogram(long fast, long normal, long slow, long verySlow, long error) {
        Histogram histogram = new Histogram(ServiceType.TEST);
        HistogramSchema schema = histogram.getHistogramSchema();

        histogram.addCallCount(schema.getFastSlot().getSlotTime(), fast);
        histogram.addCallCount(schema.getNormalSlot().getSlotTime(), normal);
        histogram.addCallCount(schema.getSlowSlot().getSlotTime(), slow);
        histogram.addCallCount(schema.getVerySlowSlot().getSlotTime(), verySlow);
        histogram.addCallCount(schema.getErrorSlot().getSlotTime(), error);
        return histogram;
    }

    @Test
    public void selectApplicationApdexScoreData() {
        ApdexScore apdexScore = apdexScoreService.selectApdexScoreData(testApplication, testRange);

        assertThat(apdexScore.getApdexScore()).isGreaterThan(0);
    }

    @Test
    public void selectNonWasApplicationApexScoreData() {
        ApdexScore apdexScore = apdexScoreService.selectApdexScoreData(new Application("nonWas", ServiceType.USER), testRange);

        assertThat(apdexScore.getApdexScore()).isEqualTo(0);
    }

    @Test
    public void selectNonExistingApplicationApexScoreData() {
        ApdexScore apdexScore = apdexScoreService.selectApdexScoreData(new Application("nonExisting", ServiceType.STAND_ALONE), testRange);

        assertThat(apdexScore.getApdexScore()).isEqualTo(0);
    }

    @Test
    public void selectAgentApexScoreData() {
        ApdexScore apdexScore = apdexScoreService.selectApdexScoreData(testApplication, agentIdList.get(0), testRange);

        assertThat(apdexScore.getApdexScore()).isGreaterThan(0);
    }

    @Test
    public void selectNonExistingAgentApexScoreData() {
        ApdexScore apdexScore = apdexScoreService.selectApdexScoreData(testApplication, "nonExistingAgentId", testRange);

        assertThat(apdexScore.getApdexScore()).isEqualTo(0);
    }
}
