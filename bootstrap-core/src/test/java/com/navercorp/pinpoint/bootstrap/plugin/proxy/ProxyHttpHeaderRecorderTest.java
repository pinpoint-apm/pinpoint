/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.proxy;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author jaehong.kim
 */
public class ProxyHttpHeaderRecorderTest {

    @Test
    public void record() throws Exception {
        List<String> list = Arrays.asList("PINPOINT-PROXY");

        // TraceContext
        ProfilerConfig config = mock(ProfilerConfig.class);
        when(config.getProxyHttpHeaderNames()).thenReturn(list);
        when(config.isProxyHttpHeaderEnable()).thenReturn(true);
        when(config.isProxyHttpHeaderHidden()).thenReturn(true);

        TraceContext traceContext = mock(TraceContext.class);
        when(traceContext.getProfilerConfig()).thenReturn(config);
        when(traceContext.cacheString(any(String.class))).thenReturn(100);

        // SpanRecorder
        SpanRecorder spanRecorder = mock(SpanRecorder.class);

        ProxyHttpHeaderRecorder recorder = new ProxyHttpHeaderRecorder(traceContext);
        recorder.record(spanRecorder, new ProxyHttpHeaderHandler() {
            @Override
            public String read(String name) {
                return name;
            }

            @Override
            public void remove(String name) {
            }
        });

        recorder.record(spanRecorder, new ProxyHttpHeaderHandler() {
            @Override
            public String read(String name) {
                throw new NullPointerException();
            }

            @Override
            public void remove(String name) {
                throw new NullPointerException();
            }
        });
    }
}