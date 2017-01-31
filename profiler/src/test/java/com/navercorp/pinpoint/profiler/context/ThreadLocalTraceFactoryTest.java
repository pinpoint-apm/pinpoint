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

import com.navercorp.pinpoint.bootstrap.context.Trace;


import com.navercorp.pinpoint.exception.PinpointException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThreadLocalTraceFactoryTest {

    private final ThreadLocalTraceFactory sampledTraceFactory = newTraceFactory(true);

    private final ThreadLocalTraceFactory unsampledTraceFactory = newTraceFactory(false);

    private ThreadLocalTraceFactory newTraceFactory(boolean sampled) {

        Trace trace = mock(Trace.class);
        when(trace.canSampled()).thenReturn(sampled);

        Trace disable = mock(Trace.class);
        when(disable.canSampled()).thenReturn(false);

        BaseTraceFactory baseTraceFactory = mock(BaseTraceFactory.class);
        when(baseTraceFactory.newTraceObject()).thenReturn(trace);
        when(baseTraceFactory.disableSampling()).thenReturn(disable);

        ThreadLocalTraceFactory traceFactory = new ThreadLocalTraceFactory(baseTraceFactory);
        return traceFactory;
    }

    @After
    public void tearDown() throws Exception {
        sampledTraceFactory.removeTraceObject();
        unsampledTraceFactory.removeTraceObject();
    }

    @Test
    public void nullTraceObject() {
        TraceFactory traceFactory = sampledTraceFactory;

        Trace currentTraceObject = traceFactory.currentTraceObject();
        Assert.assertNull(currentTraceObject);

        Trace rawTraceObject = traceFactory.currentRawTraceObject();
        Assert.assertNull(rawTraceObject);

        Trace rpcTraceObject = traceFactory.currentRpcTraceObject();
        Assert.assertNull(rpcTraceObject);

        traceFactory.newTraceObject();
        Assert.assertNotNull(traceFactory.currentRawTraceObject());
    }



    @Test
    public void testCurrentTraceObject() throws Exception {
        TraceFactory traceFactory = sampledTraceFactory;

        Trace newTrace = traceFactory.newTraceObject();
        Trace currentTrace = traceFactory.currentTraceObject();

        Assert.assertNotNull(currentTrace);
        Assert.assertSame(newTrace, currentTrace);
    }

    @Test
    public void testCurrentTraceObject_unsampled() throws Exception {
        TraceFactory traceFactory = unsampledTraceFactory;

        Trace newTrace = traceFactory.newTraceObject();
        Trace currentTrace = traceFactory.currentTraceObject();

        Assert.assertNull(currentTrace);
        Assert.assertNotEquals(newTrace, currentTrace);
    }

    @Test
    public void testCurrentRpcTraceObject() throws Exception {
        TraceFactory traceFactory = sampledTraceFactory;

        Trace trace = traceFactory.newTraceObject();
        Trace rpcTraceObject = traceFactory.currentRpcTraceObject();

        Assert.assertNotNull(rpcTraceObject);
        Assert.assertSame(trace, rpcTraceObject);
    }

    @Test
    public void testCurrentRpcTraceObject_unsampled() throws Exception {
        TraceFactory traceFactory = unsampledTraceFactory;

        Trace trace = traceFactory.newTraceObject();
        Trace rpcTraceObject = traceFactory.currentRpcTraceObject();

        Assert.assertNotNull(rpcTraceObject);
        Assert.assertSame(trace, rpcTraceObject);
    }

    @Test
    public void testCurrentRawTraceObject() throws Exception {
        TraceFactory traceFactory = sampledTraceFactory;

        Trace trace = traceFactory.newTraceObject();
        Trace rawTrace = traceFactory.currentRawTraceObject();

        Assert.assertNotNull(rawTrace);
        Assert.assertSame(trace, rawTrace);
    }

    @Test
    public void testCurrentRawTraceObject_unsampled() throws Exception {
        TraceFactory traceFactory = unsampledTraceFactory;

        Trace trace = traceFactory.newTraceObject();
        Trace rawTrace = traceFactory.currentRawTraceObject();

        Assert.assertNotNull(rawTrace);
        Assert.assertSame(trace, rawTrace);
    }

    @Test
    public void testDisableSampling() throws Exception {

        TraceFactory traceFactory = sampledTraceFactory;

        Trace trace = traceFactory.disableSampling();
        Trace rawTrace = traceFactory.currentRawTraceObject();

        Assert.assertNotNull(rawTrace);
        Assert.assertSame(trace, rawTrace);
    }

    @Test
    public void testContinueTraceObject() throws Exception {
    }

    @Test
    public void testNewTraceObject() throws Exception {
        TraceFactory traceFactory = sampledTraceFactory;

        traceFactory.newTraceObject();
        Trace rawTraceObject = traceFactory.currentRawTraceObject();
        Assert.assertNotNull(rawTraceObject);

    }


    @Test(expected = PinpointException.class)
    public void duplicatedTraceStart() {
        TraceFactory traceFactory = sampledTraceFactory;

        traceFactory.newTraceObject();
        traceFactory.newTraceObject();

    }

    @Test
    public void testDetachTraceObject() throws Exception {
        ThreadLocalTraceFactory traceFactory = this.sampledTraceFactory;

        traceFactory.newTraceObject();
        traceFactory.removeTraceObject();

        Trace rawTraceObject = traceFactory.currentRawTraceObject();
        Assert.assertNull(rawTraceObject);
    }
}