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

package com.navercorp.pinpoint.profiler.monitor.collector.buffer;


import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetricSnapshot;

/**
 * @author Roy Kim
 */
public class DefaultBufferMetricCollector implements AgentStatMetricCollector<BufferMetricSnapshot> {

    private final BufferMetric bufferMetric;

    public DefaultBufferMetricCollector(BufferMetric bufferMetric) {
        this.bufferMetric = Assert.requireNonNull(bufferMetric, "bufferMetric");
    }

    @Override
    public BufferMetricSnapshot collect() {
        final BufferMetricSnapshot snapshot = bufferMetric.getSnapshot();
        return snapshot;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultBufferMetricCollector{");
        sb.append("bufferMetric=").append(bufferMetric);
        sb.append('}');
        return sb.toString();
    }
}