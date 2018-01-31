/*
 * Copyright 2018 NAVER Corp.
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class RequestTraceReaderTest {

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

        final RequestTraceReader reader = new RequestTraceReader(traceContext);

        // sampling flag is true
        ServerRequestTrace samplingFlagServerRequestTrace = mock(ServerRequestTrace.class);
        when(samplingFlagServerRequestTrace.getHeader(Header.HTTP_SAMPLED.toString())).thenReturn("s0");
        assertEquals(disableTrace, reader.read(samplingFlagServerRequestTrace));

        // continue trace
        ServerRequestTrace continueServerRequestTrace = mock(ServerRequestTrace.class);
        when(continueServerRequestTrace.getHeader(Header.HTTP_TRACE_ID.toString())).thenReturn("avcrawler01.ugedit^1517877953952^1035131");
        when(continueServerRequestTrace.getHeader(Header.HTTP_PARENT_SPAN_ID.toString())).thenReturn("1");
        when(continueServerRequestTrace.getHeader(Header.HTTP_SPAN_ID.toString())).thenReturn("1");
        when(continueServerRequestTrace.getHeader(Header.HTTP_FLAGS.toString())).thenReturn("1");
        assertEquals(continueTrace, reader.read(continueServerRequestTrace));

        // new trace
        ServerRequestTrace newServerRequestTrace = mock(ServerRequestTrace.class);
        assertEquals(newTrace, reader.read(newServerRequestTrace));
    }
}