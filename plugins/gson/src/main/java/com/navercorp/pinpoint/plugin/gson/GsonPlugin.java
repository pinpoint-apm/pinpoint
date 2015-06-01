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

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
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
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder("com.google.gson.Gson");
        
        classEditorBuilder.editMethods(MethodFilters.name("fromJson")).injectInterceptor("com.navercorp.pinpoint.plugin.gson.interceptor.FromJsonInterceptor").group(GSON_GROUP);
        classEditorBuilder.editMethods(MethodFilters.name("toJson")).injectInterceptor("com.navercorp.pinpoint.plugin.gson.interceptor.ToJsonInterceptor").group(GSON_GROUP);
        
        context.addClassFileTransformer(classEditorBuilder.build());
    }
}
