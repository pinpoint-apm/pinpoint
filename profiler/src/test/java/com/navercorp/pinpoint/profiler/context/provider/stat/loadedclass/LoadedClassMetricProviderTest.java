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

package com.navercorp.pinpoint.profiler.context.provider.stat.loadedclass;

import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.monitor.metric.loadedclass.LoadedClassMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.loadedclass.LoadedClassMetricSnapshot;
import org.junit.Assert;
import org.junit.Test;

public class LoadedClassMetricProviderTest {
    @Test
    public void get() {
        Provider<LoadedClassMetric> provider = new LoadedClassMetricProvider();
        LoadedClassMetric loadedClassMetric = provider.get();
        LoadedClassMetricSnapshot snapshot = loadedClassMetric.getSnapshot();
        Assert.assertNotEquals(0, snapshot.getLoadedClassCount());

    }
}
