package com.nhn.pinpoint.profiler.context;

import java.util.Collections;

import com.nhn.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.context.storage.LogStorageFactory;
import com.nhn.pinpoint.profiler.monitor.metric.MetricRegistry;
import com.nhn.pinpoint.profiler.sampler.TrueSampler;

import junit.framework.Assert;

import org.junit.Test;

public class ThreadLocalTraceFactoryTest {

    private ThreadLocalTraceFactory getTraceFactory() {
        LogStorageFactory logStorageFactory = new LogStorageFactory();
        TrueSampler trueSampler = new TrueSampler();
        ServerMetaDataHolder serverMetaDataHolder = new DefaultServerMetaDataHolder(Collections.<String>emptyList());
        DefaultTraceContext traceContext = new DefaultTraceContext(100, ServiceType.TOMCAT.getCode(), logStorageFactory, trueSampler, serverMetaDataHolder);
        MetricRegistry metricRegistry = new MetricRegistry(ServiceType.TOMCAT);
        return new ThreadLocalTraceFactory(traceContext, metricRegistry, logStorageFactory, trueSampler);
    }

    @Test
    public void nullTraceObject() {
        ThreadLocalTraceFactory traceFactory = getTraceFactory();

        Trace currentTraceObject = traceFactory.currentTraceObject();
        Assert.assertNull(currentTraceObject);

        Trace rawTraceObject = traceFactory.currentRawTraceObject();
        Assert.assertNull(rawTraceObject);

    }

    @Test
    public void testCurrentTraceObject() throws Exception {
        ThreadLocalTraceFactory traceFactory = getTraceFactory();

        Trace trace = traceFactory.currentTraceObject();

    }

    @Test
    public void testCurrentRpcTraceObject() throws Exception {

    }

    @Test
    public void testCurrentRawTraceObject() throws Exception {

    }

    @Test
    public void testDisableSampling() throws Exception {

    }

    @Test
    public void testContinueTraceObject() throws Exception {

    }

    @Test
    public void testNewTraceObject() throws Exception {

    }

    @Test
    public void testDetachTraceObject() throws Exception {

    }
}