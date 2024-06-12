/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.view.TimeViewModel;
import com.navercorp.pinpoint.web.view.id.AgentNameView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.application.DoubleApplicationStatPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author emeroad
 */
public class AgentTimeHistogramTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper mapper = Jackson.newMapper();

    @Test
    public void testViewModel() throws IOException {

        Application app = new Application("test", ServiceType.STAND_ALONE);
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(app, Range.between(0, 1000 * 60));
        List<ResponseTime> responseHistogramList = createResponseTime(app, "test1", "test2");
        AgentTimeHistogram histogram = builder.build(responseHistogramList);

        JsonFields<AgentNameView, List<TimeViewModel>> viewModel = histogram.createViewModel(TimeHistogramFormat.V1);
        logger.debug("{}", viewModel);

        String json = mapper.writeValueAsString(viewModel);
        logger.debug(json);
    }

    @Test
    public void getSampledAgentApdexScoreListTest() {
        Application app = new Application("test", ServiceType.STAND_ALONE);
        Range range = Range.between(0L, 1000L * 60);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(app, timeWindow);
        List<ResponseTime> responseHistogramList = createResponseTime(app, "test3", "test4");
        AgentTimeHistogram histogram = builder.build(responseHistogramList);

        List<SampledApdexScore> sampledApdexScore1 = histogram.getSampledAgentApdexScoreList("test3");
        assertThat(sampledApdexScore1).hasSize(2);

        List<SampledApdexScore> sampledApdexScore2 = histogram.getSampledAgentApdexScoreList("test4");
        assertThat(sampledApdexScore2).hasSize(2);

        List<SampledApdexScore> wrongSampledApdexScore = histogram.getSampledAgentApdexScoreList("wrongAgentName");
        assertThat(wrongSampledApdexScore).hasSize(0);
    }

    @Test
    public void getApplicationApdexScoreListTest() {
        Application app = new Application("test", ServiceType.STAND_ALONE);
        Range range = Range.between(0L, 1000L * 60);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(app, timeWindow);
        List<ResponseTime> responseHistogramList = createResponseTime(app, "test5", "test6");
        AgentTimeHistogram histogram = builder.build(responseHistogramList);

        List<DoubleApplicationStatPoint> applicationStatPointList = histogram.getApplicationApdexScoreList(timeWindow);
        assertThat(applicationStatPointList).hasSize(2);
        assertThat(applicationStatPointList.get(0))
                .extracting(DoubleApplicationStatPoint::getXVal, DoubleApplicationStatPoint::getYValForAvg)
                .containsExactly(0L, 1.0);
        assertThat(applicationStatPointList.get(1))
                .extracting(DoubleApplicationStatPoint::getXVal, DoubleApplicationStatPoint::getYValForAvg)
                .containsExactly(1000L * 60, 0.5);
    }

    private List<ResponseTime> createResponseTime(Application app, String agentName1, String agentName2) {

        ResponseTime one = new ResponseTime(app.name(), app.serviceType(), 0);
        one.addResponseTime(agentName1, (short) 1000, 1);

        ResponseTime two = new ResponseTime(app.name(), app.serviceType(), 1000 * 60);
        two.addResponseTime(agentName1, (short) 3000, 1);

        ResponseTime three = new ResponseTime(app.name(), app.serviceType(), 0);
        three.addResponseTime(agentName2, (short) 1000, 1);

        ResponseTime four = new ResponseTime(app.name(), app.serviceType(), 1000 * 60);
        four.addResponseTime(agentName2, (short) 3000, 1);

        return List.of(one, two, three, four);
    }

    @Test
    public void aggregatedLinkDataHandleTest() {
        final long timestamp = System.currentTimeMillis();
        final long minute = 60000;
        final long aggregateTimeStamp = 0;
        Application app = new Application("test", ServiceType.STAND_ALONE);
        List<String> sourceAgentIdList = List.of("sourceAgentId1", "sourceAgentId2");
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(app, Range.between(timestamp, timestamp + minute));

        LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
        LinkCallDataMap aggregatedLinkCallDataMap = new LinkCallDataMap();
        for (String sourceAgentId : sourceAgentIdList) {
            linkCallDataMap.addLinkDataMap(createSourceLinkCallDataMap(sourceAgentId, timestamp));
            linkCallDataMap.addLinkDataMap(createSourceLinkCallDataMap(sourceAgentId, timestamp + minute));
            aggregatedLinkCallDataMap.addLinkDataMap(createSourceLinkCallDataMap(sourceAgentId, aggregateTimeStamp));
            aggregatedLinkCallDataMap.addLinkDataMap(createSourceLinkCallDataMap(sourceAgentId, aggregateTimeStamp));
        }
        AgentTimeHistogram agentTimeHistogram = builder.buildSource(linkCallDataMap);
        AgentTimeHistogram aggregatedAgentTimeHistogram = builder.buildSource(aggregatedLinkCallDataMap);

        Map<String, Histogram> defaultResult = new HashMap<>();
        Map<String, Histogram> aggregatedResult = new HashMap<>();
        for (String sourceAgentId : sourceAgentIdList) {
            for (TimeHistogram timeHistogram : agentTimeHistogram.getTimeHistogramMap().get(sourceAgentId)) {
                Histogram histogram = defaultResult.computeIfAbsent(sourceAgentId, k -> new Histogram(ServiceType.STAND_ALONE));
                histogram.add(timeHistogram);
            }

            for (TimeHistogram timeHistogram : aggregatedAgentTimeHistogram.getTimeHistogramMap().get(sourceAgentId)) {
                Histogram histogram = aggregatedResult.computeIfAbsent(sourceAgentId, k -> new Histogram(ServiceType.STAND_ALONE));
                histogram.add(timeHistogram);
            }
        }

        for (String sourceAgentId : sourceAgentIdList) {
            assertThat(aggregatedResult.get(sourceAgentId).getFastCount()).isEqualTo(defaultResult.get(sourceAgentId).getFastCount());
            assertThat(aggregatedResult.get(sourceAgentId).getNormalCount()).isEqualTo(defaultResult.get(sourceAgentId).getNormalCount());
            assertThat(aggregatedResult.get(sourceAgentId).getSlowCount()).isEqualTo(defaultResult.get(sourceAgentId).getSlowCount());
            assertThat(aggregatedResult.get(sourceAgentId).getErrorCount()).isEqualTo(defaultResult.get(sourceAgentId).getErrorCount());
            assertThat(aggregatedResult.get(sourceAgentId).getSumElapsed()).isEqualTo(defaultResult.get(sourceAgentId).getSumElapsed());
            assertThat(aggregatedResult.get(sourceAgentId).getMaxElapsed()).isEqualTo(defaultResult.get(sourceAgentId).getMaxElapsed());
        }
    }

    private LinkCallDataMap createSourceLinkCallDataMap(String sourceAgentId, long timeStamp) {
        LinkCallDataMap linkCallDataMap = new LinkCallDataMap();

        final HistogramSchema schema = BaseHistogramSchema.NORMAL_SCHEMA;
        linkCallDataMap.addCallData(sourceAgentId, ServiceType.STAND_ALONE, "targetAgentId", ServiceType.STAND_ALONE, timeStamp, schema.getFastSlot().getSlotTime(), 1L);
        linkCallDataMap.addCallData(sourceAgentId, ServiceType.STAND_ALONE, "targetAgentId", ServiceType.STAND_ALONE, timeStamp, schema.getNormalSlot().getSlotTime(), 2L);
        linkCallDataMap.addCallData(sourceAgentId, ServiceType.STAND_ALONE, "targetAgentId", ServiceType.STAND_ALONE, timeStamp, schema.getSlowErrorSlot().getSlotTime(), 3L);
        linkCallDataMap.addCallData(sourceAgentId, ServiceType.STAND_ALONE, "targetAgentId", ServiceType.STAND_ALONE, timeStamp, schema.getErrorSlot().getSlotTime(), 4L);
        linkCallDataMap.addCallData(sourceAgentId, ServiceType.STAND_ALONE, "targetAgentId", ServiceType.STAND_ALONE, timeStamp, schema.getSumStatSlot().getSlotTime(), 1000L);
        linkCallDataMap.addCallData(sourceAgentId, ServiceType.STAND_ALONE, "targetAgentId", ServiceType.STAND_ALONE, timeStamp, schema.getMaxStatSlot().getSlotTime(), 2000L);
        return linkCallDataMap;
    }

}
