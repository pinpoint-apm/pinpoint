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

import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.id.ListenableAsyncState;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ListenableAsyncStateTest {
    @Test
    public void close() throws Exception {
        SpanAsyncStateListener mock = mock(SpanAsyncStateListener.class);

        ListenableAsyncState listenableAsyncState = new ListenableAsyncState(mock, ActiveTraceHandle.EMPTY_HANDLE);
        listenableAsyncState.setup();
        listenableAsyncState.await();
        listenableAsyncState.finish();

        verify(mock, times(1)).finish();
    }


    @Test
    public void close_setup() throws Exception {
        SpanAsyncStateListener mock = mock(SpanAsyncStateListener.class);

        ListenableAsyncState listenableAsyncState = new ListenableAsyncState(mock, ActiveTraceHandle.EMPTY_HANDLE);
        listenableAsyncState.setup();
        listenableAsyncState.finish();

        verify(mock, never()).finish();
    }


    @Test
    public void close_await() throws Exception {
        SpanAsyncStateListener mock = mock(SpanAsyncStateListener.class);

        ListenableAsyncState listenableAsyncState = new ListenableAsyncState(mock, ActiveTraceHandle.EMPTY_HANDLE);
        listenableAsyncState.await();
        listenableAsyncState.finish();

        verify(mock, never()).finish();
    }

}