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

package com.navercorp.pinpoint.profiler.monitor.collector.container;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.container.ContainerMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.container.ContainerMetricSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hyunjoon Cho
 */
public class DefaultContainerMetricCollector implements AgentStatMetricCollector<ContainerMetricSnapshot> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ContainerMetric containerMetric;

    public DefaultContainerMetricCollector(ContainerMetric containerMetric){
        this.containerMetric = Assert.requireNonNull(containerMetric, "containerMetric");
    }

    @Override
    public ContainerMetricSnapshot collect() {
        final ContainerMetricSnapshot snapshot = containerMetric.getSnapshot();
        logger.debug("collect " + snapshot);
        return snapshot;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultContainerMetricCollector{");
        sb.append("containerMetric=").append(containerMetric);
        sb.append('}');
        return sb.toString();
    }
}