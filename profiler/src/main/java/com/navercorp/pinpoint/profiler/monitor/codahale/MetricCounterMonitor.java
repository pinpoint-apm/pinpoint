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

import com.codahale.metrics.Counter;
import com.navercorp.pinpoint.profiler.monitor.CounterMonitor;

public class MetricCounterMonitor implements CounterMonitor {

    final Counter delegate;

    public MetricCounterMonitor(Counter delegate) {
        if (delegate == null) {
            throw new NullPointerException("Counter delegate is null");
        }
        this.delegate = delegate;
    }

    public void incr() {
        this.delegate.inc();
    }

    public void incr(long delta) {
        this.delegate.inc(delta);
    }

    public void decr() {
        this.delegate.dec();
    }

    public void decr(long delta) {
        this.delegate.dec(delta);
    }

    public void reset() {
        throw new RuntimeException("Counter reset is not supported in Codahale Metrics 3.x.");
    }

    public long getCount() {
        return this.delegate.getCount();
    }

    public Counter getDelegate() {
        return this.delegate;
    }

    public String toString() {
        return "MetricCounterMonitor(delegate=" + this.delegate + ")";
    }

}
