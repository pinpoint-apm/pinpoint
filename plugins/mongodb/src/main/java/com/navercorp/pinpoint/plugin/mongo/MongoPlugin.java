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

import com.navercorp.pinpoint.bootstrap.instrument.ClassFilters;
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

        addFilterTransformer();
        addUpdatesTransformer();
        //TODO withReadConcern
        //TODO SimpleExpression
        //TODO Sort, Projection
//        addSortsTransformer();
//        addProjectionTransformer();

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

    private void addConnectionTransformer3_7_X() {
        //3.7.0+
        transformTemplate.transform("com.mongodb.client.MongoClients", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InstrumentMethod connect = InstrumentUtils.findMethod(target, "create", "com.mongodb.MongoClientSettings", "com.mongodb.MongoDriverInformation");

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

    private void addConnectionTransformer3_8_X() {
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


    private void addFilterTransformer() {

        //@TODO how about. pullByFilter under Updates
        TransformCallback transformCallback = new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                List<InstrumentClass> nestedClasses = target.getNestedClasses(
                        ClassFilters.name(MongoConstants.FILTERLIST.toArray(new String[0]))
                );

                for (final InstrumentClass nestedClass : nestedClasses) {

                    //GeometryOperatorFilter 3.1+
                    if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_GEOMETRYOPERATOR)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }

                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.OperatorNameGetter", "operatorName");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.FilterGetter", "fieldName");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.GeometryGetter", "geometry");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.MaxDistanceGetter", "maxDistance");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.MinDistanceGetter", "minDistance");

                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //NotFilter
                    if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_NOT)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }

                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.FilterGetter", "filter");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //SimpleEncodingFilter
                    if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_SIMPLEENCODING)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }

                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.FieldNameGetter", "fieldName");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.ValueGetter", "value");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //IterableOperatorFilter
                    if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_ITERABLEOPERATOR)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }

                                if (nestedTarget.hasField("fieldName")) {
                                    nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.FieldNameGetter", "fieldName");
                                }
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.OperatorNameGetter", "operatorName");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.IterableValuesGetter", "values");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //OrFilter
                    if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_OR)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }

                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.FiltersGetter", "filters");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //AndFilter
                    if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_AND)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }

                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.FiltersGetter", "filters");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //OperatorFilter
                    if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_OPERATOR)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }

                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.OperatorNameGetter", "operatorName");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.FieldNameGetter", "fieldName");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.ValueGetter", "value");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //SimpleFilter
                    if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_SIMPLE)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.FieldNameGetter", "fieldName");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.BsonValueGetter", "value");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //TextFilter 3.3+
                    if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_TEXT)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }

                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.SearchGetter", "search");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.TextSearchOptionsGetter", "textSearchOptions");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //OrNorFilter 3.3+
                    if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_ORNOR)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }

                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.FiltersGetter", "filters");
                                nestedTarget.addField("com.navercorp.pinpoint.plugin.mongo.field.getter.filters.InternalOperatorNameAccessor");

                                final InstrumentMethod nestedConstructor = nestedTarget.getConstructor("com.mongodb.client.model.Filters$OrNorFilter$Operator", "java.lang.Iterable");

                                if (nestedConstructor != null) {
                                    nestedConstructor.addInterceptor("com.navercorp.pinpoint.plugin.mongo.interceptor.MongoInternalOperatorNameInterceptor");
                                }

                                return nestedTarget.toBytecode();
                            }
                        });
                    }
                }
                return target.toBytecode();
            }
        };

        transformTemplate.transform("com.mongodb.client.model.Filters", transformCallback);
    }

    private void addUpdatesTransformer() {

        TransformCallback transformCallback = new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                List<InstrumentClass> nestedClasses = target.getNestedClasses(
                        ClassFilters.name(MongoConstants.UPDATESLIST.toArray(new String[0]))
                );

                for (final InstrumentClass nestedClass : nestedClasses) {

                    //SimpleUpdate
                    if (nestedClass.getName().equals(MongoConstants.MONGO_UPDATES_SIMPLE)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.FieldNameGetter", "fieldName");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.ValueGetter", "value");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.OperatorGetter", "operator");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //WithEachUpdate
                    if (nestedClass.getName().equals(MongoConstants.MONGO_UPDATES_WITHEACH)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.FieldNameGetter", "fieldName");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.updates.ListValuesGetter", "values");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.OperatorGetter", "operator");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //PushUpdate
                    if (nestedClass.getName().equals(MongoConstants.MONGO_UPDATES_PUSH)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.updates.PushOptionsGetter", "options");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //PullAllUpdate
                    if (nestedClass.getName().equals(MongoConstants.MONGO_UPDATES_PULLALL)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.FieldNameGetter", "fieldName");
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.updates.ListValuesGetter", "values");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }

                    //CompositeUpdate
                    if (nestedClass.getName().equals(MongoConstants.MONGO_UPDATES_COMPOSITE)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.ExtendedBsonListGetter", "updates");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }
                }
                return target.toBytecode();
            }
        };

        transformTemplate.transform("com.mongodb.client.model.Updates", transformCallback);
    }

    private void addSortsTransformer() {

        TransformCallback transformCallback = new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (!target.isInterceptable()) {
                    return null;
                }

                List<InstrumentClass> nestedClasses = target.getNestedClasses(
                        ClassFilters.name(MongoConstants.MONGO_SORT_COMPOSITE)
                );

                for (final InstrumentClass nestedClass : nestedClasses) {

                    //CompositeUpdate
                    if (nestedClass.getName().equals(MongoConstants.MONGO_SORT_COMPOSITE)) {
                        instrumentor.transform(loader, nestedClass.getName(), new TransformCallback() {
                            @Override
                            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                                final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                                if (!nestedTarget.isInterceptable()) {
                                    return null;
                                }
                                nestedTarget.addGetter("com.navercorp.pinpoint.plugin.mongo.field.getter.ExtendedBsonListGetter", "updates");
                                return nestedTarget.toBytecode();
                            }
                        });
                    }
                }
                return target.toBytecode();
            }
        };

        transformTemplate.transform("com.mongodb.client.model.Sorts", transformCallback);
    }

    private String[] getMethodlistR3_0_x() {

        final String[] methodList = new String[]{"findOneAndUpdate", "findOneAndReplace", "findOneAndDelete", "find", "count", "distinct", "listIndexes"
//                , "watch" ,"aggregate","mapReduce"
        };
        return methodList;
    }

    private String[] getMethodlistCUD3_0_x() {
        final String[] methodList = new String[] {"dropIndexes", "dropIndex", "createIndexes", "createIndex"
                , "updateMany", "updateOne", "replaceOne", "deleteMany", "deleteOne", "insertMany", "insertOne", "bulkWrite"};
        return methodList;
    }

    private String[] getMethodlistR3_7_x() {

        final String[] methodList = new String[]{"findOneAndUpdate", "findOneAndReplace", "findOneAndDelete", "find", "count", "distinct", "listIndexes", "countDocuments"
//                , "watch", "aggregate", "mapReduce"
        };

        return methodList;
    }

    private String[] getMethodlistCUD3_7_x() {

        final String[] methodlist = new String[]{"dropIndexes", "dropIndex", "createIndexes", "createIndex"
                , "updateMany", "updateOne", "replaceOne", "deleteMany", "deleteOne", "insertMany", "insertOne", "bulkWrite"};

        return methodlist;
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
