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

import com.navercorp.pinpoint.bootstrap.context.Trace;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThreadLocalReferenceFactoryTest extends ThreadLocalFactoryAbstractTest {

    @Override
    public TraceFactory newTraceFactory(boolean sampled) {

        final Trace trace = mock(Trace.class);
        when(trace.canSampled()).thenReturn(sampled);

        final Trace disable = mock(Trace.class);
        when(disable.canSampled()).thenReturn(false);

        final BaseTraceFactory baseTraceFactory = mock(BaseTraceFactory.class);
        when(baseTraceFactory.newTraceObject()).thenReturn(trace);
        when(baseTraceFactory.disableSampling()).thenReturn(disable);

        TraceFactory traceFactory = new ThreadLocalReferenceFactory(baseTraceFactory);
        return traceFactory;
    }



}