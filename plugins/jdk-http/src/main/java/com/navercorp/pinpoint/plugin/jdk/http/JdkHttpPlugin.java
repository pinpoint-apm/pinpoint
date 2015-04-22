package com.navercorp.pinpoint.plugin.jdk.http;
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

import static com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassConditions.*;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;

/**
 * 
 * @author Jongho Moon
 *
 */
public class JdkHttpPlugin implements ProfilerPlugin {

    @Override
    public void setup(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("sun.net.www.protocol.http.HttpURLConnection");
    
        builder.injectFieldAccessor("connected");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdk.http.interceptor.HttpURLConnectionInterceptor");
        
        // JDK 8
        builder.conditional(hasField("connecting", "boolean"), 
                new ConditionalClassFileTransformerSetup() {
                    @Override
                    public void setup(ConditionalClassFileTransformerBuilder conditional) {
                        conditional.injectFieldAccessor("connecting");
                    }
                }
        );
        
        context.addClassFileTransformer(builder.build());
    }

}
