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

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import com.navercorp.pinpoint.bootstrap.context.AsyncTrace;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.storage.Storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
@Deprecated
public class DefaultAsyncTrace implements AsyncTrace {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAsyncTrace.class);
    private static final boolean isDebug = logger.isDebugEnabled();
    private static final boolean isTrace = logger.isTraceEnabled();


    public static final int NON_REGIST = -1;

    private final AtomicInteger state = new AtomicInteger(STATE_INIT);

    private int asyncId = NON_REGIST;
    private SpanEvent spanEvent;

    private Storage storage;
    private TimerTask timeoutTask;

    public DefaultAsyncTrace(SpanEvent spanEvent) {
        this.spanEvent = spanEvent;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void setTimeoutTask(TimerTask timeoutTask) {
        this.timeoutTask = timeoutTask;
    }

    @Override
    public void setAsyncId(int asyncId) {
        this.asyncId = asyncId;
    }

    @Override
    public int getAsyncId() {
        return asyncId;
    }


    private Object attachObject;

    @Override
    public Object getAttachObject() {
        return attachObject;
    }

    @Override
    public void setAttachObject(Object attachObject) {
        this.attachObject = attachObject;
    }

    @Override
    public void traceBlockBegin() {
    }

    @Override
    public void markBeforeTime() {
        spanEvent.markStartTime();
    }

    @Override
    public long getBeforeTime() {
        return spanEvent.getStartTime();
    }

    @Override
    public void traceBlockEnd() {
        logSpan(this.spanEvent);
    }

    @Override
    public void markAfterTime() {
        spanEvent.markAfterTime();
    }


    @Override
    public void recordApi(MethodDescriptor methodDescriptor) {
        if (methodDescriptor == null) {
            return;
        }
        if (methodDescriptor.getApiId() == 0) {
            recordAttribute(AnnotationKey.API, methodDescriptor.getFullName());
        } else {
            spanEvent.setApiId(methodDescriptor.getApiId());
        }
    }



    @Override
    public void recordException(Object result) {
        if (result instanceof Throwable) {
            Throwable th = (Throwable) result;
            String drop = StringUtils.drop(th.getMessage());

            recordAttribute(AnnotationKey.EXCEPTION, drop);
        }
    }

    @Override
    public void recordAttribute(final AnnotationKey key, final String value) {
        spanEvent.addAnnotation(new Annotation(key.getCode(), value));
    }

    @Override
    public void recordAttribute(final AnnotationKey key, final int value) {
        spanEvent.addAnnotation(new Annotation(key.getCode(), value));
    }


    @Override
    public void recordAttribute(final AnnotationKey key, final Object value) {
        spanEvent.addAnnotation(new Annotation(key.getCode(), value));
    }

    @Override
    public void recordServiceType(final ServiceType serviceType) {
        this.spanEvent.setServiceType(serviceType.getCode());
    }

    @Override
    public void recordRpcName(final String rpcName) {
        this.spanEvent.setRpc(rpcName);

    }


    @Override
    public void recordDestinationId(String destinationId) {
        this.spanEvent.setDestinationId(destinationId);
    }

    @Override
    public void recordEndPoint(final String endPoint) {
        this.spanEvent.setEndPoint(endPoint);
    }

    private void logSpan(SpanEvent spanEvent) {
        try {
            if (isTrace) {
                Thread thread = Thread.currentThread();
                logger.trace("[WRITE SpanEvent]{} CurrentThreadID={} CurrentThreadName={}", spanEvent, thread.getId(), thread.getName());
            }
            this.storage.store(spanEvent);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public int getState() {
        return state.get();
    }

    public void timeout() {
        if (state.compareAndSet(STATE_INIT, STATE_TIMEOUT)) {
            // TODO 
        }
    }

    public boolean fire() {
        if (state.compareAndSet(STATE_INIT, STATE_FIRE)) {
            if (timeoutTask != null) {
                this.timeoutTask.cancel();
            }
            return true;
        }
        return false;
    }


}
