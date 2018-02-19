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

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.doReturn;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class KafkaConsumerConstructorInterceptorTest {
    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private ProfilerConfig profilerConfig;

    @Before
    public void setUp() {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn("TEST").when(profilerConfig).readString("profiler.kafka.caller", "CALLER");
        doReturn("com.navercorp.pinpoint.plugin.kafka.encoder.BytesEncoder").when(profilerConfig).readString("profiler.kafka.include.encoder" , "");
    }

    @Test
    public void constructor() {
        new KafkaConsumerConstructorInterceptor(traceContext, descriptor);
    }
}
