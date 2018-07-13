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
import com.navercorp.pinpoint.plugin.openwhisk.accessor.PinpointTraceAccessor;

import java.security.ProtectionDomain;

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
            return;
        }

        OpenwhiskDetector openwhiskDetector = new OpenwhiskDetector();
        context.addApplicationTypeDetector(openwhiskDetector);


        transformTemplate.transform(config.getTransformTargetName(), new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                final InstrumentMethod method = target.getDeclaredMethod("apply", "akka.http.scaladsl.server.RequestContext");
                method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.TransactionIdCreateInterceptor");

                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.connector.kafka.KafkaProducerConnector", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                // add `apply` method interceptor
                final MethodFilter applyMethodFilter = MethodFilters.chain(
                        MethodFilters.name("send")
                );
                for (InstrumentMethod method : target.getDeclaredMethods(applyMethodFilter)) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.KafkaProducerSendInterceptor");
                        break;
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }
                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.core.connector.ActivationMessage", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addSetter("com.navercorp.pinpoint.plugin.openwhisk.setter.TraceContextSetter", "traceContext", true);
                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.common.tracing.NoopTracer$", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                // add `apply` method interceptor
                final MethodFilter applyMethodFilter = MethodFilters.chain(
                        MethodFilters.name("setTraceContext")
                );
                for (InstrumentMethod method : target.getDeclaredMethods(applyMethodFilter)) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.NoopTracerSetTraceContextInterceptor");
                        break;
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }
                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.common.TransactionId$", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                // add `started` method interceptor
                final MethodFilter startedMethodFilter = MethodFilters.chain(
                        MethodFilters.name("started$extension"),
                        MethodFilters.argAt(2, "whisk.common.LogMarkerToken"),
                        MethodFilters.argAt(3, "scala.Function0")
                );
                for (InstrumentMethod method : target.getDeclaredMethods(startedMethodFilter)) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.TransactionIdStartedInterceptor");
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }

                // add `finished` method interceptor
                final MethodFilter finishedMethodFilter = MethodFilters.chain(
                        MethodFilters.name("finished$extension"),
                        MethodFilters.argAt(2, "whisk.common.StartMarker")
                );
                for (InstrumentMethod method : target.getDeclaredMethods(finishedMethodFilter)) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.TransactionIdFinishedInterceptor");
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }

                // add `failed` method interceptor
                final MethodFilter failedMethodFilter = MethodFilters.chain(
                        MethodFilters.name("failed$extension"),
                        MethodFilters.argAt(2, "whisk.common.StartMarker"),
                        MethodFilters.argAt(3, "scala.Function0")
                );
                for (InstrumentMethod method : target.getDeclaredMethods(failedMethodFilter)) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.TransactionIdFailedInterceptor");
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }

                // add `mark` method interceptor
                final MethodFilter markMethodFilter = MethodFilters.chain(
                        MethodFilters.name("mark$extension"),
                        MethodFilters.argAt(2, "whisk.common.LogMarkerToken"),
                        MethodFilters.argAt(3, "scala.Function0")
                );
                for (InstrumentMethod method : target.getDeclaredMethods(markMethodFilter)) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.TransactionIdMarkInterceptor");
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }

                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.common.StartMarker", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(AsyncContextAccessor.class.getName());
                target.addField(PinpointTraceAccessor.class.getName());

                // add `copy` method interceptor
                final MethodFilter copyMethodFilter = MethodFilters.chain(
                        MethodFilters.name("copy")
                );
                for (InstrumentMethod method : target.getDeclaredMethods(copyMethodFilter)) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.openwhisk.interceptor.StartMarkerCopyInterceptor");
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }

                return target.toBytecode();
            }
        });

        transformTemplate.transform("whisk.common.TransactionMetadata", new TransformCallback() {
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
