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

package com.navercorp.pinpoint.profiler.context;

import com.google.inject.AbstractModule;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.context.storage.LogStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.LoggingDataSender;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockApplicationContext extends DefaultApplicationContext {
    private InterceptorRegistryBinder interceptorRegistryBinder;


    public MockApplicationContext(AgentOption agentOption, InterceptorRegistryBinder binder, ModuleFactory moduleFactory) {
        super(agentOption, binder, moduleFactory);
        if (binder == null) {
            throw new NullPointerException("agentOption must not be null");
        }
        this.interceptorRegistryBinder = binder;
    }

    @Override
    public void close() {
        super.close();
        interceptorRegistryBinder.unbind();
    }

}
