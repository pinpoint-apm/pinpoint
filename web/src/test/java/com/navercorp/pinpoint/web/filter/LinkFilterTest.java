package com.navercorp.pinpoint.web.filter;

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.bo.SpanBo;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author emeroad
 */
public class LinkFilterTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void fromToFilterTest() {
        FilterDescriptor descriptor = new FilterDescriptor();
        descriptor.setFromApplicationName("APP_A");
        descriptor.setFromServiceType("TOMCAT");
//        descriptor.setFromAgentId("AGENT_A");

        descriptor.setToApplicationName("APP_B");
        descriptor.setToServiceType("TOMCAT");
//        descriptor.setToAgentId("AGENT_B");

        FilterHint hint = new FilterHint(Collections.<RpcHint>emptyList());

        LinkFilter linkFilter = new LinkFilter(descriptor, hint);
        logger.debug(linkFilter.toString());

        SpanBo fromSpanBo = new SpanBo();
        fromSpanBo.setApplicationId("APP_A");
        fromSpanBo.setServiceType(ServiceType.TOMCAT);
        fromSpanBo.setAgentId("AGENT_A");
        fromSpanBo.setSpanID(100);

        SpanBo toSpanBO = new SpanBo();
        toSpanBO.setApplicationId("APP_B");
        toSpanBO.setServiceType(ServiceType.TOMCAT);
        toSpanBO.setAgentId("AGENT_B");
        toSpanBO.setParentSpanId(100);

        SpanBo spanBoC = new SpanBo();
        spanBoC.setApplicationId("APP_C");
        spanBoC.setServiceType(ServiceType.TOMCAT);
        spanBoC.setAgentId("AGENT_C");

        Assert.assertTrue(linkFilter.include(Arrays.asList(fromSpanBo, toSpanBO)));
        Assert.assertFalse(linkFilter.include(Arrays.asList(fromSpanBo, spanBoC)));

    }

    @Test
    public void fromToFilterAgentTest() {
        FilterDescriptor descriptor = new FilterDescriptor();
        descriptor.setFromApplicationName("APP_A");
        descriptor.setFromServiceType("TOMCAT");
        descriptor.setFromAgentId("AGENT_A");

        descriptor.setToApplicationName("APP_B");
        descriptor.setToServiceType("TOMCAT");
        descriptor.setToAgentId("AGENT_B");

        FilterHint hint = new FilterHint(Collections.<RpcHint>emptyList());

        LinkFilter linkFilter = new LinkFilter(descriptor, hint);
        logger.debug(linkFilter.toString());

        SpanBo fromSpanBo = new SpanBo();
        fromSpanBo.setApplicationId("APP_A");
        fromSpanBo.setServiceType(ServiceType.TOMCAT);
        fromSpanBo.setAgentId("AGENT_A");
        fromSpanBo.setSpanID(100);

        SpanBo toSpanBO = new SpanBo();
        toSpanBO.setApplicationId("APP_B");
        toSpanBO.setServiceType(ServiceType.TOMCAT);
        toSpanBO.setAgentId("AGENT_B");
        toSpanBO.setParentSpanId(100);

        SpanBo spanBoC = new SpanBo();
        spanBoC.setApplicationId("APP_C");
        spanBoC.setServiceType(ServiceType.TOMCAT);
        spanBoC.setAgentId("AGENT_C");

        Assert.assertTrue(linkFilter.include(Arrays.asList(fromSpanBo, toSpanBO)));
        Assert.assertFalse(linkFilter.include(Arrays.asList(fromSpanBo, spanBoC)));

    }



}