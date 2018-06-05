/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.mongo;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.connection.Cluster;
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
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

/**
 * @author Roy Kim
 */
public class MongoPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private static final String MONGO_SCOPE = MongoConstants.MONGO_SCOPE;

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        MongoConfig config = new MongoConfig(context.getConfig());

        if (!config.isEnable()) {
            logger.info("MongoDB plugin is not executed because plugin enable value is false.");
            return;
        }

        addConnectionTransformer3_0_X();
        addConnectionTransformer3_7_X();
        addSessionTransformer();
    }

    private void addConnectionTransformer3_0_X() {

        // 3.0.0 ~ 3.6.4
        transformTemplate.transform("com.mongodb.Mongo", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentMethod connect = InstrumentUtils.findConstructor(target, "com.mongodb.connection.Cluster", "com.mongodb.MongoClientOptions", "java.util.List");

                connect.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverConnectInterceptor3_0",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);


                InstrumentMethod close = InstrumentUtils.findMethod(target, "close");
                close.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor",
                        MONGO_SCOPE);

                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.mongodb.MongoClient", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentMethod connectDeliver = InstrumentUtils.findMethod(target, "getDatabase", "java.lang.String");
                connectDeliver.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverConnectDeliverInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.mongodb.MongoDatabaseImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentMethod connect = InstrumentUtils.findMethod(target, "getCollection", "java.lang.String", "java.lang.Class");

                connect.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverConnectDeliverInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.ALWAYS);

                return target.toBytecode();
            }
        });
    }
    private void addConnectionTransformer3_7_X(){
        //3.7.0+
        transformTemplate.transform("com.mongodb.client.MongoClients", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentMethod connect = InstrumentUtils.findMethod(target, "create", "com.mongodb.MongoClientSettings","com.mongodb.MongoDriverInformation");

                connect.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverConnectInterceptor3_7",
                        MONGO_SCOPE, ExecutionPolicy.ALWAYS);

                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.mongodb.client.MongoClientImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentMethod connect = InstrumentUtils.findMethod(target, "getDatabase", "java.lang.String");
                connect.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverConnectDeliverInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.ALWAYS);

                InstrumentMethod close = InstrumentUtils.findMethod(target, "close");
                close.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor",
                        MONGO_SCOPE);

                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.mongodb.client.internal.MongoDatabaseImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentMethod connect = InstrumentUtils.findMethod(target, "getCollection", "java.lang.String", "java.lang.Class");

                connect.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverConnectDeliverInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.ALWAYS);

                return target.toBytecode();
            }
        });
    }

    private void addSessionTransformer() {
        TransformCallback transformCallback = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                //for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PRIVATE), MethodFilters.nameExclude("toUpdateResult", "translateBulkWriteResult", "executeSingleWriteRequest")))) {
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.nameExclude("_$PINPOINT$_getDatabaseInfo","_$PINPOINT$_setDatabaseInfo")))) {
                        method.addScopedInterceptor("com.navercorp.pinpoint.plugin.mongo.interceptor.MongoSessionInterceptor", MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                return target.toBytecode();
            }
        };

        // java driver 3.7+
        transformTemplate.transform("com.mongodb.client.internal.MongoCollectionImpl", transformCallback);
        // java driver 3.0.0 ~ 3.6.4
        transformTemplate.transform("com.mongodb.MongoCollectionImpl", transformCallback);
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
