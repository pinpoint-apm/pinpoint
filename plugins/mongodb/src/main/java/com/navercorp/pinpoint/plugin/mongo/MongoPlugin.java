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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

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
        addConnectionTransformer3_8_X();
        addSessionTransformer3_0_X(config);
        addSessionTransformer3_7_X(config);
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
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverGetDatabaseInterceptor",
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
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverGetCollectionInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

                InstrumentMethod getReadPreference = InstrumentUtils.findMethod(target, "withReadPreference", "com.mongodb.ReadPreference");
                getReadPreference.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoReadPreferenceInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

                InstrumentMethod getWriteConcern = InstrumentUtils.findMethod(target, "withWriteConcern", "com.mongodb.WriteConcern");
                getWriteConcern.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoWriteConcernInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

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
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

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
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverGetDatabaseInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

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
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverGetCollectionInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);


                InstrumentMethod getReadPreference = InstrumentUtils.findMethod(target, "withReadPreference", "com.mongodb.ReadPreference");
                getReadPreference.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoReadPreferenceInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

                InstrumentMethod getWriteConcern = InstrumentUtils.findMethod(target, "withWriteConcern", "com.mongodb.WriteConcern");
                getWriteConcern.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoWriteConcernInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

                return target.toBytecode();
            }
        });
    }
    private void addConnectionTransformer3_8_X(){
        //3.8.0+
        transformTemplate.transform("com.mongodb.client.internal.MongoClientImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentMethod connect = InstrumentUtils.findMethod(target, "getDatabase", "java.lang.String");
                connect.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverGetDatabaseInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

                InstrumentMethod close = InstrumentUtils.findMethod(target, "close");
                close.addScopedInterceptor(
                        "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor",
                        MONGO_SCOPE);

                return target.toBytecode();
            }
        });
    }

    private void addSessionTransformer3_0_X(final MongoConfig config) {
        TransformCallback transformCallback = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getMethodlistR3_0_x())))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.mongo.interceptor.MongoRSessionInterceptor", va(config.isCollectJson(), config.istraceBsonBindValue()), MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getMethodlistCUD3_0_x())))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.mongo.interceptor.MongoCUDSessionInterceptor", va(config.isCollectJson(), config.istraceBsonBindValue()) ,MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                InstrumentMethod getReadPreference = InstrumentUtils.findMethod(target, "withReadPreference", "com.mongodb.ReadPreference");
                getReadPreference.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoReadPreferenceInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

                InstrumentMethod getWriteConcern = InstrumentUtils.findMethod(target, "withWriteConcern", "com.mongodb.WriteConcern");
                getWriteConcern.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoWriteConcernInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

                return target.toBytecode();
            }
        };

        // java driver 3.0.0 ~ 3.6.4
        transformTemplate.transform("com.mongodb.MongoCollectionImpl", transformCallback);
    }
    private void addSessionTransformer3_7_X(final MongoConfig config) {
        TransformCallback transformCallback = new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getMethodlistR3_7_x())))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.mongo.interceptor.MongoRSessionInterceptor", va(config.isCollectJson(), config.istraceBsonBindValue()), MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getMethodlistCUD3_7_x())))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.mongo.interceptor.MongoCUDSessionInterceptor", va(config.isCollectJson(), config.istraceBsonBindValue()), MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
                }

                InstrumentMethod getReadPreference = InstrumentUtils.findMethod(target, "withReadPreference", "com.mongodb.ReadPreference");
                getReadPreference.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoReadPreferenceInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

                InstrumentMethod getWriteConcern = InstrumentUtils.findMethod(target, "withWriteConcern", "com.mongodb.WriteConcern");
                getWriteConcern.addScopedInterceptor(
                        "com.navercorp.pinpoint.plugin.mongo.interceptor.MongoWriteConcernInterceptor",
                        MONGO_SCOPE, ExecutionPolicy.BOUNDARY);


                return target.toBytecode();
            }
        };

        // java driver 3.7+
        transformTemplate.transform("com.mongodb.client.internal.MongoCollectionImpl", transformCallback);
    }

    private String[] getMethodlistR3_0_x(){

        List<String> methodlist = new ArrayList<String>();
        // object methods.
        methodlist.addAll(Arrays.asList("findOneAndUpdate","findOneAndReplace","findOneAndDelete","aggregate","find","distinct","count","mapReduce"));

        return methodlist.toArray(new String[0]);
    }

    private String[] getMethodlistCUD3_0_x(){

        List<String> methodlist = new ArrayList<String>();
        // object methods.
        methodlist.addAll(Arrays.asList("dropIndexes","dropIndex","createIndexes","createIndex"
                ,"updateMany","updateOne","replaceOne","deleteMany","deleteOne","insertMany","insertOne","bulkWrite"));

        return methodlist.toArray(new String[0]);
    }

    private String[] getMethodlistR3_7_x(){

        List<String> methodlist = new ArrayList<String>();
        // object methods.
        methodlist.addAll(Arrays.asList("findOneAndUpdate","findOneAndReplace","findOneAndDelete","watch","aggregate","find","distinct","count","mapReduce"));

        return methodlist.toArray(new String[0]);
    }
    private String[] getMethodlistCUD3_7_x(){

        List<String> methodlist = new ArrayList<String>();
        // object methods.
        methodlist.addAll(Arrays.asList("dropIndexes","dropIndex","createIndexes","createIndex"
                ,"updateMany","updateOne","replaceOne","deleteMany","deleteOne","insertMany","insertOne","bulkWrite"));

        return methodlist.toArray(new String[0]);
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
