/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.context.exception.model;

import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecordingState;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionTraceSampler;
import com.navercorp.pinpoint.profiler.context.exception.storage.ExceptionStorage;

import java.util.List;

/**
 * @author intr3p1d
 */
public class DefaultExceptionContext implements ExceptionContext {

    private static final long EMPTY_EXCEPTION_ID = Long.MIN_VALUE;
    private ExceptionTraceSampler.SamplingState samplingState = ExceptionTraceSampler.DISABLED;

    private final ExceptionStorage storage;
    private final ExceptionContextValue contextValue;

    public DefaultExceptionContext(ExceptionStorage storage) {
        this.storage = storage;
        this.contextValue = new ExceptionContextValue();
    }

    @Override
    public void store(List<ExceptionWrapper> wrappers) {
        storage.store(wrappers);
    }

    @Override
    public void flush() {
        storage.flush();
    }


    @Override
    public void setWrapped(Throwable throwable) {
        contextValue.setPrevious(throwable);
    }

    @Override
    public ExceptionRecordingState stateOf(Throwable throwable) {
        return ExceptionRecordingState.stateOf(contextValue.getPrevious(), throwable);
    }

    @Override
    public void chainStart(long startTime, ExceptionTraceSampler.SamplingState samplingState) {
        contextValue.setStartTime(startTime);
        setSamplingState(samplingState);
    }

    @Override
    public void reset() {
        contextValue.setPrevious(null);
        setSamplingState(ExceptionTraceSampler.DISABLED);
        contextValue.setStartTime(0);
    }

    @Override
    public boolean hasValidExceptionId() {
        return this.samplingState != null && this.samplingState.isSampling();
    }

    @Override
    public long getExceptionId() {
        if (samplingState != null) {
            return samplingState.currentId();
        } else {
            return EMPTY_EXCEPTION_ID;
        }
    }

    @Override
    public long getStartTime() {
        return this.contextValue.getStartTime();
    }

    @Override
    public Throwable getPrevious() {
        return this.contextValue.getPrevious();
    }

    @Override
    public ExceptionTraceSampler.SamplingState getSamplingState() {
        return this.samplingState;
    }

    public void setSamplingState(ExceptionTraceSampler.SamplingState samplingState) {
        this.samplingState = samplingState;
    }
}
