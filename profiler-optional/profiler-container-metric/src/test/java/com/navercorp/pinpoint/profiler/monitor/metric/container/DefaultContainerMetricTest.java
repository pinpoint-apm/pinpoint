/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.container;

import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.provider.stat.container.ContainerMetricProvider;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Hyunjoon Cho
 */
public class DefaultContainerMetricTest {

    @Test
    public void getSnapshot(){
        ContainerMetric containerMetric = new DefaultContainerMetric();
        ContainerMetricSnapshot snapshot = containerMetric.getSnapshot();

        Assert.assertNotEquals(snapshot.getUserCpuUsage(), ContainerMetric.UNCOLLECTED_PERCENT_USAGE);
        Assert.assertNotEquals(snapshot.getSystemCpuUsage(), ContainerMetric.UNCOLLECTED_PERCENT_USAGE);
        Assert.assertNotEquals(snapshot.getMemoryMax(), ContainerMetric.UNCOLLECTED_MEMORY);
        Assert.assertNotEquals(snapshot.getMemoryUsage(), ContainerMetric.UNCOLLECTED_MEMORY);
    }

    @Test
    public void provider() {
        Provider<ContainerMetric> provider = new ContainerMetricProvider();
        ContainerMetric containerMetric = provider.get();
        ContainerMetricSnapshot snapshot = containerMetric.getSnapshot();

        Assert.assertNotEquals(snapshot.getUserCpuUsage(), ContainerMetric.UNCOLLECTED_PERCENT_USAGE);
        Assert.assertNotEquals(snapshot.getSystemCpuUsage(), ContainerMetric.UNCOLLECTED_PERCENT_USAGE);
        Assert.assertNotEquals(snapshot.getMemoryMax(), ContainerMetric.UNCOLLECTED_MEMORY);
        Assert.assertNotEquals(snapshot.getMemoryUsage(), ContainerMetric.UNCOLLECTED_MEMORY);
    }
}
