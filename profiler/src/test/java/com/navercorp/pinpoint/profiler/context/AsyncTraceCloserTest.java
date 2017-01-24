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

import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AsyncTraceCloserTest {
    @Test
    public void close() throws Exception {
        SpanCompletionCallback mock = mock(SpanCompletionCallback.class);

        AsyncTraceCloser asyncTraceCloser = new AsyncTraceCloser(mock);
        asyncTraceCloser.setup();
        asyncTraceCloser.await();
        asyncTraceCloser.close();

        verify(mock, times(1)).onComplete();
    }


    @Test
    public void close_setup() throws Exception {
        SpanCompletionCallback mock = mock(SpanCompletionCallback.class);

        AsyncTraceCloser asyncTraceCloser = new AsyncTraceCloser(mock);
        asyncTraceCloser.setup();
        asyncTraceCloser.close();

        verify(mock, times(0)).onComplete();
    }


    @Test
    public void close_await() throws Exception {
        SpanCompletionCallback mock = mock(SpanCompletionCallback.class);

        AsyncTraceCloser asyncTraceCloser = new AsyncTraceCloser(mock);
        asyncTraceCloser.await();
        asyncTraceCloser.close();

        verify(mock, times(0)).onComplete();
    }

}