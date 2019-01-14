/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class RequestWrapperReaderTest {

    @Test
    public void read() throws Exception {
        // disable sampling
        Trace disableTrace = mock(Trace.class);
        when(disableTrace.canSampled()).thenReturn(Boolean.FALSE);

        // continue trace
        Trace continueTrace = mock(Trace.class);
        when(continueTrace.canSampled()).thenReturn(Boolean.TRUE);

        // new trace
        Trace newTrace = mock(Trace.class);
        when(newTrace.canSampled()).thenReturn(Boolean.TRUE);

        TraceContext traceContext = mock(TraceContext.class);
        when(traceContext.disableSampling()).thenReturn(disableTrace);
        when(traceContext.continueTraceObject(any(TraceId.class))).thenReturn(continueTrace);
        when(traceContext.newTraceObject()).thenReturn(newTrace);
        when(traceContext.getProfilerConfig()).thenReturn(new DefaultProfilerConfig());

        TraceId traceId = mock(TraceId.class);
        when(traceContext.createTraceId(anyString(), anyLong(), anyLong(), anyShort())).thenReturn(traceId);
        RequestAdaptor<ServerRequestWrapper> serverRequestWrapperAdaptor = new ServerRequestWrapperAdaptor();
        final RequestTraceReader<ServerRequestWrapper> reader = new RequestTraceReader<ServerRequestWrapper>(traceContext, serverRequestWrapperAdaptor);

        // sampling flag is true
        ServerRequestWrapper samplingFlagServerRequestWrapper = mock(ServerRequestWrapper.class);
        when(samplingFlagServerRequestWrapper.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn("s0");
        assertEquals(disableTrace, reader.read(samplingFlagServerRequestWrapper));

        // continue trace
        ServerRequestWrapper continueServerRequestWrapper = mock(ServerRequestWrapper.class);
        when(continueServerRequestWrapper.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn("avcrawler01.ugedit^1517877953952^1035131");
        when(continueServerRequestWrapper.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn("1");
        when(continueServerRequestWrapper.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn("1");
        when(continueServerRequestWrapper.getHeader(Header.HTTP_FLAGS.toString())).thenReturn("1");
        assertEquals(continueTrace, reader.read(continueServerRequestWrapper));

        // new trace
        ServerRequestWrapper newServerRequestWrapper = mock(ServerRequestWrapper.class);
        assertEquals(newTrace, reader.read(newServerRequestWrapper));
    }
}