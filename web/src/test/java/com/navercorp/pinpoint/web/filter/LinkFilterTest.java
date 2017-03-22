package com.navercorp.pinpoint.web.filter;

import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.util.ServiceTypeRegistryMockFactory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author emeroad
 */
public class LinkFilterTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ServiceTypeRegistryService serviceTypeRegistryService = mockServiceTypeRegistryService();

    private ServiceTypeRegistryService mockServiceTypeRegistryService() {

        final short tomcatTypeCode = 1010;
        final String tomcatTypeName = "TOMCAT";
        ServiceTypeRegistryMockFactory mockFactory = new ServiceTypeRegistryMockFactory();
        mockFactory.addServiceTypeMock(tomcatTypeCode, tomcatTypeName);

        return mockFactory.createMockServiceTypeRegistryService();
    }


    @Test
    public void fromToFilterTest() {
        ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName("TOMCAT");
        final short tomcatServiceType = tomcat.getCode();

        FilterDescriptor descriptor = new FilterDescriptor();
        descriptor.setFromApplicationName("APP_A");
        descriptor.setFromServiceType(tomcat.getName());
//        descriptor.setFromAgentId("AGENT_A");

        descriptor.setToApplicationName("APP_B");
        descriptor.setToServiceType(tomcat.getName());
//        descriptor.setToAgentId("AGENT_B");

        FilterHint hint = new FilterHint(Collections.<RpcHint>emptyList());

        LinkFilter linkFilter = new LinkFilter(descriptor, hint, serviceTypeRegistryService);
        logger.debug(linkFilter.toString());

        SpanBo fromSpanBo = new SpanBo();
        fromSpanBo.setApplicationId("APP_A");

        fromSpanBo.setServiceType(tomcatServiceType);
        fromSpanBo.setAgentId("AGENT_A");
        fromSpanBo.setSpanId(100);

        SpanBo toSpanBO = new SpanBo();
        toSpanBO.setApplicationId("APP_B");
        toSpanBO.setServiceType(tomcatServiceType);
        toSpanBO.setAgentId("AGENT_B");
        toSpanBO.setParentSpanId(100);

        SpanBo spanBoC = new SpanBo();
        spanBoC.setApplicationId("APP_C");
        spanBoC.setServiceType(tomcatServiceType);
        spanBoC.setAgentId("AGENT_C");

        Assert.assertTrue(linkFilter.include(Arrays.asList(fromSpanBo, toSpanBO)));
        Assert.assertFalse(linkFilter.include(Arrays.asList(fromSpanBo, spanBoC)));

    }

    @Test
    public void fromToFilterAgentTest() {
        final ServiceType tomcat = serviceTypeRegistryService.findServiceTypeByName("TOMCAT");
        final short tomcatServiceType = tomcat.getCode();

        FilterDescriptor descriptor = new FilterDescriptor();
        descriptor.setFromApplicationName("APP_A");
        descriptor.setFromServiceType(tomcat.getName());
        descriptor.setFromAgentId("AGENT_A");

        descriptor.setToApplicationName("APP_B");
        descriptor.setToServiceType(tomcat.getName());
        descriptor.setToAgentId("AGENT_B");

        FilterHint hint = new FilterHint(Collections.<RpcHint>emptyList());

        LinkFilter linkFilter = new LinkFilter(descriptor, hint, serviceTypeRegistryService);
        logger.debug(linkFilter.toString());

        SpanBo fromSpanBo = new SpanBo();
        fromSpanBo.setApplicationId("APP_A");

        fromSpanBo.setServiceType(tomcatServiceType);
        fromSpanBo.setAgentId("AGENT_A");
        fromSpanBo.setSpanId(100);

        SpanBo toSpanBO = new SpanBo();
        toSpanBO.setApplicationId("APP_B");
        toSpanBO.setServiceType(tomcatServiceType);
        toSpanBO.setAgentId("AGENT_B");
        toSpanBO.setParentSpanId(100);

        SpanBo spanBoC = new SpanBo();
        spanBoC.setApplicationId("APP_C");
        spanBoC.setServiceType(tomcatServiceType);
        spanBoC.setAgentId("AGENT_C");

        Assert.assertTrue(linkFilter.include(Arrays.asList(fromSpanBo, toSpanBO)));
        Assert.assertFalse(linkFilter.include(Arrays.asList(fromSpanBo, spanBoC)));

    }



}