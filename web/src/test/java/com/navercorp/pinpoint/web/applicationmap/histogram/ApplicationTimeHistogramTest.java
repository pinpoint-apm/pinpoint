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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogramBuilder;
import com.navercorp.pinpoint.web.view.ResponseTimeViewModel;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class ApplicationTimeHistogramTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testViewModel() throws IOException {

        Application app = new Application("test", ServiceType.STAND_ALONE);
        ApplicationTimeHistogramBuilder builder = new ApplicationTimeHistogramBuilder(app, new Range(0, 10*6000));
        List<ResponseTime> responseHistogramList = createResponseTime(app);
        ApplicationTimeHistogram histogram = builder.build(responseHistogramList);

        List<ResponseTimeViewModel> viewModel = histogram.createViewModel();
        logger.debug("{}", viewModel);
        ObjectWriter writer = mapper.writer();
        String s = writer.writeValueAsString(viewModel);
        logger.debug(s);

    }

    private List<ResponseTime> createResponseTime(Application app) {
        List<ResponseTime> responseTimeList = new ArrayList<ResponseTime>();

        ResponseTime one = new ResponseTime(app.getName(), app.getServiceType(), 0);
        one.addResponseTime("test", (short) 1000, 1);
        responseTimeList.add(one);

        ResponseTime two = new ResponseTime(app.getName(), app.getServiceType(), 1000*60);
        two .addResponseTime("test", (short) 3000, 1);
        responseTimeList.add(two);
        return responseTimeList;
    }
}
