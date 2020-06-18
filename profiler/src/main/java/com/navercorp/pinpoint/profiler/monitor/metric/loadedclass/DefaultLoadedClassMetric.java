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

package com.navercorp.pinpoint.profiler.monitor.metric.loadedclass;

import com.navercorp.pinpoint.profiler.monitor.metric.totalthread.TotalThreadMetricSnapshot;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

public class DefaultLoadedClassMetric implements LoadedClassMetric{
    private static final ClassLoadingMXBean CLASS_LOADING_MX_BEAN = ManagementFactory.getClassLoadingMXBean();

    @Override
    public LoadedClassMetricSnapshot getSnapshot() {
        int loadedClassCount = CLASS_LOADING_MX_BEAN.getLoadedClassCount();
        long unloadedClassCount = CLASS_LOADING_MX_BEAN.getUnloadedClassCount();
        return new LoadedClassMetricSnapshot(loadedClassCount, unloadedClassCount);
    }

    @Override
    public String toString() {
        return "Default TotalThreadMetric";
    }
}
