/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.codahale;

import com.codahale.metrics.Histogram;
import com.navercorp.pinpoint.profiler.monitor.HistogramMonitor;

public class MetricHistogramMonitor implements HistogramMonitor {

    private final Histogram delegate;

    public MetricHistogramMonitor(Histogram delegate) {
        if (delegate == null) {
            throw new NullPointerException("Histogram delegate is null");
        }
        this.delegate = delegate;
    }

    public void reset() {
        throw new RuntimeException("Histogram reset is not supported in Codahale Metrics 3.x.");
    }

    public void update(long value) {
        this.delegate.update(value);
    }

    public long getCount() {
        return this.delegate.getCount();
    }

    public Histogram getDelegate() {
        return this.delegate;
    }

    public String toString() {
        return "MetricValueDistributionMonitor(delegate=" + this.delegate + ")";
    }

}
