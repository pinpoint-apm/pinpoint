package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.view.ResponseTimeViewModel;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class ApplicationTimeSeriesHistogramTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testViewModel() throws IOException {

        Application app = new Application("test", ServiceType.TOMCAT);
        ApplicationTimeSeriesHistogramBuilder builder = new ApplicationTimeSeriesHistogramBuilder(app, new Range(0, 10*6000));
        List<ResponseTime> responseHistogramList = createResponseTime(app);
        ApplicationTimeSeriesHistogram histogram = builder.build(responseHistogramList);

        List<ResponseTimeViewModel> viewModel = histogram.createViewModel();
        logger.debug("{}", viewModel);
        ObjectWriter writer = mapper.writer();
        String s = writer.writeValueAsString(viewModel);
        logger.debug(s);

    }

    private List<ResponseTime> createResponseTime(Application app) {
        List<ResponseTime> responseTimeList = new ArrayList<ResponseTime>();

        ResponseTime one = new ResponseTime(app.getName(), app.getServiceTypeCode(), 0);
        one.addResponseTime("test", (short) 1000, 1);
        responseTimeList.add(one);

        ResponseTime two = new ResponseTime(app.getName(), app.getServiceTypeCode(), 1000*60);
        two .addResponseTime("test", (short) 3000, 1);
        responseTimeList.add(two);
        return responseTimeList;
    }
}
