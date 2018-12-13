/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.openwhisk.accessor.PinpointTraceAccessor;
import com.navercorp.pinpoint.plugin.openwhisk.interceptor.*;
import com.navercorp.pinpoint.plugin.openwhisk.setter.TraceContextSetter;

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

/**
 * @author Seonghyun Oh
 */
public class OpenwhiskPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final OpenwhiskConfig config = new OpenwhiskConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            OpenwhiskDetector openwhiskDetector = new OpenwhiskDetector();
            ServiceType detectedApplicationType = openwhiskDetector.detectApplicationType();
            if (detectedApplicationType != ServiceType.UNKNOWN) {
                logger.info("Detected application type : {}", detectedApplicationType);
                if (!context.registerApplicationType(detectedApplicationType)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), detectedApplicationType);
                }
            }
        }

        final String transformTargetName = config.getTransformTargetName();

        if (StringUtils.isEmpty(transformTargetName)) {
            logger.warn("Not found 'profiler.openwhisk.transform.targetname' in config");
        } else {
            transformTemplate.transform(toClassName(transformTargetName), EntryPointTransform.class);
            transformTemplate.transform("org.apache.openwhisk.common.tracing.NoopTracer$", NoopTracerTransform.class);
            transformTemplate.transform("org.apache.openwhisk.common.TransactionId$", TransactionIdTransform.class);
            transformTemplate.transform("org.apache.openwhisk.common.TransactionMetadata", TransactionMetadataTransform.class);
            transformTemplate.transform("org.apache.openwhisk.common.StartMarker", StartMarkerTransform.class);
            transformTemplate.transform("org.apache.openwhisk.core.connector.ActivationMessage", ActivationMessageTransform.class);
            transformTemplate.transform("org.apache.openwhisk.connector.kafka.KafkaProducerConnector", KafkaProducerConnectorTransform.class);
        }
    }

    public static class TransactionMetadataTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            return target.toBytecode();
        }
    }

    public static class NoopTracerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // add `apply` method interceptor
            final MethodFilter applyMethodFilter = MethodFilters.chain(
                    MethodFilters.name("setTraceContext")
            );
            for (InstrumentMethod method : target.getDeclaredMethods(applyMethodFilter)) {
                try {
                    method.addInterceptor(NoopTracerSetTraceContextInterceptor.class);
                    break;
                } catch (Exception e) {
                    final PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method " + method, e);
                    }
                }
            }
            return target.toBytecode();
        }
    }

    public static class ActivationMessageTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addSetter(TraceContextSetter.class, "traceContext", true);
            return target.toBytecode();
        }
    }

    public static class TransactionIdTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // add `started` method interceptor
            final MethodFilter startedMethodFilter = MethodFilters.chain(
                    MethodFilters.name("started$extension"),
                    MethodFilters.argAt(2, "org.apache.openwhisk.common.LogMarkerToken"),
                    MethodFilters.argAt(3, "scala.Function0")
            );
            for (InstrumentMethod method : target.getDeclaredMethods(startedMethodFilter)) {
                try {
                    method.addInterceptor(TransactionIdStartedInterceptor.class);
                } catch (Exception e) {
                    final PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method " + method, e);
                    }
                }
            }

            // add `finished` method interceptor
            final MethodFilter finishedMethodFilter = MethodFilters.chain(
                    MethodFilters.name("finished$extension"),
                    MethodFilters.argAt(2, "org.apache.openwhisk.common.StartMarker")
            );
            for (InstrumentMethod method : target.getDeclaredMethods(finishedMethodFilter)) {
                try {
                    method.addInterceptor(TransactionIdFinishedInterceptor.class);
                } catch (Exception e) {
                    final PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method " + method, e);
                    }
                }
            }

            // add `failed` method interceptor
            final MethodFilter failedMethodFilter = MethodFilters.chain(
                    MethodFilters.name("failed$extension"),
                    MethodFilters.argAt(2, "org.apache.openwhisk.common.StartMarker"),
                    MethodFilters.argAt(3, "scala.Function0")
            );
            for (InstrumentMethod method : target.getDeclaredMethods(failedMethodFilter)) {
                try {
                    method.addInterceptor(TransactionIdFailedInterceptor.class);
                } catch (Exception e) {
                    final PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method " + method, e);
                    }
                }
            }

            // add `mark` method interceptor
            final MethodFilter markMethodFilter = MethodFilters.chain(
                    MethodFilters.name("mark$extension"),
                    MethodFilters.argAt(2, "org.apache.openwhisk.common.LogMarkerToken"),
                    MethodFilters.argAt(3, "scala.Function0")
            );
            for (InstrumentMethod method : target.getDeclaredMethods(markMethodFilter)) {
                try {
                    method.addInterceptor(TransactionIdMarkInterceptor.class);
                } catch (Exception e) {
                    final PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method " + method, e);
                    }
                }
            }

            return target.toBytecode();
        }
    }

    public static class KafkaProducerConnectorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            // add `apply` method interceptor
            final MethodFilter applyMethodFilter = MethodFilters.chain(
                    MethodFilters.name("send")
            );
            for (InstrumentMethod method : target.getDeclaredMethods(applyMethodFilter)) {
                try {
                    method.addInterceptor(KafkaProducerSendInterceptor.class);
                    break;
                } catch (Exception e) {
                    final PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method " + method, e);
                    }
                }
            }
            return target.toBytecode();
        }
    }


    public static class StartMarkerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            target.addField(PinpointTraceAccessor.class);

            // add `apply` method interceptor
            final MethodFilter applyMethodFilter = MethodFilters.chain(
                    MethodFilters.name("copy")
            );
            for (InstrumentMethod method : target.getDeclaredMethods(applyMethodFilter)) {
                try {
                    method.addInterceptor(StartMarkerCopyInterceptor.class);
                    break;
                } catch (Exception e) {
                    final PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method " + method, e);
                    }
                }
            }
            return target.toBytecode();
        }
    }

    public static class EntryPointTransform implements TransformCallback {

        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());


        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final OpenwhiskConfig config = new OpenwhiskConfig(instrumentor.getProfilerConfig());

            final String transformTargetName = config.getTransformTargetName();
            final String targetMethodName = toMethodName(transformTargetName);
            final List<String> transformTargetParameters = config.getTransformTargetParameters();

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name(targetMethodName))) {
                if (checkSuitableMethod(method, transformTargetParameters)) {
                    logger.info("addInterceptor={}", Arrays.asList(method.getParameterTypes()));
                    method.addInterceptor(TransactionIdCreateInterceptor.class);
                } else {
                    logger.info("params={}", Arrays.asList(method.getParameterTypes()));
                }
            }
            return target.toBytecode();
        }

        static String toMethodName(String fullQualifiedMethodName) {
            final int methodBeginPosition = fullQualifiedMethodName.lastIndexOf('.');
            if (methodBeginPosition <= 0 || methodBeginPosition + 1 >= fullQualifiedMethodName.length()) {
                throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
            }

            return fullQualifiedMethodName.substring(methodBeginPosition + 1).trim();
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

    }

    private String toClassName(String fullQualifiedMethodName) {
        final int classEndPosition = fullQualifiedMethodName.lastIndexOf('.');
        if (classEndPosition <= 0) {
            throw new IllegalArgumentException("invalid full qualified method name(" + fullQualifiedMethodName + "). not found method");
        }

        return fullQualifiedMethodName.substring(0, classEndPosition).trim();
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

}
