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

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.module.ClassFileTransformModuleAdaptor;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.javamodule.ClassFileTransformerModuleHandler;
import com.navercorp.pinpoint.profiler.instrument.ASMBytecodeDumpService;
import com.navercorp.pinpoint.profiler.instrument.BytecodeDumpTransformer;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.monitor.AgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.DeadlockMonitor;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;

/**
 * @author Taejin Koo
 */
public class PinpointModuleInstanceManager implements ModuleInstanceManager, ModuleInstanceHolder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;

    private final Injector injector;

    private final DeadlockMonitor deadlockMonitor;
    private final AgentInfoSender agentInfoSender;
    private final AgentStatMonitor agentStatMonitor;

    private final TraceContext traceContext;

    private final PinpointClientFactory clientFactory;
    private final EnhancedDataSender tcpDataSender;

    private final PinpointClientFactory spanStatClientFactory;
    private final DataSender spanDataSender;
    private final DataSender statDataSender;

    private final AgentInformation agentInformation;
    private final ServerMetaDataRegistryService serverMetaDataRegistryService;

    private final ClassFileTransformer classFileTransformer;

    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final InterceptorRegistryBinder interceptorRegistryBinder;


    public PinpointModuleInstanceManager(Injector injector, Instrumentation instrumentation) {
        this.injector = Assert.requireNonNull(injector, "injector must not be null");
        Assert.requireNonNull(instrumentation, "instrumentation must not be null");

        this.profilerConfig = injector.getInstance(ProfilerConfig.class);
        this.interceptorRegistryBinder = injector.getInstance(InterceptorRegistryBinder.class);

        this.instrumentEngine = injector.getInstance(InstrumentEngine.class);

        this.classFileTransformer = injector.getInstance(ClassFileTransformer.class);
        this.dynamicTransformTrigger = injector.getInstance(DynamicTransformTrigger.class);

        ClassFileTransformer classFileTransformer = wrap(this.classFileTransformer);
        final JvmVersion version = JvmUtils.getVersion();
        if (version.onOrAfter(JvmVersion.JAVA_9)) {
            ClassFileTransformModuleAdaptor classFileTransformModuleAdaptor = new ClassFileTransformerModuleHandler(instrumentation, classFileTransformer);
            classFileTransformer = wrapJava9ClassFileTransformer(classFileTransformModuleAdaptor);
        }
        instrumentation.addTransformer(classFileTransformer, true);

        this.spanStatClientFactory = injector.getInstance(Key.get(PinpointClientFactory.class, SpanStatClientFactory.class));
        logger.info("spanStatClientFactory:{}", spanStatClientFactory);

        this.spanDataSender = newUdpSpanDataSender();
        logger.info("spanDataSender:{}", spanDataSender);

        this.statDataSender = newUdpStatDataSender();
        logger.info("statDataSender:{}", statDataSender);

        this.clientFactory = injector.getInstance(Key.get(PinpointClientFactory.class, DefaultClientFactory.class));
        logger.info("clientFactory:{}", clientFactory);

        this.tcpDataSender = injector.getInstance(EnhancedDataSender.class);
        logger.info("tcpDataSender:{}", tcpDataSender);

        this.traceContext = injector.getInstance(TraceContext.class);

        this.agentInformation = injector.getInstance(AgentInformation.class);
        logger.info("agentInformation:{}", agentInformation);
        this.serverMetaDataRegistryService = injector.getInstance(ServerMetaDataRegistryService.class);

        this.deadlockMonitor = injector.getInstance(DeadlockMonitor.class);
        this.agentInfoSender = injector.getInstance(AgentInfoSender.class);
        this.agentStatMonitor = injector.getInstance(AgentStatMonitor.class);
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

    private DataSender newUdpStatDataSender() {
        Key<DataSender> statDataSenderKey = Key.get(DataSender.class, StatDataSender.class);
        return injector.getInstance(statDataSenderKey);
    }

    private DataSender newUdpSpanDataSender() {
        Key<DataSender> spanDataSenderKey = Key.get(DataSender.class, SpanDataSender.class);
        return injector.getInstance(spanDataSenderKey);
    }


    @Override
    public void start() {
        this.interceptorRegistryBinder.bind();

        this.deadlockMonitor.start();
        this.agentInfoSender.start();
        this.agentStatMonitor.start();
    }

    @Override
    public ModuleInstanceHolder getModuleInstanceHolder() {
        return this;
    }

    @Override
    public void stop() {
        this.agentInfoSender.stop();
        this.agentStatMonitor.stop();
        this.deadlockMonitor.stop();

        // Need to process stop
        this.spanDataSender.stop();
        this.statDataSender.stop();
        if (spanStatClientFactory != null) {
            spanStatClientFactory.release();
        }

        closeTcpDataSender();

        if (profilerConfig.getStaticResourceCleanup()) {
            this.interceptorRegistryBinder.unbind();
        }
    }

    private void closeTcpDataSender() {
        final EnhancedDataSender tcpDataSender = this.tcpDataSender;
        if (tcpDataSender != null) {
            tcpDataSender.stop();
        }
        final PinpointClientFactory clientFactory = this.clientFactory;
        if (clientFactory != null) {
            clientFactory.release();
        }
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    @Override
    public TraceContext getTraceContext() {
        return traceContext;
    }

    @Override
    public DataSender getSpanDataSender() {
        return spanDataSender;
    }

    @Override
    public AgentInformation getAgentInformation() {
        return agentInformation;
    }

    @Override
    public ServerMetaDataRegistryService getServerMetaDataRegistryService() {
        return serverMetaDataRegistryService;
    }

    @Override
    public ClassFileTransformer getClassFileTransformer() {
        return classFileTransformer;
    }

    @Override
    public InstrumentEngine getInstrumentEngine() {
        return instrumentEngine;
    }

    @Override
    public DynamicTransformTrigger getDynamicTransformTrigger() {
        return dynamicTransformTrigger;
    }

    @Override
    public Injector getInjector() {
        return injector;
    }

}
