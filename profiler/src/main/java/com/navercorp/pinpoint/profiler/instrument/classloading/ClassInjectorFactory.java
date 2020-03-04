/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.classloading;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassInjectorFactory {

    private final InstrumentEngine instrumentEngine;
    private final BootstrapCore bootstrapCore;

    public ClassInjectorFactory(InstrumentEngine instrumentEngine, BootstrapCore bootstrapCore) {
        this.instrumentEngine = Assert.requireNonNull(instrumentEngine, "instrumentEngine");
        this.bootstrapCore = Assert.requireNonNull(bootstrapCore, "bootstrapCore");
    }

    public ClassInjector newClassInjector(PluginConfig pluginConfig) {
        return new JarProfilerPluginClassInjector(pluginConfig, instrumentEngine, bootstrapCore);
    }
}
