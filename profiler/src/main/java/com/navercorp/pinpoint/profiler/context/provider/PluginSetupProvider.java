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
import com.navercorp.pinpoint.profiler.context.ApplicationContext;
import com.navercorp.pinpoint.profiler.plugin.DefaultPluginSetup;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginSetupProvider implements Provider<PluginSetup> {

    private ApplicationContext applicationContext;

    @Inject
    public PluginSetupProvider(ApplicationContext applicationContext) {
        if (applicationContext == null) {
            throw new NullPointerException("applicationContext must not be null");
        }

        this.applicationContext = applicationContext;
    }

    @Override
    public PluginSetup get() {
        return new DefaultPluginSetup(applicationContext);
    }
}
