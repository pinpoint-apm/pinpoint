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
package com.navercorp.pinpoint.profiler.context.exception;

import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionContext;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapper;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapperFactory;
import com.navercorp.pinpoint.profiler.context.exception.sampler.ExceptionChainSampler;

import java.util.List;

/**
 * @author intr3p1d
 */
public enum ExceptionRecordingState {

    NEW {
        @Override
        public List<ExceptionWrapper> toWrappers(
                ExceptionContext context, ExceptionWrapperFactory factory
        ) {
            return factory.newExceptionWrappers(context);
        }

        @Override
        public void cleanUp(ExceptionContext context) {
            context.cleanContext();
        }
    },
    CONTINUED {
        @Override
        public List<ExceptionWrapper> toWrappers(ExceptionContext context, ExceptionWrapperFactory factory) {
            return null;
        }

        @Override
        public void cleanUp(ExceptionContext context) {
            // do nothing
        }
    },
    CLEAN {
        @Override
        public List<ExceptionWrapper> toWrappers(
                ExceptionContext context, ExceptionWrapperFactory factory
        ) {
            return null;
        }

        @Override
        public void cleanUp(ExceptionContext context) {
            // do nothing
        }
    };

    public static ExceptionRecordingState stateOf(Throwable previous, Throwable current) {
        if (current == null) {
            return CLEAN;
        } else {
            if (isChaining(previous, current)) {
                return CONTINUED;
            }
            return NEW;
        }
    }

    private static boolean isChaining(Throwable previous, Throwable current) {
        if (previous == null && current == null) {
            return false;
        }

        Throwable throwable = current;
        while (throwable != null) {
            if (throwable == previous) {
                return true;
            }
            throwable = throwable.getCause();
        }
        return false;
    }

    public static void flush(
            ExceptionContext context,
            ExceptionChainSampler.SamplingState samplingState,
            ExceptionWrapperFactory factory
    ) {
        if (samplingState.isSampling()) {
            final List<ExceptionWrapper> wrappers = factory.newExceptionWrappers(context);
            if (wrappers != null) {
                context.store(wrappers);
            }
        }
    }

    public boolean needsNewChainId() {
        return this == NEW;
    }

    public void pushThenUpdate(
            ExceptionContext context,
            Throwable current,
            long currentStartTime,
            ExceptionChainSampler.SamplingState samplingState,
            ExceptionWrapperFactory factory
    ) {
        this.push(context, samplingState, factory);
        this.cleanUp(context);
        this.update(context, current, currentStartTime, samplingState);
    }


    private void push(
            ExceptionContext context,
            ExceptionChainSampler.SamplingState samplingState,
            ExceptionWrapperFactory factory
    ) {
        if (samplingState.isSampling()) {
            final List<ExceptionWrapper> wrappers = this.toWrappers(
                    context, factory
            );
            if (wrappers != null) {
                context.store(wrappers);
            }
        }
    }


    public void update(
            ExceptionContext context,
            Throwable current,
            long currentStartTime,
            ExceptionChainSampler.SamplingState samplingState
    ) {
        context.update(current, currentStartTime, samplingState);
    }

    public abstract List<ExceptionWrapper> toWrappers(
            ExceptionContext context,
            ExceptionWrapperFactory factory
    );

    public abstract void cleanUp(ExceptionContext context);
}
