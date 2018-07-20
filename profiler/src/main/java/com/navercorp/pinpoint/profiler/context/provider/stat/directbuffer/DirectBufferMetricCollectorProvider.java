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

package com.navercorp.pinpoint.profiler.context.provider.stat.directbuffer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.monitor.collector.directbuffer.DefaultDirectBufferMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.directbuffer.DirectBufferMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.directbuffer.UnsupportedDirectBufferMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.directbuffer.DirectBufferMetric;


/**
 * @author Roy Kim
 */
public class DirectBufferMetricCollectorProvider implements Provider<DirectBufferMetricCollector> {

    private final DirectBufferMetric directBufferMetric;

    @Inject
    public DirectBufferMetricCollectorProvider(DirectBufferMetric directBufferMetric) {
        if (directBufferMetric == null) {
            throw new NullPointerException("directBufferMetric must not be null");
        }
        this.directBufferMetric = directBufferMetric;
    }

    @Override
    public DirectBufferMetricCollector get() {
        if (directBufferMetric == DirectBufferMetric.UNSUPPORTED_DIRECT_BUFFER_METRIC) {
            return new UnsupportedDirectBufferMetricCollector();
        }
        return new DefaultDirectBufferMetricCollector(directBufferMetric);
    }
}
