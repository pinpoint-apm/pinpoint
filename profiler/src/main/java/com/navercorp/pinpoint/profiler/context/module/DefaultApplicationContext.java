/*
 * Copyright 2017 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.ClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.instrument.ASMBytecodeDumpService;
import com.navercorp.pinpoint.profiler.instrument.BytecodeDumpTransformer;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.monitor.AgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.DeadlockMonitor;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

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

    private final PinpointClientFactory clientFactory;
    private final PinpointClient client;
    private final EnhancedDataSender tcpDataSender;

    private final DataSender statDataSender;
    private final DataSender spanDataSender;

    private final AgentInformation agentInformation;
    private final AgentOption agentOption;
    private final ServerMetaDataRegistryService serverMetaDataRegistryService;

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final ClassFileTransformerDispatcher classFileDispatcher;

    private final Instrumentation instrumentation;
    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;

    private final Injector injector;


    public DefaultApplicationContext(AgentOption agentOption, final InterceptorRegistryBinder interceptorRegistryBinder) {
        if (agentOption == null) {
            throw new NullPointerException("agentOption must not be null");
        }
        if (interceptorRegistryBinder == null) {
            throw new NullPointerException("interceptorRegistryBinder must not be null");
        }

        this.agentOption = agentOption;
        this.profilerConfig = agentOption.getProfilerConfig();
        this.instrumentation = agentOption.getInstrumentation();
        this.serviceTypeRegistryService = agentOption.getServiceTypeRegistryService();

        if (logger.isInfoEnabled()) {
            logger.info("DefaultAgent classLoader:{}", this.getClass().getClassLoader());
        }

        final Module applicationContextModule = newApplicationContextModule(agentOption, interceptorRegistryBinder);
        this.injector = Guice.createInjector(Stage.PRODUCTION, applicationContextModule);

        this.instrumentEngine = injector.getInstance(InstrumentEngine.class);

        this.classFileDispatcher = injector.getInstance(ClassFileTransformerDispatcher.class);
        this.dynamicTransformTrigger = injector.getInstance(DynamicTransformTrigger.class);
//        ClassFileTransformer classFileTransformer = injector.getInstance(ClassFileTransformer.class);
        ClassFileTransformer classFileTransformer = wrap(classFileDispatcher);
        instrumentation.addTransformer(classFileTransformer, true);

        this.spanDataSender = newUdpSpanDataSender();
        logger.info("spanDataSender:{}", spanDataSender);

        this.statDataSender = newUdpStatDataSender();
        logger.info("statDataSender:{}", statDataSender);

        this.clientFactory = injector.getInstance(PinpointClientFactory.class);
        logger.info("clientFactory:{}", clientFactory);

        this.client = injector.getInstance(PinpointClient.class);
        logger.info("client:{}", client);

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

    public ClassFileTransformer wrap(ClassFileTransformerDispatcher classFileTransformerDispatcher) {

        final boolean enableBytecodeDump = profilerConfig.readBoolean(ASMBytecodeDumpService.ENABLE_BYTECODE_DUMP, ASMBytecodeDumpService.ENABLE_BYTECODE_DUMP_DEFAULT_VALUE);
        if (enableBytecodeDump) {
            logger.info("wrapBytecodeDumpTransformer");
            return BytecodeDumpTransformer.wrap(classFileTransformerDispatcher, profilerConfig);
        }
        return classFileTransformerDispatcher;
    }

    protected Module newApplicationContextModule(AgentOption agentOption, InterceptorRegistryBinder interceptorRegistryBinder) {
        return new ApplicationContextModule(agentOption, profilerConfig, serviceTypeRegistryService, interceptorRegistryBinder);
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
    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    public Injector getInjector() {
        return injector;
    }

    @Override
    public TraceContext getTraceContext() {
        return traceContext;
    }

    public DataSender getSpanDataSender() {
        return spanDataSender;
    }

    public InstrumentEngine getInstrumentEngine() {
        return instrumentEngine;
    }


    @Override
    public DynamicTransformTrigger getDynamicTransformTrigger() {
        return dynamicTransformTrigger;
    }


    @Override
    public ClassFileTransformerDispatcher getClassFileTransformerDispatcher() {
        return classFileDispatcher;
    }

    @Override
    public AgentInformation getAgentInformation() {
        return this.agentInformation;
    }

    public ServerMetaDataRegistryService getServerMetaDataRegistryService() {
        return this.serverMetaDataRegistryService;
    }

    @Override
    public void start() {
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
        this.spanDataSender.stop();
        this.statDataSender.stop();

        closeTcpDataSender();
    }

    private void closeTcpDataSender() {
        final EnhancedDataSender tcpDataSender = this.tcpDataSender;
        if (tcpDataSender != null) {
            tcpDataSender.stop();
        }
        final PinpointClient client = this.client;
        if (client != null) {
            client.close();
        }
        final PinpointClientFactory clientFactory = this.clientFactory;
        if (clientFactory != null) {
            clientFactory.release();
        }
    }

}
