package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReactorNettyBugWorkaroundFilterTest {

    IgnoreStatFilter filter = new ReactorNettyBugWorkaroundFilter();

    @Test
    public void filter_servlet() {
        ServiceType serviceType = ServiceType.SERVLET;

        Assertions.assertFalse(filter.filter(serviceType, "/url"));
        Assertions.assertFalse(filter.filter(serviceType, "url?test=a"));

        Assertions.assertFalse(filter.filter(serviceType, "localhost:1234"));
    }

    @Test
    public void filter_netty() {
        ServiceType serviceType = newReactorNettyClient();

        Assertions.assertTrue(filter.filter(serviceType, "/url"));
        Assertions.assertTrue(filter.filter(serviceType, "url?test=a"));

        Assertions.assertFalse(filter.filter(serviceType, "localhost:1234"));
    }

    private ServiceType newReactorNettyClient() {
        int reactorNettyClient = ReactorNettyBugWorkaroundFilter.REACTOR_NETTY_CODE;
        ServiceTypeBuilder builder = new ServiceTypeBuilder((short) reactorNettyClient, "Netty");
        return builder.build();
    }

    @Test
    public void filter_null() {
        ServiceType serviceType = newReactorNettyClient();

        Assertions.assertFalse(filter.filter(serviceType, null));
    }

    @Test
    public void filter_empty() {
        ServiceType serviceType = newReactorNettyClient();

        Assertions.assertFalse(filter.filter(serviceType, ""));
    }

}