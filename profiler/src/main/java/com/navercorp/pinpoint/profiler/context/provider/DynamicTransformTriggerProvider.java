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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.profiler.ClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.DynamicTransformService;

import java.lang.instrument.Instrumentation;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DynamicTransformTriggerProvider implements Provider<DynamicTransformTrigger> {

    private final Instrumentation instrumentation;
    private final ClassFileTransformerDispatcher listener;

    @Inject
    public DynamicTransformTriggerProvider(Instrumentation instrumentation, ClassFileTransformerDispatcher listener) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (listener == null) {
            throw new NullPointerException("listener must not be null");
        }

        this.instrumentation = instrumentation;
        this.listener = listener;
    }

    @Override
    public DynamicTransformTrigger get() {

        return new DynamicTransformService(instrumentation, listener);
    }
}
