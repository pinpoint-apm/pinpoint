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
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;


import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class SpanAsyncStateListenerTest {

    @Mock
    private Span span;
    @Mock
    private TraceRoot traceRoot;
    @Mock
    private StorageFactory storageFactory;
    @Mock
    private Storage storage;

    @Test
    public void onComplete() throws Exception {
        when(span.getTraceRoot()).thenReturn(traceRoot);
        SpanChunkFactory spanChunkFactory = or((SpanChunkFactory) isNull(), (SpanChunkFactory) any());
        when(storageFactory.createStorage(spanChunkFactory)).thenReturn(storage);


        ListenableAsyncState.AsyncStateListener listener = new SpanAsyncStateListener(span, storageFactory);
        listener.finish();

        verify(span).isTimeRecording();
        verify(storage).store(span);

        //
        listener.finish();
        verify(span).isTimeRecording();
        verify(storage).store(span);
    }

    @Test
    public void onComplete_check_atomicity() throws Exception {
        when(span.getTraceRoot()).thenReturn(traceRoot);
        SpanChunkFactory spanChunkFactory = or((SpanChunkFactory) isNull(), (SpanChunkFactory) any());
        when(storageFactory.createStorage(spanChunkFactory)).thenReturn(storage);

        ListenableAsyncState.AsyncStateListener listener = new SpanAsyncStateListener(span, storageFactory);
        listener.finish();
        listener.finish();
        verify(span).isTimeRecording();
        verify(storage).store(span);
    }


}