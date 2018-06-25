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

package com.navercorp.pinpoint.profiler.monitor.collector.directbuffer;


import com.navercorp.pinpoint.profiler.monitor.metric.directbuffer.DirectBufferMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.directbuffer.DirectBufferMetricSnapshot;
import com.navercorp.pinpoint.thrift.dto.TDirectBuffer;

/**
 * @author Roy Kim
 */
public class DefaultDirectBufferMetricCollector implements DirectBufferMetricCollector {

    private final DirectBufferMetric directBufferMetric;

    public DefaultDirectBufferMetricCollector(DirectBufferMetric directBufferMetric) {
        if (directBufferMetric == null) {
            throw new NullPointerException("directBufferMetric must not be null");
        }
        this.directBufferMetric = directBufferMetric;
    }

    @Override
    public TDirectBuffer collect() {
        TDirectBuffer tdirectBuffer = new TDirectBuffer();
        DirectBufferMetricSnapshot snapshot = directBufferMetric.getSnapshot();
        tdirectBuffer.setDirectCount(snapshot.getDirectCount());
        tdirectBuffer.setDirectMemoryUsed(snapshot.getDirectMemoryUsed());
        tdirectBuffer.setMappedCount(snapshot.getMappedCount());
        tdirectBuffer.setMappedMemoryUsed(snapshot.getMappedMemoryUsed());
        return tdirectBuffer;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultDirectBufferMetricCollector{");
        sb.append("directBufferMetric=").append(directBufferMetric);
        sb.append('}');
        return sb.toString();
    }
}
