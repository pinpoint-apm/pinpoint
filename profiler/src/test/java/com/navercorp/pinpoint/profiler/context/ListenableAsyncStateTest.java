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
import com.navercorp.pinpoint.profiler.context.id.DefaultShared;
import com.navercorp.pinpoint.profiler.context.id.ListenableAsyncState;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.storage.DisabledUriStatStorage;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ListenableAsyncStateTest {
    @Test
    public void close() {
        SpanAsyncStateListener mock = mock(SpanAsyncStateListener.class);
        TraceRoot traceRoot = newTraceRoot();

        ListenableAsyncState listenableAsyncState = new ListenableAsyncState(traceRoot, mock, ActiveTraceHandle.EMPTY_HANDLE, DisabledUriStatStorage.INSTANCE);
        listenableAsyncState.setup();
        listenableAsyncState.await();
        listenableAsyncState.finish();

        verify(mock).finish();
    }

    private static TraceRoot newTraceRoot() {
        TraceRoot traceRoot = mock(TraceRoot.class);
        when(traceRoot.getShared()).thenReturn(new DefaultShared());
        return traceRoot;
    }


    @Test
    public void close_setup() {
        SpanAsyncStateListener mock = mock(SpanAsyncStateListener.class);
        TraceRoot traceRoot = newTraceRoot();

        ListenableAsyncState listenableAsyncState = new ListenableAsyncState(traceRoot, mock, ActiveTraceHandle.EMPTY_HANDLE, DisabledUriStatStorage.INSTANCE);
        listenableAsyncState.setup();
        listenableAsyncState.finish();

        verify(mock, never()).finish();
    }


    @Test
    public void close_await() {
        SpanAsyncStateListener mock = mock(SpanAsyncStateListener.class);
        TraceRoot traceRoot = newTraceRoot();

        ListenableAsyncState listenableAsyncState = new ListenableAsyncState(traceRoot, mock, ActiveTraceHandle.EMPTY_HANDLE, DisabledUriStatStorage.INSTANCE);
        listenableAsyncState.await();
        listenableAsyncState.finish();

        verify(mock, never()).finish();
    }

}