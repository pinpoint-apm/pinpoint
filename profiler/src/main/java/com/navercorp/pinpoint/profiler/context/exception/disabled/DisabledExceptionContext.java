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
package com.navercorp.pinpoint.profiler.context.exception.disabled;

import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecordingState;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContext;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContextValue;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapper;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionChainSampler;

import java.util.List;

/**
 * @author intr3p1d
 */
public class DisabledExceptionContext implements ExceptionContext {

    public static final DisabledExceptionContext INSTANCE = new DisabledExceptionContext();

    @Override
    public void store(List<ExceptionWrapper> wrappers) {

    }

    @Override
    public void flush() {

    }

    @Override
    public void update(Throwable throwable, long startTime, ExceptionChainSampler.SamplingState samplingState) {
    }

    @Override
    public ExceptionRecordingState stateOf(Throwable throwable) {
        return null;
    }

    @Override
    public void cleanContext() {
    }

    @Override
    public boolean hasValidExceptionId() {
        return false;
    }

    @Override
    public ExceptionContextValue getContextValue() {
        return null;
    }

    @Override
    public ExceptionChainSampler.SamplingState getSamplingState() {
        return null;
    }

    @Override
    public Throwable getPrevious() {
        return null;
    }

    @Override
    public long getExceptionId() {
        return 0;
    }

    @Override
    public long getStartTime() {
        return 0;
    }
}
