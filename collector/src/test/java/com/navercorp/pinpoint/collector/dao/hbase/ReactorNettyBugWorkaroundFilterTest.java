package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeBuilder;
import org.junit.Assert;
import org.junit.Test;

public class ReactorNettyBugWorkaroundFilterTest {

    IgnoreStatFilter filter = new ReactorNettyBugWorkaroundFilter();

    @Test
    public void filter_servlet() {
        ServiceType serviceType = ServiceType.SERVLET;

        Assert.assertFalse(filter.filter(serviceType, "/url"));
        Assert.assertFalse(filter.filter(serviceType, "url?test=a"));

        Assert.assertFalse(filter.filter(serviceType, "localhost:1234"));
    }

    @Test
    public void filter_netty() {
        ServiceType serviceType = newReactorNettyClient();

        Assert.assertTrue(filter.filter(serviceType, "/url"));
        Assert.assertTrue(filter.filter(serviceType, "url?test=a"));

        Assert.assertFalse(filter.filter(serviceType, "localhost:1234"));
    }

    private ServiceType newReactorNettyClient() {
        int reactorNettyClient = ReactorNettyBugWorkaroundFilter.REACTOR_NETTY_CODE;
        ServiceTypeBuilder builder = new ServiceTypeBuilder((short) reactorNettyClient, "Netty");
        return builder.build();
    }

    @Test
    public void filter_null() {
        ServiceType serviceType = newReactorNettyClient();

        Assert.assertFalse(filter.filter(serviceType, null));
    }

    @Test
    public void filter_empty() {
        ServiceType serviceType = newReactorNettyClient();

        Assert.assertFalse(filter.filter(serviceType, ""));
    }

}