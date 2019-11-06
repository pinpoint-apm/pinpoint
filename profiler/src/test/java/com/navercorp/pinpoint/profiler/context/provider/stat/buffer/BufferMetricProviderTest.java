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

package com.navercorp.pinpoint.profiler.context.provider.stat.buffer;

import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.buffer.BufferMetricSnapshot;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class BufferMetricProviderTest {

    @Test
    public void get() {
        Provider<BufferMetric> provider = new BufferMetricProvider();
        BufferMetric bufferMetric = provider.get();
        BufferMetricSnapshot snapshot = bufferMetric.getSnapshot();
        Assert.assertEquals(snapshot.getDirectCount(), BufferMetric.UNCOLLECTED_VALUE);
    }
}