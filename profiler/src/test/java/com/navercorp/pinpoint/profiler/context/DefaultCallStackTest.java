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

import org.junit.Before;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class DefaultCallStackTest extends CallStackTest {

    private Span span;
    private SpanEvent spanEvent;

    @Before
    public void before() {
        span = new Span();
        spanEvent = new SpanEvent(span);
    }

    @Override
    public CallStack newCallStack() {
        return new DefaultCallStack(span);
    }

    @Override
    public CallStack newCallStack(int depth) {
        return new DefaultCallStack(span, depth);
    }

    @Override
    public Span getSpan() {
        return span;
    }

    @Override
    public SpanEvent getSpanEvent() {
        return spanEvent;
    }
}