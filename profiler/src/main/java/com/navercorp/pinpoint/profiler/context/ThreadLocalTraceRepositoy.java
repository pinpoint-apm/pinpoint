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

package com.navercorp.pinpoint.profiler.context;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricRegistry;
import com.navercorp.pinpoint.profiler.util.NamedThreadLocal;

/**
 * @author emeroad
 * @author Taejin Koo
 */
public class ThreadLocalTraceRepositoy implements TraceRepository {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ThreadLocal<Trace> threadLocal = new NamedThreadLocal<Trace>("Trace");

    private final MetricRegistry metricRegistry;

    private final StorageFactory storageFactory;
    private final Sampler sampler;
    
    private final DefaultTraceFactory defaultTraceFactory;
    private final ActiveTraceLifeCycleEventListener activeTraceEventListener;
    
    private volatile TraceFactory traceFactory;

    // Unique id for tracing a internal stacktrace and calculating a slow time of activethreadcount
    // moved here in order to make codes simpler for now
    private final AtomicLong transactionId = new AtomicLong(0);

    public ThreadLocalTraceRepositoy(TraceContext traceContext, MetricRegistry metricRegistry, StorageFactory storageFactory, Sampler sampler) {
        this(traceContext, metricRegistry, storageFactory, sampler, null);
    }
    
    public ThreadLocalTraceRepositoy(TraceContext traceContext, MetricRegistry metricRegistry, StorageFactory storageFactory, Sampler sampler, ActiveTraceLifeCycleEventListener activeTraceEventListener) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (metricRegistry == null) {
            throw new NullPointerException("metricRegistry must not be null");
        }
        if (storageFactory == null) {
            throw new NullPointerException("storageFactory must not be null");
        }
        if (sampler == null) {
            throw new NullPointerException("sampler must not be null");
        }
        this.metricRegistry = metricRegistry;
        this.storageFactory = storageFactory;
        this.sampler = sampler;
        
        this.defaultTraceFactory = new DefaultTraceFactory(traceContext, storageFactory);
        this.activeTraceEventListener = activeTraceEventListener;

        this.traceFactory = defaultTraceFactory;
    }

    /**
     * Return Trace object after validating whether it can be sampled or not.
     * @return Trace
     */
    @Override
    public Trace currentTraceObject() {
        final Trace trace = threadLocal.get();
        if (trace == null) {
            return null;
        }
        if (trace.canSampled()) {
            return trace;
        }
        return null;
    }

    /**
     * Return Trace object without validating
     * @return
     */
    @Override
    public Trace currentRpcTraceObject() {
        final Trace trace = threadLocal.get();
        if (trace == null) {
            return null;
        }
        return trace;
    }

    @Override
    public Trace currentRawTraceObject() {
        return threadLocal.get();
    }

    @Override
    public Trace disableSampling() {
        checkBeforeTraceObject();
        
        // TODO STATDISABLE, disabled to store statistics for now. createMetricTrace() returns DisableTrace.INSTANCE.
        final Trace metricTrace = traceFactory.createMetricTrace();
        set(metricTrace);
        return metricTrace;
    }

    // continue to trace the request that has been determined to be sampled on previous nodes
    @Override
    public Trace continueTraceObject(final TraceId traceID) {
        checkBeforeTraceObject();

        // TODO need to modify how to bind a datasender
        // always set true because the decision of sampling has been  made on previous nodes
        // TODO need to consider as a target to sample in case Trace object has a sampling flag (true) marked on previous node.
        final boolean sampling = true;
        Trace trace = traceFactory.createDefaultTrace(traceID, sampling);
        set(trace);
        return trace;
    }
    
    @Override
    public Trace continueTraceObject(Trace trace) {
        checkBeforeTraceObject();
        set(trace);
        return trace;
    }

    private void checkBeforeTraceObject() {
        final Trace old = this.threadLocal.get();
        if (old != null) {
            final PinpointException exception = new PinpointException("already Trace Object exist.");
            if (logger.isWarnEnabled()) {
                logger.warn("beforeTrace:{}", old, exception);
            }
            throw exception;
        }
    }

    @Override
    public Trace newTraceObject() {
        return newTraceObject(TraceType.DEFAULT);
    }
    
    @Override
    public Trace newTraceObject(TraceType traceType) {
        checkBeforeTraceObject();
        // TODO need to modify how to inject a datasender
        final boolean sampling = sampler.isSampling();
        if (sampling) {
            // final Storage storage = storageFactory.createStorage();
            Trace trace = traceFactory.createDefaultTrace(nextTransactionId(), traceType, sampling);
            set(trace);
            return trace;
        } else {
            final Trace metricTrace = traceFactory.createMetricTrace();
            set(metricTrace);
            return metricTrace;
        }
    }

    @Override
    public Trace removeTraceObject() {
        final Trace trace = currentRawTraceObject();
        this.threadLocal.remove();

        return trace;
    }
    
    @Override
    public void enableActiveTraceTracking() {
        if (activeTraceEventListener != null) {
            logger.info("enableActiveTraceTracking.");
            this.traceFactory = new ActiveTraceFactory(defaultTraceFactory, activeTraceEventListener);
        }
    }

    @Override
    public void disableActiveTraceTracking() {
        logger.info("disableActiveTraceTracking.");
        this.traceFactory = defaultTraceFactory;
    }
    
    private Trace createMetricTrace() {
        return DisableTrace.INSTANCE;
        // TODO STATDISABLE ,  disabled to store statistics for now
        // return new MetricTrace(traceContext, nextTransactionId());
    }

    private long nextTransactionId() {
        return this.transactionId.getAndIncrement();
    }
    
    public Trace continueAsyncTraceObject(AsyncTraceId traceId, int asyncId, long startTime) {
        checkBeforeTraceObject();
        
        final boolean sampling = true;
        Trace asyncTrace = traceFactory.createAsyncTrace(traceId, asyncId, startTime, sampling);
        set(asyncTrace);        
        return asyncTrace;
    }
    
    private void set(Trace trace) {
        threadLocal.set(trace);
        if (trace instanceof ActiveTrace) {
            ActiveTraceInfo activeTraceInfo = ((ActiveTrace) trace).getActiveTraceInfo();
            Thread currentThread = Thread.currentThread();
            activeTraceInfo.addThreadName(currentThread.getName());
        }
    }
    
}