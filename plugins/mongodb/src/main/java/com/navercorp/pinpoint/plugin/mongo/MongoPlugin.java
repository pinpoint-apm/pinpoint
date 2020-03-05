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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
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
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.plugin.mongo.field.getter.ExtendedBsonListGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.FieldNameGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.OperatorGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.ValueGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.BsonValueGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.FilterGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.FiltersGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.GeometryGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.InternalOperatorNameAccessor;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.IterableValuesGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.MaxDistanceGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.MinDistanceGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.OperatorNameGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.SearchGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.filters.TextSearchOptionsGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.updates.ListValuesGetter;
import com.navercorp.pinpoint.plugin.mongo.field.getter.updates.PushOptionsGetter;
import com.navercorp.pinpoint.plugin.mongo.interceptor.PublisherInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoCUDSessionInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverConnectInterceptor3_0;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverConnectInterceptor3_7;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverGetCollectionInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDriverGetDatabaseInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoInternalOperatorNameInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoRSessionInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoReadPreferenceInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoWriteConcernInterceptor;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
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
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

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
        addConnectionTransformerReactive();
        addSessionTransformer3_0_X();
        addSessionTransformer3_7_X();
        // Reactive stream
        addSessionTransformerReactive();
    }

    private void addConnectionTransformer3_0_X() {

        // 3.0.0 ~ 3.6.4
        transformTemplate.transform("com.mongodb.Mongo", ClientConnectionTransform3_0_X.class);
        transformTemplate.transform("com.mongodb.MongoClient", DatabaseConnectionTransform3_0_X.class);
        transformTemplate.transform("com.mongodb.MongoDatabaseImpl", CollectionConnectionTransform3_0_X.class);

    }

    public static class ClientConnectionTransform3_0_X implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            InstrumentMethod connect = InstrumentUtils.findConstructor(target, "com.mongodb.connection.Cluster", "com.mongodb.MongoClientOptions", "java.util.List");

            connect.addScopedInterceptor(MongoDriverConnectInterceptor3_0.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);


            InstrumentMethod close = InstrumentUtils.findMethod(target, "close");
            close.addScopedInterceptor(ConnectionCloseInterceptor.class, MONGO_SCOPE);

            return target.toBytecode();
        }
    }

    public static class DatabaseConnectionTransform3_0_X implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            InstrumentMethod connectDeliver = InstrumentUtils.findMethod(target, "getDatabase", "java.lang.String");
            connectDeliver.addScopedInterceptor(MongoDriverGetDatabaseInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            return target.toBytecode();
        }
    }

    public static class CollectionConnectionTransform3_0_X implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            InstrumentMethod connect = InstrumentUtils.findMethod(target, "getCollection", "java.lang.String", "java.lang.Class");
            connect.addScopedInterceptor(MongoDriverGetCollectionInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            InstrumentMethod getReadPreference = InstrumentUtils.findMethod(target, "withReadPreference", "com.mongodb.ReadPreference");
            getReadPreference.addScopedInterceptor(MongoReadPreferenceInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            InstrumentMethod getWriteConcern = InstrumentUtils.findMethod(target, "withWriteConcern", "com.mongodb.WriteConcern");
            getWriteConcern.addScopedInterceptor(MongoWriteConcernInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            return target.toBytecode();
        }
    }

    private void addConnectionTransformer3_7_X() {
        //3.7.0+
        transformTemplate.transform("com.mongodb.client.MongoClients", ClientConnectionTransform3_7_X.class);
        transformTemplate.transform("com.mongodb.client.MongoClientImpl", DatabaseConnectionTransform3_7_X.class);
        transformTemplate.transform("com.mongodb.client.internal.MongoDatabaseImpl", CollectionConnectionTransform3_7_X.class);
    }

    public static class ClientConnectionTransform3_7_X implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            InstrumentMethod connect = InstrumentUtils.findMethod(target, "create", "com.mongodb.MongoClientSettings", "com.mongodb.MongoDriverInformation");

            connect.addScopedInterceptor(MongoDriverConnectInterceptor3_7.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            return target.toBytecode();
        }
    }

    public static class DatabaseConnectionTransform3_7_X implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            InstrumentMethod connect = InstrumentUtils.findMethod(target, "getDatabase", "java.lang.String");
            connect.addScopedInterceptor(MongoDriverGetDatabaseInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            InstrumentMethod close = InstrumentUtils.findMethod(target, "close");
            close.addScopedInterceptor(ConnectionCloseInterceptor.class, MONGO_SCOPE);

            return target.toBytecode();
        }
    }

    public static class CollectionConnectionTransform3_7_X implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            InstrumentMethod connect = InstrumentUtils.findMethod(target, "getCollection", "java.lang.String", "java.lang.Class");
            connect.addScopedInterceptor(MongoDriverGetCollectionInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);


            InstrumentMethod getReadPreference = InstrumentUtils.findMethod(target, "withReadPreference", "com.mongodb.ReadPreference");
            getReadPreference.addScopedInterceptor(MongoReadPreferenceInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            InstrumentMethod getWriteConcern = InstrumentUtils.findMethod(target, "withWriteConcern", "com.mongodb.WriteConcern");
            getWriteConcern.addScopedInterceptor(MongoWriteConcernInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            return target.toBytecode();
        }
    }

    private void addConnectionTransformer3_8_X() {
        //3.8.0+
        transformTemplate.transform("com.mongodb.client.internal.MongoClientImpl", DatabaseConnectionTransform3_8_X.class);
    }

    public static class DatabaseConnectionTransform3_8_X implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            InstrumentMethod connect = InstrumentUtils.findMethod(target, "getDatabase", "java.lang.String");
            connect.addScopedInterceptor(MongoDriverGetDatabaseInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            InstrumentMethod close = InstrumentUtils.findMethod(target, "close");
            close.addScopedInterceptor(ConnectionCloseInterceptor.class, MONGO_SCOPE);

            return target.toBytecode();
        }
    }

    private void addConnectionTransformerReactive() {
        //reactivestreams
        transformTemplate.transform("com.mongodb.reactivestreams.client.MongoClients", ClientConnectionTransform3_7_X.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.MongoClientImpl", DatabaseConnectionTransform3_8_X.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.MongoDatabaseImpl", CollectionConnectionTransform3_7_X.class);
    }


    private void addSessionTransformer3_0_X() {
        transformTemplate.transform("com.mongodb.MongoCollectionImpl", SessionTransform3_0_X.class);
    }

    public static class SessionTransform3_0_X implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(DatabaseInfoAccessor.class);

            MongoConfig config = new MongoConfig(instrumentor.getProfilerConfig());

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getMethodlistR3_0_x())))) {
                method.addScopedInterceptor(MongoRSessionInterceptor.class, va(config.isCollectJson(), config.istraceBsonBindValue()), MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getMethodlistCUD3_0_x())))) {
                method.addScopedInterceptor(MongoCUDSessionInterceptor.class, va(config.isCollectJson(), config.istraceBsonBindValue()), MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            InstrumentMethod getReadPreference = InstrumentUtils.findMethod(target, "withReadPreference", "com.mongodb.ReadPreference");
            getReadPreference.addScopedInterceptor(MongoReadPreferenceInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            InstrumentMethod getWriteConcern = InstrumentUtils.findMethod(target, "withWriteConcern", "com.mongodb.WriteConcern");
            getWriteConcern.addScopedInterceptor(MongoWriteConcernInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            return target.toBytecode();
        }
    }

    private void addSessionTransformer3_7_X() {
        transformTemplate.transform("com.mongodb.client.internal.MongoCollectionImpl", SessionTransform3_7_X.class);
    }

    public static class SessionTransform3_7_X implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            MongoConfig config = new MongoConfig(instrumentor.getProfilerConfig());

            target.addField(DatabaseInfoAccessor.class);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getMethodlistR3_7_x())))) {
                method.addScopedInterceptor(MongoRSessionInterceptor.class, va(config.isCollectJson(), config.istraceBsonBindValue()), MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getMethodlistCUD3_7_x())))) {
                method.addScopedInterceptor(MongoCUDSessionInterceptor.class, va(config.isCollectJson(), config.istraceBsonBindValue()), MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            InstrumentMethod getReadPreference = InstrumentUtils.findMethod(target, "withReadPreference", "com.mongodb.ReadPreference");
            getReadPreference.addScopedInterceptor(MongoReadPreferenceInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);

            InstrumentMethod getWriteConcern = InstrumentUtils.findMethod(target, "withWriteConcern", "com.mongodb.WriteConcern");
            getWriteConcern.addScopedInterceptor(MongoWriteConcernInterceptor.class, MONGO_SCOPE, ExecutionPolicy.BOUNDARY);


            return target.toBytecode();
        }
    }

    private void addSessionTransformerReactive() {
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.MongoCollectionImpl", SessionTransform3_7_X.class);

        // Reactive
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.FindPublisherImpl", ObservableToPublisherTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.AggregatePublisherImpl", ObservableToPublisherTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.ChangeStreamPublisherImpl", ObservableToPublisherTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.DistinctPublisherImpl", ObservableToPublisherTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.GridFSDownloadPublisherImpl", ObservableToPublisherTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.GridFSFindPublisherImpl", ObservableToPublisherTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.GridFSUploadPublisherImpl", ObservableToPublisherTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.ListCollectionsPublisherImpl", ObservableToPublisherTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.ListDatabasesPublisherImpl", ObservableToPublisherTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.ListIndexesPublisherImpl", ObservableToPublisherTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.MapReducePublisherImpl", ObservableToPublisherTransform.class);
        // 1.12
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.SingleResultObservableToPublisher", ObservableToPublisherTransform.class);
        // 1.13
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.ObservableToPublisher", ObservableToPublisherTransform.class);
    }

    public static class ObservableToPublisherTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "org.reactivestreams.Subscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(PublisherInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    private void addFilterTransformer() {
        transformTemplate.transform("com.mongodb.client.model.Filters", FilterTransform.class);
    }

    //@TODO how about. pullByFilter under Updates
    public static class FilterTransform implements TransformCallback {
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
                    instrumentor.transform(loader, nestedClass.getName(), GeometryOperatorTransform.class);
                }

                //NotFilter
                if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_NOT)) {
                    instrumentor.transform(loader, nestedClass.getName(), NotFilterTransform.class);
                }

                //SimpleEncodingFilter
                if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_SIMPLEENCODING)) {
                    instrumentor.transform(loader, nestedClass.getName(), SimpleEncodingFilterTransform.class);
                }

                //IterableOperatorFilter
                if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_ITERABLEOPERATOR)) {
                    instrumentor.transform(loader, nestedClass.getName(), IterableOperatorFilterTransform.class);
                }

                //OrFilter
                if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_OR)) {
                    instrumentor.transform(loader, nestedClass.getName(), OrFilterTransform.class);
                }

                //AndFilter
                if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_AND)) {
                    instrumentor.transform(loader, nestedClass.getName(), AndFilterTransform.class);
                }

                //OperatorFilter
                if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_OPERATOR)) {
                    instrumentor.transform(loader, nestedClass.getName(), OperatorFilterTransform.class);
                }

                //SimpleFilter
                if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_SIMPLE)) {
                    instrumentor.transform(loader, nestedClass.getName(), SimpleFilterTransform.class);
                }

                //TextFilter 3.3+
                if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_TEXT)) {
                    instrumentor.transform(loader, nestedClass.getName(), TextFilterTransform.class);
                }

                //OrNorFilter 3.3+
                if (nestedClass.getName().equals(MongoConstants.MONGO_FILTER_ORNOR)) {
                    instrumentor.transform(loader, nestedClass.getName(), OrNorFilterTransform.class);
                }
            }
            return target.toBytecode();
        }
    }

    public static class GeometryOperatorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }

            nestedTarget.addGetter(OperatorNameGetter.class, "operatorName");
            nestedTarget.addGetter(FilterGetter.class, "fieldName");
            nestedTarget.addGetter(GeometryGetter.class, "geometry");
            nestedTarget.addGetter(MaxDistanceGetter.class, "maxDistance");
            nestedTarget.addGetter(MinDistanceGetter.class, "minDistance");

            return nestedTarget.toBytecode();
        }
    }

    public static class NotFilterTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }

            nestedTarget.addGetter(FilterGetter.class, "filter");
            return nestedTarget.toBytecode();
        }
    }

    public static class SimpleEncodingFilterTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }

            nestedTarget.addGetter(FieldNameGetter.class, "fieldName");
            nestedTarget.addGetter(ValueGetter.class, "value");
            return nestedTarget.toBytecode();
        }
    }

    public static class IterableOperatorFilterTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }

            if (nestedTarget.hasField("fieldName")) {
                nestedTarget.addGetter(FieldNameGetter.class, "fieldName");
            }
            nestedTarget.addGetter(OperatorNameGetter.class, "operatorName");
            nestedTarget.addGetter(IterableValuesGetter.class, "values");
            return nestedTarget.toBytecode();
        }
    }

    public static class OrFilterTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }

            nestedTarget.addGetter(FiltersGetter.class, "filters");
            return nestedTarget.toBytecode();
        }
    }

    public static class AndFilterTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }

            nestedTarget.addGetter(FiltersGetter.class, "filters");
            return nestedTarget.toBytecode();
        }
    }

    public static class OperatorFilterTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }

            nestedTarget.addGetter(OperatorNameGetter.class, "operatorName");
            nestedTarget.addGetter(FieldNameGetter.class, "fieldName");
            nestedTarget.addGetter(ValueGetter.class, "value");
            return nestedTarget.toBytecode();
        }
    }

    public static class SimpleFilterTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            nestedTarget.addGetter(FieldNameGetter.class, "fieldName");
            nestedTarget.addGetter(BsonValueGetter.class, "value");
            return nestedTarget.toBytecode();
        }
    }

    public static class TextFilterTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }

            nestedTarget.addGetter(SearchGetter.class, "search");
            nestedTarget.addGetter(TextSearchOptionsGetter.class, "textSearchOptions");
            return nestedTarget.toBytecode();
        }
    }

    public static class OrNorFilterTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }

            nestedTarget.addGetter(FiltersGetter.class, "filters");
            nestedTarget.addField(InternalOperatorNameAccessor.class);

            final InstrumentMethod nestedConstructor = nestedTarget.getConstructor("com.mongodb.client.model.Filters$OrNorFilter$Operator", "java.lang.Iterable");

            if (nestedConstructor != null) {
                nestedConstructor.addInterceptor(MongoInternalOperatorNameInterceptor.class);
            }

            return nestedTarget.toBytecode();
        }
    }

    private void addUpdatesTransformer() {
        transformTemplate.transform("com.mongodb.client.model.Updates", UpdatesTransform.class);
    }

    public static class UpdatesTransform implements TransformCallback {
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
                    instrumentor.transform(loader, nestedClass.getName(), SimpleUpdateTransform.class);
                }

                //WithEachUpdate
                if (nestedClass.getName().equals(MongoConstants.MONGO_UPDATES_WITHEACH)) {
                    instrumentor.transform(loader, nestedClass.getName(), WithEachUpdateTransform.class);
                }

                //PushUpdate
                if (nestedClass.getName().equals(MongoConstants.MONGO_UPDATES_PUSH)) {
                    instrumentor.transform(loader, nestedClass.getName(), PushUpdateTransform.class);
                }

                //PullAllUpdate
                if (nestedClass.getName().equals(MongoConstants.MONGO_UPDATES_PULLALL)) {
                    instrumentor.transform(loader, nestedClass.getName(), PullAllUpdateTransform.class);
                }

                //CompositeUpdate
                if (nestedClass.getName().equals(MongoConstants.MONGO_UPDATES_COMPOSITE)) {
                    instrumentor.transform(loader, nestedClass.getName(), CompositeUpdateTransform.class);
                }
            }
            return target.toBytecode();
        }
    }

    public static class SimpleUpdateTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }
            nestedTarget.addGetter(FieldNameGetter.class, "fieldName");
            nestedTarget.addGetter(ValueGetter.class, "value");
            nestedTarget.addGetter(OperatorGetter.class, "operator");
            return nestedTarget.toBytecode();
        }
    }

    public static class WithEachUpdateTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }
            nestedTarget.addGetter(FieldNameGetter.class, "fieldName");
            nestedTarget.addGetter(ListValuesGetter.class, "values");
            nestedTarget.addGetter(OperatorGetter.class, "operator");
            return nestedTarget.toBytecode();
        }
    }

    public static class PushUpdateTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }
            nestedTarget.addGetter(PushOptionsGetter.class, "options");
            return nestedTarget.toBytecode();
        }
    }

    public static class PullAllUpdateTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }
            nestedTarget.addGetter(FieldNameGetter.class, "fieldName");
            nestedTarget.addGetter(ListValuesGetter.class, "values");
            return nestedTarget.toBytecode();
        }
    }

    public static class CompositeUpdateTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass nestedTarget = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!nestedTarget.isInterceptable()) {
                return null;
            }
            nestedTarget.addGetter(ExtendedBsonListGetter.class, "updates");
            return nestedTarget.toBytecode();
        }
    }

    private void addSortsTransformer() {
        transformTemplate.transform("com.mongodb.client.model.Sorts", SortsTransform.class);
    }

    public static class SortsTransform implements TransformCallback {
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
                            nestedTarget.addGetter(ExtendedBsonListGetter.class, "updates");
                            return nestedTarget.toBytecode();
                        }
                    });
                }
            }
            return target.toBytecode();
        }
    }

    private static String[] getMethodlistR3_0_x() {

        final String[] methodList = new String[]{"findOneAndUpdate", "findOneAndReplace", "findOneAndDelete", "find", "count", "distinct", "listIndexes"
//                , "watch" ,"aggregate","mapReduce"
        };
        return methodList;
    }

    private static String[] getMethodlistCUD3_0_x() {
        final String[] methodList = new String[]{"dropIndexes", "dropIndex", "createIndexes", "createIndex"
                , "updateMany", "updateOne", "replaceOne", "deleteMany", "deleteOne", "insertMany", "insertOne", "bulkWrite"};
        return methodList;
    }

    private static String[] getMethodlistR3_7_x() {

        final String[] methodList = new String[]{"findOneAndUpdate", "findOneAndReplace", "findOneAndDelete", "find", "count", "distinct", "listIndexes", "countDocuments"
//                , "watch", "aggregate", "mapReduce"
        };

        return methodList;
    }

    private static String[] getMethodlistCUD3_7_x() {

        final String[] methodlist = new String[]{"dropIndexes", "dropIndex", "createIndexes", "createIndex"
                , "updateMany", "updateOne", "replaceOne", "deleteMany", "deleteOne", "insertMany", "insertOne", "bulkWrite"};

        return methodlist;
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
