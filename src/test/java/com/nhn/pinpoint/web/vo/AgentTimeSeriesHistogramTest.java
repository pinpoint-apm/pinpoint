package com.nhn.pinpoint.web.vo;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.view.AgentResponseTimeViewModel;
import com.nhn.pinpoint.web.view.AgentResponseTimeViewModelList;
import com.nhn.pinpoint.web.view.ResponseTimeViewModel;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.util.logging.resources.logging;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        JsonFactory jsonFactory = mapper.getJsonFactory();
        StringWriter stringWriter = new StringWriter();
        JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(stringWriter);
        jsonGenerator.writeStartObject();
        for (AgentResponseTimeViewModel agentResponseTimeViewModel : viewModel) {
            jsonGenerator.writeObject(agentResponseTimeViewModel);
        }
        jsonGenerator.writeEndObject();
        jsonGenerator.flush();
        jsonGenerator.close();
        logger.debug(stringWriter.toString());

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
