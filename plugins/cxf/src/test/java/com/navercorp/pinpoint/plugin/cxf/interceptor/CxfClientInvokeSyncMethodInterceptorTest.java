/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.cxf.interceptor;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.plugin.cxf.CxfPluginConstants;

/**
 * @author barney
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CxfClientInvokeSyncMethodInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private ProfilerConfig profilerConfig;

    @Mock
    private Trace trace;

    @Mock
    private TraceId traceId;

    @Mock
    private TraceId nextId;

    @Mock
    private SpanEventRecorder recorder;

    @Test
    public void before() throws Exception {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(traceId).when(trace).getTraceId();
        doReturn(nextId).when(traceId).getNextTraceId();
        doReturn(recorder).when(trace).traceBlockBegin();

        Object target = new Object();
        Object operInfo = "[BindingOperationInfo: {http://foo.com/}getFoo]";
        Object[] arg = new Object[] { "foo", "bar" };
        Object[] args = new Object[] { "", operInfo, arg };

        CxfClientInvokeSyncMethodInterceptor interceptor = new CxfClientInvokeSyncMethodInterceptor(traceContext, descriptor);
        interceptor.before(target, args);

        verify(recorder).recordServiceType(CxfPluginConstants.CXF_CLIENT_SERVICE_TYPE);
        verify(recorder).recordDestinationId("http://foo.com/");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_OPERATION, "{http://foo.com/}getFoo");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_ARGS, "[foo, bar]");
    }

    @Test
    public void sampled_false() throws Exception {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(false).when(trace).canSampled();

        Object target = new Object();
        Object[] args = new Object[] {};

        CxfClientInvokeSyncMethodInterceptor interceptor = new CxfClientInvokeSyncMethodInterceptor(traceContext, descriptor);
        interceptor.before(target, args);

        verify(trace, never()).traceBlockBegin();
    }

    @Test
    public void hidden_all_params() throws Exception {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        String hiddenParams = "{http://foo.com/}getFoo";
        doReturn(hiddenParams).when(profilerConfig).readString("profiler.cxf.client.hiddenParams", "");
        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(traceId).when(trace).getTraceId();
        doReturn(nextId).when(traceId).getNextTraceId();
        doReturn(recorder).when(trace).traceBlockBegin();

        Object target = new Object();
        Object operInfo = "[BindingOperationInfo: {http://foo.com/}getFoo]";
        Object[] arg = new Object[] { "foo", "bar" };
        Object[] args = new Object[] { "", operInfo, arg };

        CxfClientInvokeSyncMethodInterceptor interceptor = new CxfClientInvokeSyncMethodInterceptor(traceContext, descriptor);
        interceptor.before(target, args);

        verify(recorder).recordServiceType(CxfPluginConstants.CXF_CLIENT_SERVICE_TYPE);
        verify(recorder).recordDestinationId("http://foo.com/");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_OPERATION, "{http://foo.com/}getFoo");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_ARGS, "[HIDDEN 2 PARAM]");
    }

    @Test
    public void hidden_param_index() throws Exception {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        String hiddenParams = "{http://foo.com/}getFoo:1";
        doReturn(hiddenParams).when(profilerConfig).readString("profiler.cxf.client.hiddenParams", "");
        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(traceId).when(trace).getTraceId();
        doReturn(nextId).when(traceId).getNextTraceId();
        doReturn(recorder).when(trace).traceBlockBegin();

        Object target = new Object();
        Object operInfo = "[BindingOperationInfo: {http://foo.com/}getFoo]";
        Object[] arg = new Object[] { "foo", "bar" };
        Object[] args = new Object[] { "", operInfo, arg };

        CxfClientInvokeSyncMethodInterceptor interceptor = new CxfClientInvokeSyncMethodInterceptor(traceContext, descriptor);
        interceptor.before(target, args);

        verify(recorder).recordServiceType(CxfPluginConstants.CXF_CLIENT_SERVICE_TYPE);
        verify(recorder).recordDestinationId("http://foo.com/");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_OPERATION, "{http://foo.com/}getFoo");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_ARGS, "[foo, [HIDDEN PARAM]]");
    }

    @Test
    public void hidden_param_incorrect_index() throws Exception {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        String hiddenParams = "{http://foo.com/}getFoo:2";
        doReturn(hiddenParams).when(profilerConfig).readString("profiler.cxf.client.hiddenParams", "");
        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(traceId).when(trace).getTraceId();
        doReturn(nextId).when(traceId).getNextTraceId();
        doReturn(recorder).when(trace).traceBlockBegin();

        Object target = new Object();
        Object operInfo = "[BindingOperationInfo: {http://foo.com/}getFoo]";
        Object[] arg = new Object[] { "foo", "bar" };
        Object[] args = new Object[] { "", operInfo, arg };

        CxfClientInvokeSyncMethodInterceptor interceptor = new CxfClientInvokeSyncMethodInterceptor(traceContext, descriptor);
        interceptor.before(target, args);

        verify(recorder).recordServiceType(CxfPluginConstants.CXF_CLIENT_SERVICE_TYPE);
        verify(recorder).recordDestinationId("http://foo.com/");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_OPERATION, "{http://foo.com/}getFoo");
        verify(recorder).recordAttribute(CxfPluginConstants.CXF_ARGS, "[foo, bar]");
    }
}
