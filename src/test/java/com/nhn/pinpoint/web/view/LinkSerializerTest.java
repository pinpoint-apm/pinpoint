package com.nhn.pinpoint.web.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.Link;
import com.nhn.pinpoint.web.applicationmap.CreateType;
import com.nhn.pinpoint.web.applicationmap.Node;
import com.nhn.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class LinkSerializerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testSerialize() throws Exception {
        AgentHistogramList list = new AgentHistogramList();
        AgentHistogram histogram = new AgentHistogram(new Application("test", ServiceType.TOMCAT));
        list.addAgentHistogram(histogram);
        Node node1 = new Node(new Application("test1", ServiceType.TOMCAT));
        Node node2 = new Node(new Application("test1", ServiceType.TOMCAT));

        Link link = new Link(CreateType.Source, node1, node2, new Range(0, 1));
        ObjectWriter objectWriter = MAPPER.writerWithDefaultPrettyPrinter();
        String s = objectWriter.writeValueAsString(link);

        logger.debug(s);
    }
}
