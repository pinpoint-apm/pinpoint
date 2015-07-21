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

package com.navercorp.pinpoint.profiler.context;

import java.util.Collections;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaDataHolder;
import com.navercorp.pinpoint.profiler.context.DefaultTraceContext;
import com.navercorp.pinpoint.profiler.context.ThreadLocalTraceFactory;
import com.navercorp.pinpoint.profiler.context.storage.LogStorageFactory;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricRegistry;
import com.navercorp.pinpoint.profiler.sampler.TrueSampler;

import org.junit.Assert;
import org.junit.Test;

public class ThreadLocalTraceFactoryTest {

    private ThreadLocalTraceFactory getTraceFactory() {
        LogStorageFactory logStorageFactory = new LogStorageFactory();
        TrueSampler trueSampler = new TrueSampler();
        ServerMetaDataHolder serverMetaDataHolder = new DefaultServerMetaDataHolder(Collections.<String>emptyList());
        AgentInformation agentInformation = new AgentInformation("agentId", "applicationName", System.currentTimeMillis(), 10, "test", "127.0.0.1", ServiceType.STAND_ALONE, Version.VERSION);
        DefaultTraceContext traceContext = new DefaultTraceContext(100, agentInformation, logStorageFactory, trueSampler, serverMetaDataHolder);
        return new ThreadLocalTraceFactory(traceContext, logStorageFactory, trueSampler);
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