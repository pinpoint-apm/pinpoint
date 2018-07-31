/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.navercorp.pinpoint.common.util.Assert;

import java.lang.instrument.Instrumentation;

/**
 * @author Taejin Koo
 */
public class PinpointModuleHolder {

    private final Module module;
    private final Instrumentation instrumentation;

    private final Object lock = new Object();
    private ModuleInstanceManager moduleInstanceManager;

    public PinpointModuleHolder(Module module, Instrumentation instrumentation) {
        this.module = Assert.requireNonNull(module, "module must not be null");
        this.instrumentation = Assert.requireNonNull(instrumentation, "instrumentation must not be null");
    }

    public Module getModule() {
        return module;
    }

    ModuleInstanceManager getInstanceManager(Injector injector) {
        synchronized (lock) {
            if (this.moduleInstanceManager == null) {
                this.moduleInstanceManager = new PinpointModuleInstanceManager(injector, instrumentation);
            }
        }

        return moduleInstanceManager;
    }

}
