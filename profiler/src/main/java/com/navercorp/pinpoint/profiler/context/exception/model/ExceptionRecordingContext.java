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

import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionTraceSampler;

import javax.annotation.Nullable;

/**
 * @author intr3p1d
 */
public class ExceptionRecordingContext {

    private static final long EMPTY_EXCEPTION_ID = Long.MIN_VALUE;
    private static final Throwable INITIAL_EXCEPTION = null;

    private Throwable previous = INITIAL_EXCEPTION;
    private ExceptionTraceSampler.SamplingState samplingState = null;
    private long startTime = 0;

    public static ExceptionRecordingContext newContext() {
        return new ExceptionRecordingContext();
    }


    public boolean hasValidExceptionId() {
        return this.samplingState != null && this.samplingState.isSampling();
    }

    public long getExceptionId() {
        return samplingState.currentId();
    }

    public void resetPrevious() {
        setPrevious(null);
    }

    public void resetExceptionId() {
        setSamplingState(ExceptionTraceSampler.DISABLED);
    }

    public void resetStartTime() {
        setStartTime(0);
    }

    public Throwable getPrevious() {
        return previous;
    }

    public void setPrevious(@Nullable Throwable previous) {
        this.previous = previous;
    }

    public ExceptionTraceSampler.SamplingState getSamplingState() {
        return samplingState;
    }

    public void setSamplingState(ExceptionTraceSampler.SamplingState samplingState) {
        this.samplingState = samplingState;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
