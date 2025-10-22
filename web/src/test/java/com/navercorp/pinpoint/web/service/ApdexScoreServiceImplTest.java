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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.timeseries.time.DateTimeUtils;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.AgentResponse;
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
    private TimeWindow timeWindow;

    @BeforeEach
    public void mockResponseDao() {
        Instant endTimestamp = DateTimeUtils.epochMilli().truncatedTo(ChronoUnit.MINUTES);
        testRange = Range.between(endTimestamp.minus(Duration.ofMinutes(5)), endTimestamp);
        timeWindow = new TimeWindow(testRange);

        List<ResponseTime> responseTimeList = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            Instant timestamp = endTimestamp.minus(Duration.ofMinutes(i));
            responseTimeList.add(createResponseTime(timestamp.toEpochMilli()));
        }

        MapAgentResponseDao mapAgentResponseDao = mock(MapAgentResponseDao.class);
        when(mapAgentResponseDao.selectResponseTime(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        when(mapAgentResponseDao.selectResponseTime(ArgumentMatchers.eq(testApplication), ArgumentMatchers.any())).thenReturn(responseTimeList);

        MapResponseDao mapResponseDao = mock(MapResponseDao.class);
        // ApplicationResponse -----------
        ApplicationResponse.Builder emptyBuilder = ApplicationResponse.newBuilder(testApplication);
        ApplicationResponse empty = emptyBuilder.build();
        when(mapResponseDao.selectApplicationResponse(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(empty);

        ApplicationResponse.Builder builder = ApplicationResponse.newBuilder(testApplication);
        for (ResponseTime responseTime : responseTimeList) {
            Histogram histogram = responseTime.getApplicationResponseHistogram();
            builder.addResponseTime(responseTime.getApplicationName(), responseTime.getTimeStamp(), histogram);
        }

        when(mapResponseDao.selectApplicationResponse(ArgumentMatchers.eq(testApplication), ArgumentMatchers.any())).thenReturn(builder.build());

        // AgentResponse -----------
        AgentResponse.Builder agentBuilder = AgentResponse.newBuilder(testApplication);
        agentBuilder.addAgentResponse(responseTimeList);
        when(mapAgentResponseDao.selectAgentResponse(ArgumentMatchers.eq(testApplication), ArgumentMatchers.any())).thenReturn(agentBuilder.build());

        apdexScoreService = new ApdexScoreServiceImpl(mapAgentResponseDao, mapResponseDao);
    }

    private ResponseTime createResponseTime(long timeStamp) {
        ResponseTime.Builder responseTimeBuilder = ResponseTime.newBuilder(testApplication.getName(), testApplication.getServiceType(), timeStamp);
        for (String agentId : agentIdList) {
            responseTimeBuilder.addResponseTime(agentId, createTestHistogram(1, 2, 3, 4, 5));
        }
        return responseTimeBuilder.build();
    }

    private Histogram createTestHistogram(long fast, long normal, long slow, long verySlow, long error) {
        Histogram histogram = new Histogram(ServiceType.TEST);
        HistogramSchema schema = histogram.getHistogramSchema();

        histogram.addCallCount(schema.getFastSlot().getSlotTime(), fast);
        histogram.addCallCount(schema.getNormalSlot().getSlotTime(), normal);
        histogram.addCallCount(schema.getSlowSlot().getSlotTime(), slow);
        histogram.addCallCount(schema.getVerySlowSlot().getSlotTime(), verySlow);
        histogram.addCallCount(schema.getSlowErrorSlot().getSlotTime(), error);
        return histogram;
    }

    @Test
    public void selectApplicationApdexScoreData() {
        ApdexScore apdexScore = apdexScoreService.selectApdexScoreData(testApplication, timeWindow);

        assertThat(apdexScore.getApdexScore()).isGreaterThan(0);
    }

    @Test
    public void selectNonWasApplicationApexScoreData() {
        ApdexScore apdexScore = apdexScoreService.selectApdexScoreData(new Application("nonWas", ServiceType.USER), timeWindow);

        assertThat(apdexScore.getApdexScore()).isEqualTo(0);
    }

    @Test
    public void selectNonExistingApplicationApexScoreData() {
        ApdexScore apdexScore = apdexScoreService.selectApdexScoreData(new Application("nonExisting", ServiceType.STAND_ALONE), timeWindow);

        assertThat(apdexScore.getApdexScore()).isEqualTo(0);
    }

    @Test
    public void selectAgentApexScoreData() {
        ApdexScore apdexScore = apdexScoreService.selectApdexScoreData(testApplication, agentIdList.get(0), timeWindow);

        assertThat(apdexScore.getApdexScore()).isGreaterThan(0);
    }

    @Test
    public void selectNonExistingAgentApexScoreData() {
        ApdexScore apdexScore = apdexScoreService.selectApdexScoreData(testApplication, "nonExistingAgentId", timeWindow);

        assertThat(apdexScore.getApdexScore()).isEqualTo(0);
    }
}
