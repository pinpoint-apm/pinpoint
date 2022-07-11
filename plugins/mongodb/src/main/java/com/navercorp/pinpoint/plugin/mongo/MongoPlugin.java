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
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorContextAccessor;
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
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoClientConstructorInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoClientGetDatabaseInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoClientImplConstructorInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoClientImplGetDatabaseInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoClientsInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoCollectionImplConstructorInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoCollectionImplReadOperationInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoCollectionImplWithInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoCollectionImplWriteOperationInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDatabaseImplGetCollectionInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoDatabaseImplWithInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.MongoInternalOperatorNameInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.ReactiveMongoClientImplConstructorInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.ReactiveMongoClientImplGetDatabaseInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.ReactiveMongoCollectionImplConstructorInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.ReactiveMongoCollectionImplReadOperationInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.ReactiveMongoCollectionImplWithInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.ReactiveMongoCollectionImplWriteOperationInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.ReactiveMongoDatabaseImplGetCollectionInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.ReactiveMongoDatabaseImplWithInterceptor;
import com.navercorp.pinpoint.plugin.mongo.interceptor.ReactiveMongoOperationPublisherConstructorInterceptor;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Roy Kim
 */
public class MongoPlugin implements ProfilerPlugin, TransformTemplateAware {
    private static final String MONGO_SCOPE = MongoConstants.MONGO_SCOPE;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

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

        addMongoDatabase();
        addMongoReactiveDatabase();
    }

    private void addMongoDatabase() {
        // 3.0.x ~ 3.6.4
        transformTemplate.transform("com.mongodb.MongoClient", MongoClientTransform.class);
        // Only 3.7.x
        transformTemplate.transform("com.mongodb.client.MongoClientImpl", MongoClientImplTransform.class);
        // 3.8.x+
        transformTemplate.transform("com.mongodb.client.internal.MongoClientImpl", MongoClientImplTransform.class);

        // 3.7.x+
        transformTemplate.transform("com.mongodb.client.MongoClients", MongoClientsTransform.class);

        // 3.0.x ~ 3.6.4
        transformTemplate.transform("com.mongodb.MongoDatabaseImpl", MongoDatabaseImplTransform.class);
        // 3.7.x+
        transformTemplate.transform("com.mongodb.client.internal.MongoDatabaseImpl", MongoDatabaseImplTransform.class);

        // 3.0.x ~ 3.6.x
        transformTemplate.transform("com.mongodb.MongoCollectionImpl", MongoCollectionImplTransform.class);
        // 3.7.x+
        transformTemplate.transform("com.mongodb.client.internal.MongoCollectionImpl", MongoCollectionImplTransform.class);
    }

    private void addMongoReactiveDatabase() {
        // 4.2+
        transformTemplate.transform("com.mongodb.reactivestreams.client.MongoClients", MongoClientsTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.MongoClientImpl", ReactiveMongoClientImplTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.MongoDatabaseImpl", ReactiveMongoDatabaseImplTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.MongoOperationPublisher", ReactiveMongoOperationPublisherTransform.class);
        transformTemplate.transform("com.mongodb.reactivestreams.client.internal.MongoCollectionImpl", ReactiveMongoCollectionImplTransform.class);

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

    public static class MongoClientTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(HostListAccessor.class);

            // 3.0
            // MongoClient(final ServerAddress addr, final MongoClientOptions options)
            final InstrumentMethod constructorMethod1 = target.getConstructor("com.mongodb.ServerAddress", "com.mongodb.MongoClientOptions");
            if (constructorMethod1 != null) {
                constructorMethod1.addInterceptor(MongoClientConstructorInterceptor.class);
            }
            // MongoClient(final ServerAddress addr, final List<MongoCredential> credentialsList, final MongoClientOptions options)
            final InstrumentMethod constructorMethod2 = target.getConstructor("com.mongodb.ServerAddress", "java.util.List", "com.mongodb.MongoClientOptions");
            if (constructorMethod2 != null) {
                constructorMethod2.addInterceptor(MongoClientConstructorInterceptor.class);
            }
            // MongoClient(final List<ServerAddress> seeds, final MongoClientOptions options)
            final InstrumentMethod constructorMethod3 = target.getConstructor("java.util.List", "com.mongodb.MongoClientOptions");
            if (constructorMethod3 != null) {
                constructorMethod3.addInterceptor(MongoClientConstructorInterceptor.class);
            }
            // MongoClient(final List<ServerAddress> seeds, final List<MongoCredential> credentialsList, final MongoClientOptions options)
            final InstrumentMethod constructorMethod4 = target.getConstructor("java.util.List", "java.util.List", "com.mongodb.MongoClientOptions");
            if (constructorMethod4 != null) {
                constructorMethod4.addInterceptor(MongoClientConstructorInterceptor.class);
            }
            // Ignored - MongoClient(final MongoClientURI uri)

            // 3.4
            // Ignored - MongoClient(final MongoClientURI uri, final MongoDriverInformation mongoDriverInformation)
            // MongoClient(final ServerAddress addr, final List<MongoCredential> credentialsList, final MongoClientOptions options, final MongoDriverInformation mongoDriverInformation)
            final InstrumentMethod constructorMethod7 = target.getConstructor("com.mongodb.ServerAddress", "java.util.List", "com.mongodb.MongoClientOptions", "com.mongodb.MongoDriverInformation");
            if (constructorMethod7 != null) {
                constructorMethod7.addInterceptor(MongoClientConstructorInterceptor.class);
            }
            // MongoClient(final List<ServerAddress> seeds, final List<MongoCredential> credentialsList, final MongoClientOptions options, final MongoDriverInformation mongoDriverInformation)
            final InstrumentMethod constructorMethod8 = target.getConstructor("java.util.List", "java.util.List", "com.mongodb.MongoClientOptions", "com.mongodb.MongoDriverInformation");
            if (constructorMethod8 != null) {
                constructorMethod8.addInterceptor(MongoClientConstructorInterceptor.class);
            }
            // 3.6
            // MongoClient(final ServerAddress addr, final MongoCredential credential, final MongoClientOptions options)
            final InstrumentMethod constructorMethod9 = target.getConstructor("com.mongodb.ServerAddress", "com.mongodb.MongoCredential", "com.mongodb.MongoClientOptions");
            if (constructorMethod9 != null) {
                constructorMethod9.addInterceptor(MongoClientConstructorInterceptor.class);
            }
            // MongoClient(final List<ServerAddress> seeds, final MongoCredential credential, final MongoClientOptions options)
            final InstrumentMethod constructorMethod10 = target.getConstructor("java.util.List", "com.mongodb.MongoCredential", "com.mongodb.MongoClientOptions");
            if (constructorMethod10 != null) {
                constructorMethod10.addInterceptor(MongoClientConstructorInterceptor.class);
            }
            // MongoClient(final ServerAddress addr, final MongoCredential credential, final MongoClientOptions options, final MongoDriverInformation mongoDriverInformation)
            final InstrumentMethod constructorMethod11 = target.getConstructor("com.mongodb.ServerAddress", "com.mongodb.MongoCredential", "com.mongodb.MongoClientOptions", "com.mongodb.MongoDriverInformation");
            if (constructorMethod11 != null) {
                constructorMethod11.addInterceptor(MongoClientConstructorInterceptor.class);
            }
            // MongoClient(final List<ServerAddress> seeds, final MongoCredential credential, final MongoClientOptions options, final MongoDriverInformation mongoDriverInformation)
            final InstrumentMethod constructorMethod12 = target.getConstructor("java.util.List", "com.mongodb.MongoCredential", "com.mongodb.MongoClientOptions", "com.mongodb.MongoDriverInformation");
            if (constructorMethod12 != null) {
                constructorMethod12.addInterceptor(MongoClientConstructorInterceptor.class);
            }

            final InstrumentMethod getDatabaseMethod = target.getDeclaredMethod("getDatabase", "java.lang.String");
            if (getDatabaseMethod != null) {
                getDatabaseMethod.addInterceptor(MongoClientGetDatabaseInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MongoClientImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(HostListAccessor.class);

            // 3.7
            InstrumentMethod constructorMethod = target.getConstructor("com.mongodb.connection.Cluster", "com.mongodb.MongoClientSettings", "com.mongodb.client.internal.OperationExecutor");
            if (constructorMethod == null) {
                // 4.0
                constructorMethod = target.getConstructor("com.mongodb.internal.connection.Cluster", "com.mongodb.MongoClientSettings", "com.mongodb.client.internal.OperationExecutor");
            }
            if (constructorMethod == null) {
                // 4.2 or later
                constructorMethod = target.getConstructor("com.mongodb.internal.connection.Cluster", "com.mongodb.MongoDriverInformation", "com.mongodb.MongoClientSettings", "com.mongodb.client.internal.OperationExecutor");
            }
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(MongoClientImplConstructorInterceptor.class);
            }

            final InstrumentMethod getDatabaseMethod = target.getDeclaredMethod("getDatabase", "java.lang.String");
            if (getDatabaseMethod != null) {
                getDatabaseMethod.addInterceptor(MongoClientImplGetDatabaseInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MongoClientsTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(HostListAccessor.class);

            final InstrumentMethod createMethod = target.getDeclaredMethod("create", "com.mongodb.MongoClientSettings", "com.mongodb.MongoDriverInformation");
            if (createMethod != null) {
                createMethod.addInterceptor(MongoClientsInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MongoDatabaseImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(HostListAccessor.class);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("withCodecRegistry", "withReadPreference", "withWriteConcern", "withReadConcern"))) {
                method.addInterceptor(MongoDatabaseImplWithInterceptor.class);
            }

            final InstrumentMethod getCollectionMethod = target.getDeclaredMethod("getCollection", "java.lang.String", "java.lang.Class");
            if (getCollectionMethod != null) {
                getCollectionMethod.addInterceptor(MongoDatabaseImplGetCollectionInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class MongoCollectionImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(HostListAccessor.class);
            target.addField(DatabaseInfoAccessor.class);

            // 3.0
            InstrumentMethod constructorMethod = target.getConstructor("com.mongodb.MongoNamespace", "java.lang.Class", "org.bson.codecs.configuration.CodecRegistry", "com.mongodb.ReadPreference", "com.mongodb.WriteConcern", "com.mongodb.operation.OperationExecutor");
            if (constructorMethod == null) {
                // 3.2
                constructorMethod = target.getConstructor("com.mongodb.MongoNamespace", "java.lang.Class", "org.bson.codecs.configuration.CodecRegistry", "com.mongodb.ReadPreference", "com.mongodb.WriteConcern", "com.mongodb.ReadConcern", "com.mongodb.operation.OperationExecutor");
            }
            if (constructorMethod == null) {
                // 3.6
                constructorMethod = target.getConstructor("com.mongodb.MongoNamespace", "java.lang.Class", "org.bson.codecs.configuration.CodecRegistry", "com.mongodb.ReadPreference", "com.mongodb.WriteConcern", "boolean", "com.mongodb.ReadConcern", "com.mongodb.OperationExecutor");
            }
            if (constructorMethod == null) {
                // 3.7+
                constructorMethod = target.getConstructor("com.mongodb.MongoNamespace", "java.lang.Class", "org.bson.codecs.configuration.CodecRegistry", "com.mongodb.ReadPreference", "com.mongodb.WriteConcern", "boolean", "com.mongodb.ReadConcern", "com.mongodb.client.internal.OperationExecutor");
            }
            if (constructorMethod == null) {
                // 3.11
                constructorMethod = target.getConstructor("com.mongodb.MongoNamespace", "java.lang.Class", "org.bson.codecs.configuration.CodecRegistry", "com.mongodb.ReadPreference", "com.mongodb.WriteConcern", "boolean", "boolean", "com.mongodb.ReadConcern", "com.mongodb.client.internal.OperationExecutor");
            }
            if (constructorMethod == null) {
                // 3.12
                constructorMethod = target.getConstructor("com.mongodb.MongoNamespace", "java.lang.Class", "org.bson.codecs.configuration.CodecRegistry", "com.mongodb.ReadPreference", "com.mongodb.WriteConcern", "boolean", "boolean", "com.mongodb.ReadConcern", "org.bson.UuidRepresentation", "com.mongodb.client.internal.OperationExecutor");
            }
            if (constructorMethod == null) {
                // 4.7
                constructorMethod = target.getConstructor("com.mongodb.MongoNamespace", "java.lang.Class", "org.bson.codecs.configuration.CodecRegistry", "com.mongodb.ReadPreference", "com.mongodb.WriteConcern", "boolean", "boolean", "com.mongodb.ReadConcern", "org.bson.UuidRepresentation", "com.mongodb.AutoEncryptionSettings", "com.mongodb.client.internal.OperationExecutor");
            }
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(MongoCollectionImplConstructorInterceptor.class);
            }

            final MongoConfig config = new MongoConfig(instrumentor.getProfilerConfig());
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getReadOperationMethodList())))) {
                method.addScopedInterceptor(MongoCollectionImplReadOperationInterceptor.class, va(config.isCollectJson(), config.istraceBsonBindValue()), MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getWriteOperationMethodList())))) {
                method.addScopedInterceptor(MongoCollectionImplWriteOperationInterceptor.class, va(config.isCollectJson(), config.istraceBsonBindValue()), MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("withDocumentClass", "withCodecRegistry", "withReadPreference", "withWriteConcern", "withReadConcern"))) {
                method.addInterceptor(MongoCollectionImplWithInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ReactiveMongoClientImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(HostListAccessor.class);

            // 4.2
            InstrumentMethod constructorMethod = target.getConstructor("com.mongodb.MongoClientSettings", "com.mongodb.internal.connection.Cluster", "com.mongodb.reactivestreams.client.internal.OperationExecutor", "java.io.Closeable");
            if (constructorMethod == null) {
                // 4.6 or later
                constructorMethod = target.getConstructor("com.mongodb.MongoClientSettings", "com.mongodb.MongoDriverInformation", "com.mongodb.internal.connection.Cluster", "com.mongodb.reactivestreams.client.internal.OperationExecutor", "java.io.Closeable");
            }
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(ReactiveMongoClientImplConstructorInterceptor.class);
            }

            final InstrumentMethod getDatabaseMethod = target.getDeclaredMethod("getDatabase", "java.lang.String");
            if (getDatabaseMethod != null) {
                getDatabaseMethod.addInterceptor(ReactiveMongoClientImplGetDatabaseInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ReactiveMongoDatabaseImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(HostListAccessor.class);

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("withCodecRegistry", "withReadPreference", "withWriteConcern", "withReadConcern"))) {
                method.addInterceptor(ReactiveMongoDatabaseImplWithInterceptor.class);
            }

            final InstrumentMethod getCollectionMethod = target.getDeclaredMethod("getCollection", "java.lang.String");
            if (getCollectionMethod != null) {
                getCollectionMethod.addInterceptor(ReactiveMongoDatabaseImplGetCollectionInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ReactiveMongoOperationPublisherTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(HostListAccessor.class);
            target.addField(DatabaseInfoAccessor.class);

            // 4.2
            InstrumentMethod constructorMethod = target.getConstructor("com.mongodb.MongoNamespace", "java.lang.Class", "org.bson.codecs.configuration.CodecRegistry", "com.mongodb.ReadPreference", "com.mongodb.ReadConcern", "com.mongodb.WriteConcern", "boolean", "boolean", "org.bson.UuidRepresentation", "com.mongodb.reactivestreams.client.internal.OperationExecutor");
            if (constructorMethod == null) {
                // 4.7 or later
                constructorMethod = target.getConstructor("com.mongodb.MongoNamespace", "java.lang.Class", "org.bson.codecs.configuration.CodecRegistry", "com.mongodb.ReadPreference", "com.mongodb.ReadConcern", "com.mongodb.WriteConcern", "boolean", "boolean", "org.bson.UuidRepresentation", "com.mongodb.AutoEncryptionSettings", "com.mongodb.reactivestreams.client.internal.OperationExecutor");
            }
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(ReactiveMongoOperationPublisherConstructorInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ReactiveMongoCollectionImplTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(HostListAccessor.class);
            target.addField(DatabaseInfoAccessor.class);

            // 4.2
            final InstrumentMethod constructorMethod = target.getConstructor("com.mongodb.reactivestreams.client.internal.MongoOperationPublisher");
            if (constructorMethod != null) {
                constructorMethod.addInterceptor(ReactiveMongoCollectionImplConstructorInterceptor.class);
            }

            final MongoConfig config = new MongoConfig(instrumentor.getProfilerConfig());
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getReadOperationMethodList())))) {
                method.addScopedInterceptor(ReactiveMongoCollectionImplReadOperationInterceptor.class, va(config.isCollectJson(), config.istraceBsonBindValue()), MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(getWriteOperationMethodList())))) {
                method.addScopedInterceptor(ReactiveMongoCollectionImplWriteOperationInterceptor.class, va(config.isCollectJson(), config.istraceBsonBindValue()), MONGO_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("withDocumentClass", "withCodecRegistry", "withReadPreference", "withWriteConcern", "withReadConcern"))) {
                method.addInterceptor(ReactiveMongoCollectionImplWithInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ObservableToPublisherTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "org.reactivestreams.Subscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxAndMonoSubscribeInterceptor.class);
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

    private static String[] getReadOperationMethodList() {
        final String[] methodList = new String[]{"findOneAndUpdate", "findOneAndReplace", "findOneAndDelete", "find", "count", "distinct", "listIndexes", "countDocuments"};
        return methodList;
    }

    private static String[] getWriteOperationMethodList() {
        final String[] methodlist = new String[]{"dropIndexes", "dropIndex", "createIndexes", "createIndex", "updateMany", "updateOne", "replaceOne", "deleteMany", "deleteOne", "insertMany", "insertOne", "bulkWrite"};
        return methodlist;
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
