/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.junit.Before;

import static org.mockito.Mockito.mock;


/**
 * @author Woonduk Kang(emeroad)
 */
public class DepthCompressCallStackTest extends CallStackTest {

    private SpanEvent spanEvent;
    private TraceRoot internalTraceId;

    @Before
    public void before() {
        this.internalTraceId = mock(TraceRoot.class);
        this.spanEvent = new SpanEvent(internalTraceId);
    }

    @Override
    public CallStack newCallStack() {
        return new DefaultCallStack(internalTraceId);
    }


    @Override
    public CallStack newCallStack(int depth) {
        return new DepthCompressCallStack(internalTraceId, depth);
    }

    @Override
    TraceRoot getLocalTraceId() {
        return internalTraceId;
    }
    @Override
    public SpanEvent getSpanEvent() {
        return spanEvent;
    }

}