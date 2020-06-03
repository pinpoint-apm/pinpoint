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

package com.navercorp.pinpoint.profiler.context.monitor.metric;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.CustomMetricRegistry;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.DoubleGauge;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntGauge;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongGauge;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Taejin Koo
 */
public class CustomMetricRegistryAdaptor implements CustomMetricRegistry {

    private final CustomMetricRegistryService delegate;

    public CustomMetricRegistryAdaptor(CustomMetricRegistryService delegate) {
        this.delegate = Assert.requireNonNull(delegate, "delegate");
    }

    @Override
    public boolean register(IntCounter intCounter) {
        return delegate.register(intCounter);
    }

    @Override
    public boolean register(LongCounter longCounter) {
        return delegate.register(longCounter);
    }

    @Override
    public boolean register(IntGauge intGauge) {
        return delegate.register(intGauge);
    }

    @Override
    public boolean register(LongGauge longGauge) {
        return delegate.register(longGauge);
    }

    @Override
    public boolean register(DoubleGauge doubleGauge) {
        return delegate.register(doubleGauge);
    }

    @Override
    public boolean unregister(IntCounter intCounter) {
        return delegate.unregister(intCounter);
    }

    @Override
    public boolean unregister(LongCounter longCounter) {
        return delegate.unregister(longCounter);
    }

    @Override
    public boolean unregister(IntGauge intGauge) {
        return delegate.unregister(intGauge);
    }

    @Override
    public boolean unregister(LongGauge longGauge) {
        return delegate.unregister(longGauge);
    }

    @Override
    public boolean unregister(DoubleGauge doubleGauge) {
        return delegate.unregister(doubleGauge);
    }
    
}
