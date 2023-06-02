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

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilePropertyLoader;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.module.ClassFileTransformModuleAdaptor;
import com.navercorp.pinpoint.bootstrap.module.JavaModuleFactory;
import com.navercorp.pinpoint.common.util.*;
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
import com.navercorp.pinpoint.profiler.monitor.DefaultRemoteConfigMonitor;
import com.navercorp.pinpoint.profiler.monitor.RemoteConfigMonitor;
import com.navercorp.pinpoint.profiler.monitor.processor.ReSetConfigProcessorFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultApplicationContext implements ApplicationContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;

    private final DeadlockMonitor deadlockMonitor;
    private final AgentInfoSender agentInfoSender;
    private final AgentStatMonitor agentStatMonitor;
    private final RemoteConfigMonitor remoteConfigMonitor;

    private final TraceContext traceContext;

    private final ModuleLifeCycle rpcModuleLifeCycle;

    private final AgentInformation agentInformation;
    private final ServerMetaDataRegistryService serverMetaDataRegistryService;

    private final ClassFileTransformer classFileTransformer;

    private final InstrumentEngine instrumentEngine;
    private final DynamicTransformTrigger dynamicTransformTrigger;
    private final InterceptorRegistryBinder interceptorRegistryBinder;

    private final Injector injector;

    private ConfigService configService;

    public DefaultApplicationContext(AgentOption agentOption, ModuleFactory moduleFactory) {
        Assert.requireNonNull(agentOption, "agentOption");
        Assert.requireNonNull(moduleFactory, "moduleFactory");
        Assert.requireNonNull(agentOption.getProfilerConfig(), "profilerConfig");

        final Instrumentation instrumentation = agentOption.getInstrumentation();
        if (logger.isInfoEnabled()) {
            logger.info("DefaultAgent classLoader:{}", this.getClass().getClassLoader());
        }

        final Module applicationContextModule = moduleFactory.newModule(agentOption, this);
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
        this.remoteConfigMonitor = injector.getInstance(RemoteConfigMonitor.class);
        initRemoteConfig(agentOption);
        initNacosConfigService();
    }
    private void initRemoteConfig(AgentOption agentOption){
        try {
            Properties propertiesOrigin = profilerConfig.getProperties();
            Object remoteEnable = propertiesOrigin.get("profiler.remote.config.init.enable");
            if(remoteEnable !=null && Boolean.parseBoolean(remoteEnable.toString())){
                logger.info("activate the remote initialization configuration");
                //get remote properties
                String remoteConfigStr = loadAndSetRemoteProfile(propertiesOrigin, agentOption.getAgentLicence(), agentOption.getApplicationName());
                if(null != remoteConfigStr && !"".equals(remoteConfigStr) && !"null".equals(remoteConfigStr)){
                    //reset profilerConfig
                    ((DefaultProfilerConfig)profilerConfig).resetDefaultProfilerConfig(propertiesOrigin);
                    //reinit some config
                    ReSetConfigProcessorFactory reSetConfigProcessorFactory = ((DefaultRemoteConfigMonitor) this.remoteConfigMonitor).getReSetConfigProcessorFactory();
                    reSetConfigProcessorFactory.dealConfigInfo(remoteConfigStr);
                }
            }else{
                logger.info("skip the remote initialization configuration");
            }
        } catch (Exception e) {
            logger.error("initRemoteConfig error:"+e.getMessage());
        }
    }
    private void initNacosConfigService(){
        try {
            //init configService
            if(DefaultRemoteConfigMonitor.startRemoteMonitorIsBeginNacos(this.profilerConfig)){
                Properties nacosProperties = DefaultRemoteConfigMonitor.getNacosProperties(this.profilerConfig);
                this.configService = NacosFactory.createConfigService(nacosProperties);
            }else{
                logger.info("Uninitialized nacos");
            }
        } catch (Exception e) {
            logger.error("initNacosConfigService error:"+e.getMessage());
        }
    }
    private static final String NACOS_USERNAME="username";
    private static final String NACOS_PASSWORD="password";
    private static final String NACOS_NAMESPANCE="namespaceCenter";

    private String loadAndSetRemoteProfile(Properties defaultProperties, String licence, String appName){
        String profileStr = null;
        String nacosUrl = "http://%s/nacos/v1/cs/configs?username="+NACOS_USERNAME+"&password="+NACOS_PASSWORD+"&tenant="+NACOS_NAMESPANCE+"&dataId=%s&group=%s";

        if(!StringUtils.isEmpty(licence)
                && !StringUtils.isEmpty(appName)
                && null!=defaultProperties
                && !StringUtils.isEmpty(defaultProperties.getProperty("profiler.remote.config.addr"))
        ){
            String remoteAddrRoot = defaultProperties.getProperty("profiler.remote.config.addr");
            int connectTimeout = 3000;
            int readTimeout = 2000;
            try {
                connectTimeout = Integer.parseInt(defaultProperties.getProperty("profiler.remote.config.connectTimeout", "3000"));
                readTimeout = Integer.parseInt(defaultProperties.getProperty("profiler.remote.config.readTimeout", "2000"));
            } catch (Exception e) {
            }
            int forSize = remoteAddrRoot.contains(",") ? remoteAddrRoot.split(",").length : 1;
            for(int i=0; i<forSize; i++){
                try {
                    String remoteAddr = remoteAddrRoot.contains(",") ? remoteAddrRoot.split(",")[i] : remoteAddrRoot;
                    //精准匹配licence和appName dataId=licence&&group=appName
                    String level = licence+":"+ appName;
                    profileStr = HttpUtils.doGet(String.format(nacosUrl
                            , remoteAddr
                            , licence
                            , appName)
                            , connectTimeout, readTimeout);
                    if(StringUtils.isEmpty(profileStr)){
                        //无精准匹配则二次匹配 licence dataId=licence&&group=default
                        level = licence+":default";
                        profileStr = HttpUtils.doGet(String.format(nacosUrl
                                , remoteAddr
                                , licence
                                , "default")
                                , connectTimeout, readTimeout);
                    }
                    if(StringUtils.isEmpty(profileStr)){
                        //三次匹配采用 默认模板 dataId=default&&group=default
                        level = "default:default";
                        profileStr = HttpUtils.doGet(String.format(nacosUrl
                                , remoteAddr
                                , "default"
                                , "default")
                                , connectTimeout, readTimeout);
                    }

                    if(!StringUtils.isEmpty(profileStr)){
                        logger.info(String.format("use remote config level: [%s]", level));
                        ProfilePropertyLoader.loadFilePropertiesByProfileStr(defaultProperties, profileStr);
                        logger.info("loadAngSetRemoteProfile successes!");
                    }
                    break;
                }catch (Exception e){
                    logger.info(String.format("loadAngSetRemoteProfile failed[%s], cause by:%s", i, e.getMessage()));
                }

            }
        }
        return profileStr;
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
        this.remoteConfigMonitor.start(this.configService);
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
