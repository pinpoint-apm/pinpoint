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

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerExceptionHandler;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.gson.filter.GsonMethodFilter;
import com.navercorp.pinpoint.plugin.gson.filter.GsonMethodNames;

/**
 * @author ChaYoung You
 */
public class GsonPlugin implements ProfilerPlugin {
    public static final ServiceType GSON_SERVICE_TYPE = ServiceType.of(7000, "GSON", NORMAL_SCHEMA);
    public static final AnnotationKey GSON_ANNOTATION_KEY_JSON_LENGTH = new AnnotationKey(9000, "JSON_LENGTH");
    private static final String GSON_CLASS = "com.google.gson.Gson";
    private static final String GSON_METHODS_INTERCEPTOR = "com.navercorp.pinpoint.plugin.gson.interceptor.GsonMethodInterceptor";
    private static final String GSON_GROUP = "GSON_GROUP";

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    @Override
    public void setup(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassFileTransformerBuilder(GSON_CLASS);
        final MethodTransformerBuilder methodEditorBuilder = classEditorBuilder.editMethods(new GsonMethodFilter(GsonMethodNames.get()));
        methodEditorBuilder.exceptionHandler(new MethodTransformerExceptionHandler() {
            @Override
            public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Throwable {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + targetClassName + "." + targetMethodName, exception);
                }
            }
        });
        methodEditorBuilder.injectInterceptor(GSON_METHODS_INTERCEPTOR).group(GSON_GROUP);
        context.addClassFileTransformer(classEditorBuilder.build());
    }
}
