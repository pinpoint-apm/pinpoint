package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.view.AgentResponseTimeViewModel;
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
public class AgentTimeSeriesHistogramTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testViewModel() throws IOException {

        Application app = new Application("test", ServiceType.TOMCAT);
        AgentTimeSeriesHistogram histogram = new AgentTimeSeriesHistogram(app, new Range(0, 1000*60));
        List<ResponseTime> responseHistogramList = createResponseTime(app, "test1", "test2");
        histogram.build(responseHistogramList);

        List<AgentResponseTimeViewModel> viewModel = histogram.createViewModel();
        logger.debug("{}", viewModel);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        String s = writer.writeValueAsString(viewModel);
        logger.debug(s);

    }

    private List<ResponseTime> createResponseTime(Application app, String agentName1, String agentName2) {
        List<ResponseTime> responseTimeList = new ArrayList<ResponseTime>();

        ResponseTime one = new ResponseTime(app.getName(), app.getServiceTypeCode(), 0);
        one.addResponseTime(agentName1, (short) 1000, 1);
        responseTimeList.add(one);

        ResponseTime two = new ResponseTime(app.getName(), app.getServiceTypeCode(), 1000*60);
        two.addResponseTime(agentName1, (short) 3000, 1);
        responseTimeList.add(two);

        ResponseTime three = new ResponseTime(app.getName(), app.getServiceTypeCode(), 0);
        three.addResponseTime(agentName2, (short) 1000, 1);
        responseTimeList.add(three);

        ResponseTime four = new ResponseTime(app.getName(), app.getServiceTypeCode(), 1000*60);
        four.addResponseTime(agentName2, (short) 3000, 1);
        responseTimeList.add(four);
        return responseTimeList;
    }

}
