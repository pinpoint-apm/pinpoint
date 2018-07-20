/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.akka.http;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;


public class AkkaHttpPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final AkkaHttpConfig config = new AkkaHttpConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("Disable akka http plugin");
            return;
        }

        final String transformTargetName = config.getTransformTargetName();
        final List<String> transformTargetParameters = config.getTransformTargetParameters();

        if (StringUtils.isEmpty(transformTargetName)) {
            logger.warn("Not found 'profiler.akka.http.transform.targetname' in config");
        } else {
            try {
                final String className = toClassName(transformTargetName);
                final String methodName = toMethodName(transformTargetName);
                logger.info("Add request handler method for Akka HTTP Server. class={}, method={}", className, methodName);

                transformDirectives(className, methodName, transformTargetParameters);
                transformRequestContext();
                transformHttpRequest();
            } catch (IllegalArgumentException e) {
                logger.warn("can't find target '{}' value={}", AkkaHttpConfig.KEY_TRANSFORM_TARGET_NAME, transformTargetName);
            }
        }
    }

    private String toClassName(String fullQualifiedMethodName) {
        final int classEndPosition = fullQualifiedMethodName.lastIndexOf('.');
        if (classEndPosition <= 0) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(0, classEndPosition).trim();
    }

    private String toMethodName(String fullQualifiedMethodName) {
        final int methodBeginPosition = fullQualifiedMethodName.lastIndexOf('.');
        if (methodBeginPosition <= 0 || methodBeginPosition + 1 >= fullQualifiedMethodName.length()) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(methodBeginPosition + 1).trim();
    }

    private void transformDirectives(String clazzName, final String methodName, final List<String> methodParameters) {
        transformTemplate.transform(clazzName, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(methodName))) {
                    if (checkSuitableMethod(method, methodParameters)) {
                        logger.info("addInterceptor={}", Arrays.asList(method.getParameterTypes()));
                        method.addInterceptor(AkkaHttpConstants.DIRECTIVE_INTERCEPTOR);
                    } else {
                        logger.info("params={}", Arrays.asList(method.getParameterTypes()));
                    }
                }
                return target.toBytecode();
            }
        });
    }

    private boolean checkSuitableMethod(InstrumentMethod method, List<String> parameters) {
        if (method == null) {
            return false;
        }

        String[] parameterTypes = method.getParameterTypes();
        int parameterSize = parameters.size();

        if (ArrayUtils.getLength(parameterTypes) != parameterSize) {
            return false;
        }
        for (int i = 0; i < parameterSize; i++) {
            if (!parameterTypes[i].equals(parameters.get(i))) {
                return false;
            }
        }
        return true;
    }

    private void transformRequestContext() {
        transformTemplate.transform("akka.http.scaladsl.server.RequestContextImpl", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());

                final InstrumentMethod completeMethod = target.getDeclaredMethod("complete", "akka.http.scaladsl.marshalling.ToResponseMarshallable");
                completeMethod.addScopedInterceptor(AkkaHttpConstants.REQUEST_CONTEXT_COMPLETE_INTERCEPTOR, "test", ExecutionPolicy.ALWAYS);

                final InstrumentMethod redirectMethod = target.getDeclaredMethod("redirect", "akka.http.scaladsl.model.Uri", "akka.http.scaladsl.model.StatusCodes$Redirection");
                redirectMethod.addInterceptor(AkkaHttpConstants.REQUEST_CONTEXT_REDIRECT_INTERCEPTOR);

                final InstrumentMethod failMethod = target.getDeclaredMethod("fail", "java.lang.Throwable");
                failMethod.addInterceptor(AkkaHttpConstants.REQUEST_CONTEXT_FAIL_INTERCEPTOR);

                final InstrumentMethod copyMethod = target.getDeclaredMethod("copy", "akka.http.scaladsl.model.HttpRequest",
                        "akka.http.scaladsl.model.Uri$Path", "scala.concurrent.ExecutionContextExecutor", "akka.stream.Materializer", "akka.event.LoggingAdapter",
                        "akka.http.scaladsl.settings.RoutingSettings", "akka.http.scaladsl.settings.ParserSettings");
                copyMethod.addInterceptor(AkkaHttpConstants.REQUEST_CONTEXT_COPY_INTERCEPTOR);
                return target.toBytecode();
            }
        });
    }

    private void transformHttpRequest() {
        transformTemplate.transform("akka.http.javadsl.model.HttpRequest", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());

                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
