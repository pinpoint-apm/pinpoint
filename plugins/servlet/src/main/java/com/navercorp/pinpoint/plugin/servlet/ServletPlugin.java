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
package com.navercorp.pinpoint.plugin.servlet;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;

/**
 * @author Jongho Moon
 *
 */
public class ServletPlugin implements ProfilerPlugin {

    @Override
    public void setup(ProfilerPluginContext context) {
        addHttpServletEditor(context);
    }

    private void addHttpServletEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("javax.servlet.http.HttpServlet");
        
        MethodTransformerBuilder doGetBuilder = builder.editMethod("doGet", "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");
        doGetBuilder.injectInterceptor("com.navercorp.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor", ServletConstants.SERVLET);
        
        MethodTransformerBuilder doPostBuilder = builder.editMethod("doPost", "javax.servlet.http.HttpServletRequest", "javax.servlet.http.HttpServletResponse");
        doPostBuilder.injectInterceptor("com.navercorp.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor", ServletConstants.SERVLET);
        
        context.addClassFileTransformer(builder.build());
    }
}
