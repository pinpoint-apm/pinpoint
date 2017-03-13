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

import com.navercorp.pinpoint.profiler.context.id.ListenableAsyncState;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanAsyncStateListenerTest {


    @Test
    public void onComplete() throws Exception {
        Span span = mock(Span.class);
        Storage storage = mock(Storage.class);

        ListenableAsyncState.AsyncStateListener listener = new SpanAsyncStateListener(span, storage);
        listener.finish();

        verify(span, times(1)).isTimeRecording();
        verify(storage, times(1)).store(span);

        //
        listener.finish();
        verify(span, times(1)).isTimeRecording();
        verify(storage, times(1)).store(span);
    }

    @Test
    public void onComplete_check_atomicity() throws Exception {
        Span span = mock(Span.class);
        Storage storage = mock(Storage.class);

        ListenableAsyncState.AsyncStateListener listener = new SpanAsyncStateListener(span, storage);
        listener.finish();
        listener.finish();
        verify(span, times(1)).isTimeRecording();
        verify(storage, times(1)).store(span);
    }


}