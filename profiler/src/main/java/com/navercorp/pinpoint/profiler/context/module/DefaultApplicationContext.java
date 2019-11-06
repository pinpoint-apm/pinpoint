/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.module.ClassFileTransformModuleAdaptor;
import com.navercorp.pinpoint.bootstrap.module.JavaModuleFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.javamodule.ClassFileTransformerModuleHandler;
import com.navercorp.pinpoint.profiler.context.javamodule.JavaModuleFactoryFinder;
import com.navercorp.pinpoint.profiler.instrument.ASMBytecodeDumpService;
import com.navercorp.pinpoint.profiler.instrument.BytecodeDumpTransformer;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.instrument.lambda.LambdaTransformBootloader;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.monitor.AgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.DeadlockMonitor;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultApplicationContext implements ApplicationContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;

    private final DeadlockMonitor deadlockMonitor;
    private final AgentInfoSender agentInfoSender;
    private final AgentStatMonitor agentStatMonitor;

    private final TraceContext traceContext;

    private final ModuleLifeCycle rpcModuleLifeCycle;

    private final AgentInformation agentInformation;
    private final ServerMetaDataRegistryService serverMetaDataRegistryService;

    private final ClassFileTransformer classFileTransformer;

    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final InterceptorRegistryBinder interceptorRegistryBinder;

    private final Injector injector;

    public DefaultApplicationContext(AgentOption agentOption, ModuleFactory moduleFactory) {
        Assert.requireNonNull(agentOption, "agentOption");
        Assert.requireNonNull(moduleFactory, "moduleFactory");
        Assert.requireNonNull(agentOption.getProfilerConfig(), "profilerConfig");

        final Instrumentation instrumentation = agentOption.getInstrumentation();
        if (logger.isInfoEnabled()) {
            logger.info("DefaultAgent classLoader:{}", this.getClass().getClassLoader());
        }

        final Module applicationContextModule = moduleFactory.newModule(agentOption);
        this.injector = Guice.createInjector(Stage.PRODUCTION, applicationContextModule);

        this.profilerConfig = injector.getInstance(ProfilerConfig.class);
        this.interceptorRegistryBinder = injector.getInstance(InterceptorRegistryBinder.class);

        this.instrumentEngine = injector.getInstance(InstrumentEngine.class);

        this.classFileTransformer = injector.getInstance(ClassFileTransformer.class);
        this.dynamicTransformTrigger = injector.getInstance(DynamicTransformTrigger.class);

        ClassFileTransformer classFileTransformer = wrap(this.classFileTransformer);
        final JvmVersion version = JvmUtils.getVersion();
        if (version.onOrAfter(JvmVersion.JAVA_9)) {
            final JavaModuleFactory javaModuleFactory = JavaModuleFactoryFinder.lookup(instrumentation);
            ClassFileTransformModuleAdaptor classFileTransformModuleAdaptor = new ClassFileTransformerModuleHandler(instrumentation, classFileTransformer, javaModuleFactory);
            classFileTransformer = wrapJava9ClassFileTransformer(classFileTransformModuleAdaptor);

            lambdaFactorySetup(instrumentation, classFileTransformModuleAdaptor, javaModuleFactory);

            instrumentation.addTransformer(classFileTransformer, true);
        } else {
            instrumentation.addTransformer(classFileTransformer, true);
        }


        this.rpcModuleLifeCycle = injector.getInstance(Key.get(ModuleLifeCycle.class, Names.named("RPC-MODULE")));
        logger.info("rpcModuleLifeCycle:{}", rpcModuleLifeCycle);
        this.rpcModuleLifeCycle.start();

        this.traceContext = injector.getInstance(TraceContext.class);

        this.agentInformation = injector.getInstance(AgentInformation.class);
        logger.info("agentInformation:{}", agentInformation);
        this.serverMetaDataRegistryService = injector.getInstance(ServerMetaDataRegistryService.class);

        this.deadlockMonitor = injector.getInstance(DeadlockMonitor.class);
        this.agentInfoSender = injector.getInstance(AgentInfoSender.class);
        this.agentStatMonitor = injector.getInstance(AgentStatMonitor.class);
    }

    private void lambdaFactorySetup(Instrumentation instrumentation, ClassFileTransformModuleAdaptor classFileTransformer, JavaModuleFactory javaModuleFactory) {
        final JvmVersion version = JvmUtils.getVersion();
//      TODO version.onOrAfter(JvmVersion.JAVA_8)
        if (version.onOrAfter(JvmVersion.JAVA_9)) {
            LambdaTransformBootloader lambdaTransformBootloader = new LambdaTransformBootloader();
            lambdaTransformBootloader.transformLambdaFactory(instrumentation, classFileTransformer, javaModuleFactory);
        }
    }

    private ClassFileTransformer wrapJava9ClassFileTransformer(ClassFileTransformModuleAdaptor classFileTransformer) {
        logger.info("initialize Java9ClassFileTransformer");
        String moduleWrap = "com.navercorp.pinpoint.bootstrap.java9.module.ClassFileTransformerModuleWrap";
        try {
            Class<ClassFileTransformer> cftClass = (Class<ClassFileTransformer>) forName(moduleWrap, Object.class.getClassLoader());
            Constructor<ClassFileTransformer> constructor = cftClass.getDeclaredConstructor(ClassFileTransformModuleAdaptor.class);
            return constructor.newInstance(classFileTransformer);
        } catch (Exception e) {
            throw new IllegalStateException(moduleWrap + " load fail Caused by:" + e.getMessage(), e);
        }
    }

    private Class<?> forName(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(className + " not found");
        }
    }

    private ClassFileTransformer wrap(ClassFileTransformer classFileTransformer) {
        final boolean enableBytecodeDump = profilerConfig.readBoolean(ASMBytecodeDumpService.ENABLE_BYTECODE_DUMP, ASMBytecodeDumpService.ENABLE_BYTECODE_DUMP_DEFAULT_VALUE);
        if (enableBytecodeDump) {
            logger.info("wrapBytecodeDumpTransformer");
            return BytecodeDumpTransformer.wrap(classFileTransformer, profilerConfig);
        }
        return classFileTransformer;
    }

    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    public Injector getInjector() {
        return injector;
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }

    public DataSender getSpanDataSender() {
        Key<DataSender> spanDataSenderKey = Key.get(DataSender.class, SpanDataSender.class);
        return injector.getInstance(spanDataSenderKey);
    }

    public InstrumentEngine getInstrumentEngine() {
        return instrumentEngine;
    }


    public DynamicTransformTrigger getDynamicTransformTrigger() {
        return dynamicTransformTrigger;
    }


    public ClassFileTransformer getClassFileTransformer() {
        return classFileTransformer;
    }

    public AgentInformation getAgentInformation() {
        return this.agentInformation;
    }

    public ServerMetaDataRegistryService getServerMetaDataRegistryService() {
        return this.serverMetaDataRegistryService;
    }


    @Override
    public void start() {
        this.interceptorRegistryBinder.bind();

        this.deadlockMonitor.start();
        this.agentInfoSender.start();
        this.agentStatMonitor.start();
    }

    @Override
    public void close() {
        this.agentInfoSender.stop();
        this.agentStatMonitor.stop();
        this.deadlockMonitor.stop();

        // Need to process stop
        if (rpcModuleLifeCycle != null) {
            this.rpcModuleLifeCycle.shutdown();
        }

        if (profilerConfig.getStaticResourceCleanup()) {
            this.interceptorRegistryBinder.unbind();
        }
    }

}
