/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.profiler.context.module.ApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;

import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.profiler.DefaultAgent;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 * @author jaehong.kim
 */
public class PluginTestAgent extends DefaultAgent {

    public PluginTestAgent(AgentOption agentOption) {
        super(agentOption);
    }

    @Override
    protected ApplicationContext newApplicationContext(AgentOption agentOption) {

        PluginApplicationContextModule pluginApplicationContextModule = new PluginApplicationContextModule();
        ModuleFactory moduleFactory = new OverrideModuleFactory(pluginApplicationContextModule);
        DefaultApplicationContext applicationContext = new DefaultApplicationContext(agentOption, moduleFactory);

        exportVerifier(applicationContext);

        return applicationContext ;

    }

    private void exportVerifier(DefaultApplicationContext applicationContext) {
        PluginVerifierExternalAdaptor adaptor = new PluginVerifierExternalAdaptor(applicationContext);
        PluginTestVerifierHolder.setInstance(adaptor);
    }


}
