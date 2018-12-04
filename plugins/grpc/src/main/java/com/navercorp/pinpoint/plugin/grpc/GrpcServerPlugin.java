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

package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.Assert;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author Taejin Koo
 */
class GrpcServerPlugin {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TransformTemplate transformTemplate;
    private final GrpcConfig grpcConfig;

    GrpcServerPlugin(TransformTemplate transformTemplate, GrpcConfig grpcConfig) {
        this.transformTemplate = Assert.requireNonNull(transformTemplate, "transformTemplate must not be null");
        this.grpcConfig = Assert.requireNonNull(grpcConfig, "grpcConfig must not be null");
    }

    void addInterceptor() {
        transformTemplate.transform("io.grpc.internal.ServerImpl$ServerTransportListenerImpl", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                InstrumentMethod streamCreatedMethod = target.getDeclaredMethod("streamCreated",
                        "io.grpc.internal.ServerStream", "java.lang.String", "io.grpc.Metadata");
                if (streamCreatedMethod != null) {
                    streamCreatedMethod.addInterceptor("com.navercorp.pinpoint.plugin.grpc.interceptor.server.ServerStreamCreatedInterceptor");
                } else {
                    if (isDebug) {
                        logger.debug("can't find streamCreated method");
                    }
                }

                return target.toBytecode();
            }
        });

        transformTemplate.transform("io.grpc.internal.AbstractServerStream", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor");

                return target.toBytecode();
            }
        });

        transformTemplate.transform("io.grpc.ServerCall$Listener", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor");

                return target.toBytecode();
            }
        });

        transformTemplate.transform("io.grpc.internal.ServerCallImpl", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                target.addGetter("com.navercorp.pinpoint.plugin.grpc.field.accessor.ServerStreamGetter", "stream");

                return target.toBytecode();
            }
        });

        transformTemplate.transform("io.grpc.stub.ServerCalls$UnaryServerCallHandler", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                addStartCallMethodInterceptor(target);

                return target.toBytecode();
            }
        });


        transformTemplate.transform("io.grpc.stub.ServerCalls$UnaryServerCallHandler$UnaryServerCallListener", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                addListenerMethod(target, true);

                return target.toBytecode();
            }
        });

        if (grpcConfig.isServerStreamingEnable()) {
            transformTemplate.transform("io.grpc.stub.ServerCalls$StreamingServerCallHandler", new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                    addStartCallMethodInterceptor(target);

                    return target.toBytecode();
                }
            });


            transformTemplate.transform("io.grpc.stub.ServerCalls$StreamingServerCallHandler$StreamingServerCallListener", new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                    addListenerMethod(target, grpcConfig.isServerStreamingOnMessageEnable());

                    return target.toBytecode();
                }
            });
        }
    }

    private void addStartCallMethodInterceptor(InstrumentClass target) throws InstrumentException {
        InstrumentMethod startCall = target.getDeclaredMethod("startCall", "io.grpc.ServerCall", "io.grpc.Metadata");
        if (startCall != null) {
            startCall.addInterceptor("com.navercorp.pinpoint.plugin.grpc.interceptor.server.CopyAsyncContextInterceptor");
        } else {
            if (isDebug) {
                logger.debug("can't find startCall method");
            }
        }
    }

    private void addListenerMethod(InstrumentClass target, boolean traceOnMessage) throws InstrumentException {
        List<InstrumentMethod> declaredMethods = target.getDeclaredMethods();
        for (InstrumentMethod declaredMethod : declaredMethods) {
            if (declaredMethod.getName().equals("onMessage") && !traceOnMessage) {
                if (isDebug) {
                    logger.debug("skip add onMessage interceptor");
                }
                continue;
            }

            declaredMethod.addInterceptor("com.navercorp.pinpoint.plugin.grpc.interceptor.server.ServerListenerInterceptor");
        }

    }

}
