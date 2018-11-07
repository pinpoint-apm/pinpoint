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
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
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
class GrpcClientPlugin {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final TransformTemplate transformTemplate;

    GrpcClientPlugin(TransformTemplate transformTemplate) {
        this.transformTemplate = Assert.requireNonNull(transformTemplate, "transformTemplate must not be null");
    }

    void addInterceptor() {
        final String managedChannel = "io.grpc.internal.ManagedChannelImpl$RealChannel";
        transformTemplate.transform(managedChannel, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                addNewCallMethodInterceptor(target);

                return target.toBytecode();
            }

        });

        final String oobChannel = "io.grpc.internal.OobChannel";
        transformTemplate.transform(oobChannel, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                addNewCallMethodInterceptor(target);

                return target.toBytecode();
            }

        });


        final String clientStreamListener = "io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl";
        transformTemplate.transform(clientStreamListener, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                InstrumentMethod constructor = target.getConstructor("io.grpc.internal.ClientCallImpl", "io.grpc.ClientCall$Listener");
                if (constructor == null) {
                    logger.info("can't find \"io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl\" constructor");
                    return target.toBytecode();
                }

                constructor.addInterceptor("com.navercorp.pinpoint.plugin.grpc.interceptor.client.ListenerConstructorInterceptor");

                MethodFilter closedMethodsFilter = MethodFilters.chain(MethodFilters.name("closed"), MethodFilters.argAt(0, "io.grpc.Status"));
                List<InstrumentMethod> closedMethods = target.getDeclaredMethods(closedMethodsFilter);
                for (InstrumentMethod closedMethod : closedMethods) {
                    closedMethod.addInterceptor("com.navercorp.pinpoint.plugin.grpc.interceptor.client.ListenerClosedInterceptor");
                }

                target.addField(AsyncContextAccessor.class.getName());

                return target.toBytecode();
            }

        });

        transformTemplate.transform("io.grpc.internal.ClientCallImpl", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                InstrumentMethod startMethod = target.getDeclaredMethod("start", "io.grpc.ClientCall$Listener", "io.grpc.Metadata");
                if (startMethod == null) {
                    logger.debug("can't find start method");
                    return target.toBytecode();
                }

                startMethod.addInterceptor("com.navercorp.pinpoint.plugin.grpc.interceptor.client.ClientCallStartInterceptor");

                target.addField("com.navercorp.pinpoint.plugin.grpc.field.accessor.RemoteAddressAccessor");
                target.addField("com.navercorp.pinpoint.plugin.grpc.field.accessor.MethodNameAccessor");

                return target.toBytecode();
            }
        });
    }

    private void addNewCallMethodInterceptor(InstrumentClass target) throws InstrumentException {
        InstrumentMethod newCallMethod = target.getDeclaredMethod("newCall", "io.grpc.MethodDescriptor", "io.grpc.CallOptions");
        if (newCallMethod != null) {
            newCallMethod.addInterceptor("com.navercorp.pinpoint.plugin.grpc.interceptor.client.ChannelNewCallInterceptor");
        } else {
            logger.debug("can't find newCall method");
        }
    }


}
