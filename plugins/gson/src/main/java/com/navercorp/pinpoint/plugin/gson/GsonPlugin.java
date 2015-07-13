/**
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
package com.navercorp.pinpoint.plugin.gson;

import static com.navercorp.pinpoint.common.trace.HistogramSchema.*;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.BaseClassFileTransformer;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author ChaYoung You
 */
public class GsonPlugin implements ProfilerPlugin {
    public static final ServiceType GSON_SERVICE_TYPE = ServiceType.of(5010, "GSON", NORMAL_SCHEMA);
    public static final AnnotationKey GSON_ANNOTATION_KEY_JSON_LENGTH = new AnnotationKey(9000, "gson.json.length");

    private static final String GSON_GROUP = "GSON_GROUP";

    @Override
    public void setup(ProfilerPluginContext context) {
        context.addClassFileTransformer("com.google.gson.Gson", new BaseClassFileTransformer(context) {
            
            @Override
            protected byte[] transform(ProfilerPluginContext pluginContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = pluginContext.getInstrumentClass(loader, className, classfileBuffer);
                InterceptorGroup group = pluginContext.getInterceptorGroup(GSON_GROUP); 
                
                for (MethodInfo m : target.getDeclaredMethods(MethodFilters.name("fromJson"))) {
                    m.addInterceptor("com.navercorp.pinpoint.plugin.gson.interceptor.FromJsonInterceptor", group);
                }
                
                for (MethodInfo m : target.getDeclaredMethods(MethodFilters.name("toJson"))) {
                    m.addInterceptor("com.navercorp.pinpoint.plugin.gson.interceptor.ToJsonInterceptor", group);
                }
                
                return target.toBytecode();
            }
        });
    }
}
