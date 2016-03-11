/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.hystrix;

import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandTransformer;

/**
 * Any Pinpoint profiler plugin must implement ProfilerPlugin interface.
 * ProfilerPlugin declares only one method {@link #setup(ProfilerPluginSetupContext)}.
 * You should implement the method to do whatever you need to setup your plugin with the passed ProfilerPluginSetupContext object.
 *
 * @author Jiaqi Feng
 */
public class HystrixPlugin implements ProfilerPlugin, TransformTemplateAware {
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        addTransformers();
    }

    private void addTransformers() {
        transformTemplate.transform("com.netflix.hystrix.HystrixCommand", new HystrixCommandTransformer());
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
