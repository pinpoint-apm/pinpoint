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
package com.navercorp.pinpoint.plugin.jetty;

import com.navercorp.pinpoint.bootstrap.instrument.transformer.PinpointClassFileTransformers;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

public class JettyPlugin implements ProfilerPlugin {

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        context.addApplicationTypeDetector(new JettyDetector());
        JettyConfiguration config = new JettyConfiguration(context.getConfig());
        
        addServerInterceptor(context, config);
        addRequestEditor(context);
    }

    private void addServerInterceptor(ProfilerPluginSetupContext context, JettyConfiguration config){
        context.addClassFileTransformer("org.eclipse.jetty.server.Server", PinpointClassFileTransformers.addInterceptor("com.navercorp.pinpoint.plugin.jetty.interceptor.ServerHandleInterceptor", config.getJettyExcludeUrlFilter()));
    }
    
    private void addRequestEditor(ProfilerPluginSetupContext context) {
        context.addClassFileTransformer("org.eclipse.jetty.server.Request", PinpointClassFileTransformers.addField("com.navercorp.pinpoint.plugin.jetty.interceptor.TraceAccessor"));
    }
}
