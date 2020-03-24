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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.grpc.field.accessor.ServerStreamGetter;
import com.navercorp.pinpoint.plugin.grpc.interceptor.server.ServerStreamCreatedInterceptor;

import java.security.ProtectionDomain;

/**
 * @author Taejin Koo
 */
public class GrpcServerPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final GrpcServerConfig config = new GrpcServerConfig(context.getConfig());
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        if (!config.isServerEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        addInterceptor(config);
    }


    private void addInterceptor(GrpcServerConfig grpcConfig) {
        transformTemplate.transform("io.grpc.internal.ServerImpl$ServerTransportListenerImpl", ServerTransportListenerImplTransform.class);

        transformTemplate.transform("io.grpc.internal.AbstractServerStream", AddAsyncContextAccessorTransform.class);

        transformTemplate.transform("io.grpc.ServerCall$Listener", AddAsyncContextAccessorTransform.class);

        transformTemplate.transform("io.grpc.internal.ServerCallImpl", ServerCallImplTransform.class);

        transformTemplate.transform("io.grpc.stub.ServerCalls$UnaryServerCallHandler", UnaryServerCallHandler.class);

        transformTemplate.transform("io.grpc.stub.ServerCalls$UnaryServerCallHandler$UnaryServerCallListener", UnaryServerCallListenerTransform.class);

        if (grpcConfig.isServerStreamingEnable()) {
            transformTemplate.transform("io.grpc.stub.ServerCalls$StreamingServerCallHandler", StreamingServerCallHandlerTransform.class);

            transformTemplate.transform("io.grpc.stub.ServerCalls$StreamingServerCallHandler$StreamingServerCallListener", StreamingServerCallListenerTransform.class);
        }
    }

    public static class ServerTransportListenerImplTransform implements TransformCallback {
        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            InstrumentMethod streamCreatedMethod = target.getDeclaredMethod("streamCreated",
                    "io.grpc.internal.ServerStream", "java.lang.String", "io.grpc.Metadata");
            if (streamCreatedMethod != null) {
                streamCreatedMethod.addInterceptor(ServerStreamCreatedInterceptor.class);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("can't find streamCreated method");
                }
            }

            return target.toBytecode();
        }
    }

    public static class AddAsyncContextAccessorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            target.addField(AsyncContextAccessor.class);

            return target.toBytecode();
        }
    }

    public static class ServerCallImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            target.addGetter(ServerStreamGetter.class, "stream");

            return target.toBytecode();
        }
    }

    public static class UnaryServerCallHandler implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            GrpcUtils.addStartCallMethodInterceptor(target);

            return target.toBytecode();
        }
    }


    public static class UnaryServerCallListenerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            GrpcUtils.addServerListenerMethod(target, true);

            return target.toBytecode();
        }
    }

    public static class StreamingServerCallHandlerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            GrpcUtils.addStartCallMethodInterceptor(target);

            return target.toBytecode();
        }
    }


    public static class StreamingServerCallListenerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final GrpcServerConfig grpcConfig = new GrpcServerConfig(instrumentor.getProfilerConfig());

            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            GrpcUtils.addServerListenerMethod(target, grpcConfig.isServerStreamingOnMessageEnable());

            return target.toBytecode();
        }
    }


    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
