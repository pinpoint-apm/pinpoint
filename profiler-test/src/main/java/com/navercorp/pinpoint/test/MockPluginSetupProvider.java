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

package com.navercorp.pinpoint.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;

/**
 * @author HyunGil Jeong
 */
public class MockPluginSetupProvider implements Provider<PluginSetup> {

    private final PluginSetup pluginSetup;

    @Inject
    public MockPluginSetupProvider(InstrumentEngine instrumentEngine, DynamicTransformTrigger dynamicTransformTrigger) {
        this.pluginSetup = new MockPluginSetup(instrumentEngine, dynamicTransformTrigger);
    }

    @Override
    public PluginSetup get() {
        return pluginSetup;
    }
}
