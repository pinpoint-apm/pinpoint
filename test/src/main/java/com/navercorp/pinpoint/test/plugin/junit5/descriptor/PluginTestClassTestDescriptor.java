/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin.junit5.descriptor;

import com.navercorp.pinpoint.test.plugin.PluginTestInstance;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.platform.engine.UniqueId;

public class PluginTestClassTestDescriptor extends ClassTestDescriptor {

    final PluginTestInstance pluginTestInstance;

    public PluginTestClassTestDescriptor(UniqueId uniqueId, Class<?> testClass, JupiterConfiguration configuration, PluginTestInstance pluginTestInstance) {
        super(uniqueId, testClass, configuration);
        this.pluginTestInstance = pluginTestInstance;
    }

    @Override
    public JupiterEngineExecutionContext before(JupiterEngineExecutionContext context) {
        return this.pluginTestInstance.execute(() -> {
            return super.before(context);
        }, false);
    }

    @Override
    public void after(JupiterEngineExecutionContext context) {
        this.pluginTestInstance.execute(() -> {
            super.after(context);
            return null;
        }, false);
    }
}
