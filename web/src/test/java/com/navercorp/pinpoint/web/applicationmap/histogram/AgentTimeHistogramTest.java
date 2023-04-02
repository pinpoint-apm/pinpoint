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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.view.AgentResponseTimeViewModel;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import com.navercorp.pinpoint.web.vo.stat.SampledApdexScore;
import com.navercorp.pinpoint.web.vo.stat.chart.application.DoubleApplicationStatPoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author emeroad
 */
public class AgentTimeHistogramTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testViewModel() throws IOException {

        Application app = new Application("test", ServiceType.STAND_ALONE);
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(app, Range.between(0, 1000 * 60));
        List<ResponseTime> responseHistogramList = createResponseTime(app, "test1", "test2");
        AgentTimeHistogram histogram = builder.build(responseHistogramList);

        List<AgentResponseTimeViewModel> viewModel = histogram.createViewModel(TimeHistogramFormat.V1);
        logger.debug("{}", viewModel);

        JsonFactory jsonFactory = mapper.getFactory();
        StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);
        jsonGenerator.writeStartObject();
        for (AgentResponseTimeViewModel agentResponseTimeViewModel : viewModel) {
            jsonGenerator.writeObject(agentResponseTimeViewModel);
        }
        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        jsonGenerator.close();
        logger.debug(stringWriter.toString());

    }

    @Test
    public void getSampledAgentApdexScoreListTest() {
        Application app = new Application("test", ServiceType.STAND_ALONE);
        Range range = Range.between(0, 1000 * 60);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(app, range, timeWindow);
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
        Range range = Range.between(0, 1000 * 60);
        TimeWindow timeWindow = new TimeWindow(range, new TimeWindowSlotCentricSampler());
        AgentTimeHistogramBuilder builder = new AgentTimeHistogramBuilder(app, range, timeWindow);
        List<ResponseTime> responseHistogramList = createResponseTime(app, "test5", "test6");
        AgentTimeHistogram histogram = builder.build(responseHistogramList);

        List<DoubleApplicationStatPoint> applicationStatPointList = histogram.getApplicationApdexScoreList(timeWindow);
        assertThat(applicationStatPointList).hasSize(2);
        Assertions.assertEquals(applicationStatPointList.get(0).getXVal(), 0);
        Assertions.assertEquals(applicationStatPointList.get(0).getYValForAvg(), 1.0, 0.001);
        Assertions.assertEquals(applicationStatPointList.get(1).getXVal(), 1000 * 60);
        Assertions.assertEquals(applicationStatPointList.get(1).getYValForAvg(), 0.5, 0.001);
    }

    private List<ResponseTime> createResponseTime(Application app, String agentName1, String agentName2) {

        ResponseTime one = new ResponseTime(app.getName(), app.getServiceType(), 0);
        one.addResponseTime(agentName1, (short) 1000, 1);

        ResponseTime two = new ResponseTime(app.getName(), app.getServiceType(), 1000 * 60);
        two.addResponseTime(agentName1, (short) 3000, 1);

        ResponseTime three = new ResponseTime(app.getName(), app.getServiceType(), 0);
        three.addResponseTime(agentName2, (short) 1000, 1);

        ResponseTime four = new ResponseTime(app.getName(), app.getServiceType(), 1000 * 60);
        four.addResponseTime(agentName2, (short) 3000, 1);

        return List.of(one, two, three, four);
    }

}
