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

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;

public class JettyPlugin implements ProfilerPlugin, JettyConstants{

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        context.addApplicationTypeDetector(new JettyDetector());
        JettyConfiguration config = new JettyConfiguration(context.getConfig());
        addServerInterceptor(context, config);
        addRequestEditor(context);
    }

    private void addServerInterceptor(ProfilerPluginSetupContext context, JettyConfiguration config){
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("org.eclipse.jetty.server.Server");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.jetty.interceptor.ServerHandleInterceptor", config.getJettyExcludeUrlFilter());
        context.addClassFileTransformer(builder.build());
    }
    
    private void addRequestEditor(ProfilerPluginSetupContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("org.eclipse.jetty.server.Request");
        builder.injectMetadata(METADATA_TRACE);
        context.addClassFileTransformer(builder.build());
    }
}
