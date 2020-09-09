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

import jdk.internal.platform.Container;
import jdk.internal.platform.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Hyunjoon Cho
 */
public class DefaultContainerMetric implements ContainerMetric{

    private final Metrics metrics;  //replace with MXBean?
    private final Logger logger;
    private long prevUserCpuUsage;
    private long prevSystemCpuUsage;
    private long prevTime;

    public DefaultContainerMetric() {
        Metrics metrics = Container.metrics();
        logger = LoggerFactory.getLogger(this.getClass());
        prevUserCpuUsage = metrics.getCpuUserUsage();
        prevSystemCpuUsage = metrics.getCpuSystemUsage();
        prevTime = System.currentTimeMillis();
        this.metrics = metrics;
    }

    @Override
    public ContainerMetricSnapshot getSnapshot() {
        final long currentUserCpuUsage = metrics.getCpuUserUsage();
        final long currentSystemCpuUsage = metrics.getCpuSystemUsage();
        final long currentTime = System.currentTimeMillis();
        final long memoryMax = metrics.getMemoryLimit();
        final long memoryUsage = metrics.getMemoryUsage();
        final double userCpuUsage = (currentUserCpuUsage - prevUserCpuUsage) * 1000 / (currentTime - prevTime);
        final double systemCpuUsage = (currentSystemCpuUsage - prevSystemCpuUsage) * 1000 / (currentTime - prevTime);
        // cpu usage in 1/100sec tick
        prevUserCpuUsage = currentUserCpuUsage;
        prevSystemCpuUsage = currentSystemCpuUsage;
        prevTime = currentTime;
        logger.debug("userCPU:{}% systemCPU:{}% memoryMax:{}B memoryUsage:{}B", userCpuUsage, systemCpuUsage, memoryMax, memoryUsage);

        return new ContainerMetricSnapshot(userCpuUsage, systemCpuUsage, memoryMax, memoryUsage);
    }

    @Override
    public String toString(){
        return "DefaultContainerMetric";
    }
}
