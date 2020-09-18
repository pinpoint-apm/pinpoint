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

/**
 * @author Hyunjoon Cho
 */
public class ContainerMetricSnapshot {
    private final long userCpuUsage;
    private final long systemCpuUsage;
    private final long memoryUsage;

    public ContainerMetricSnapshot(long userCpuUsage, long systemCpuUsage, long memoryUsage){
        this.userCpuUsage = userCpuUsage;
        this.systemCpuUsage = systemCpuUsage;
        this.memoryUsage = memoryUsage;
    }

    public long getUserCpuUsage(){
        return userCpuUsage;
    }

    public long getSystemCpuUsage(){
        return systemCpuUsage;
    }

    public long getMemoryUsage(){
        return memoryUsage;
    }
}
