/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.rpc;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.TraceFactory;

/**
 * @author emeroad
 */
public class MetricTraceFactory implements TraceFactory {
    private final TraceFactory delegate;
    private final MetricRegistry metricRegistry;

    private MetricTraceFactory(TraceFactory traceFactory, ServiceType serviceType) {
        if (traceFactory == null) {
            throw new NullPointerException("traceFactory must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.delegate = traceFactory;
        this.metricRegistry = new MetricRegistry(serviceType);
    }

    public static TraceFactory wrap(TraceFactory traceFactory, ServiceType serviceType) {
        return new MetricTraceFactory(traceFactory, serviceType);
    }

    @Override
    public Trace currentTraceObject() {
        return delegate.currentTraceObject();
    }

    @Override
    public Trace currentRawTraceObject() {
        return delegate.currentRawTraceObject();
    }

    @Override
    public Trace disableSampling() {
        return delegate.disableSampling();
    }

    @Override
    public Trace continueTraceObject(TraceId traceId) {
        return delegate.continueTraceObject(traceId);
    }

    @Override
    public Trace continueTraceObject(Trace trace) {
        return delegate.continueTraceObject(trace);
    }

    @Override
    public Trace newAsyncTraceObject() {
        return delegate.newAsyncTraceObject();
    }

    @Override
    public Trace continueAsyncTraceObject(TraceId traceId) {
        return delegate.continueAsyncTraceObject(traceId);
    }


    @Override
    public Trace newTraceObject() {
        return delegate.newTraceObject();
    }

    @Override
    public Trace removeTraceObject() {
        final Trace trace = delegate.removeTraceObject();
//        TODO;
//        long time = trace.getSpanRecorder().getResponseTime();
//        metricRegistry.addResponseTime(time);
        return trace;
    }

    public Metric getRpcMetric(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }

        return this.metricRegistry.getRpcMetric(serviceType);
    }

    public void recordContextMetric(int elapsedTime, boolean error) {
        final ContextMetric contextMetric = this.metricRegistry.getResponseMetric();
        contextMetric.addResponseTime(elapsedTime, error);
    }

    public void recordAcceptResponseTime(String parentApplicationName, short parentApplicationType, int elapsedTime, boolean error) {
        final ContextMetric contextMetric = this.metricRegistry.getResponseMetric();
        contextMetric.addAcceptHistogram(parentApplicationName, parentApplicationType, elapsedTime, error);
    }

    public void recordUserAcceptResponseTime(int elapsedTime, boolean error) {
        final ContextMetric contextMetric = this.metricRegistry.getResponseMetric();
        contextMetric.addUserAcceptHistogram(elapsedTime, error);
    }
}
