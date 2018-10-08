/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.stat.directbuffer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.monitor.collector.directbuffer.BufferMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.directbuffer.DefaultBufferMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.directbuffer.UnsupportedBufferMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.directbuffer.BufferMetric;


/**
 * @author Roy Kim
 */
public class BufferMetricCollectorProvider implements Provider<BufferMetricCollector> {

    private final BufferMetric bufferMetric;

    @Inject
    public BufferMetricCollectorProvider(BufferMetric bufferMetric) {
        if (bufferMetric == null) {
            throw new NullPointerException("bufferMetric must not be null");
        }
        this.bufferMetric = bufferMetric;
    }

    @Override
    public BufferMetricCollector get() {
        if (bufferMetric == BufferMetric.UNSUPPORTED_BUFFER_METRIC) {
            return new UnsupportedBufferMetricCollector();
        }
        return new DefaultBufferMetricCollector(bufferMetric);
    }
}
