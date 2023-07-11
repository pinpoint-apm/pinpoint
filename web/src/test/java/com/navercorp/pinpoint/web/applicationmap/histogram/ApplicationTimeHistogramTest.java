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
import com.fasterxml.jackson.databind.ObjectWriter;
import com.navercorp.pinpoint.common.server.util.json.Jackson;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.view.TimeViewModel;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author emeroad
 */
public class ApplicationTimeHistogramTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ObjectMapper mapper = Jackson.newMapper();

    @Test
    public void testViewModel() throws IOException {

        Application app = new Application("test", ServiceType.STAND_ALONE);
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(app, Range.between(0, 10 * 6000));
        List<ResponseTime> responseHistogramList = createResponseTime(app);
        ApplicationTimeHistogram histogram = builder.build(responseHistogramList);

        List<TimeViewModel> viewModel = histogram.createViewModel(TimeHistogramFormat.V1);
        logger.debug("{}", viewModel);
        ObjectWriter writer = mapper.writer();
        String s = writer.writeValueAsString(viewModel);
        logger.debug(s);

    }

    private List<ResponseTime> createResponseTime(Application app) {

        ResponseTime one = new ResponseTime(app.getName(), app.getServiceType(), 0);
        one.addResponseTime("test", (short) 1000, 1);

        ResponseTime two = new ResponseTime(app.getName(), app.getServiceType(), 1000 * 60);
        two.addResponseTime("test", (short) 3000, 1);

        return List.of(one, two);
    }

    @Test
    public void testLoadViewModel() {
        Application app = new Application("test", ServiceType.STAND_ALONE);
        final long timestamp = System.currentTimeMillis();
        Range range = Range.between(timestamp, timestamp + 60000);

        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(app, range);

        ResponseTime responseTime = new ResponseTime(app.getName(), app.getServiceType(), timestamp);
        responseTime.addResponseTime("test", (short) 1000, 1);

        List<ResponseTime> responseHistogramList = List.of(responseTime);

        ApplicationTimeHistogram histogram = builder.build(responseHistogramList);

        List<TimeViewModel> viewModelList = histogram.createViewModel(TimeHistogramFormat.V2);
        logger.debug("{}", viewModelList);
    }
}
