/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.provider.Provider;
import com.navercorp.pinpoint.profiler.plugin.GuardProfilerPluginContext;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.apache.thrift.TBase;

import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.DefaultAgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.service.DefaultAnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.DefaultServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.instrument.ClassInjector;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class MockApplicationContext extends DefaultApplicationContext {
    private InterceptorRegistryBinder interceptorRegistryBinder;

    public static MockApplicationContext of(String configPath) {
        ProfilerConfig profilerConfig = null;
        try {
            URL resource = MockApplicationContext.class.getClassLoader().getResource(configPath);
            if (resource == null) {
                throw new FileNotFoundException("pinpoint.config not found. configPath:" + configPath);
            }
            profilerConfig = DefaultProfilerConfig.load(resource.getPath());
            ((DefaultProfilerConfig)profilerConfig).setApplicationServerType(ServiceType.TEST_STAND_ALONE.getName());
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        return of(profilerConfig);
    }
    
    public static MockApplicationContext of(ProfilerConfig config) {
        AgentOption agentOption = new DefaultAgentOption(new DummyInstrumentation(), "mockAgent", "mockApplicationName", config, new URL[0], null, new DefaultServiceTypeRegistryService(), new DefaultAnnotationKeyRegistryService());
        InterceptorRegistryBinder binder = new TestInterceptorRegistryBinder();

        
        return new MockApplicationContext(agentOption, binder);
    }

    public MockApplicationContext(AgentOption agentOption, InterceptorRegistryBinder binder) {
        super(agentOption, binder);
        this.interceptorRegistryBinder = binder;
        binder.bind();
    }

    @Override
    protected Provider<DataSender> newUdpStatDataSenderProvider() {

        DataSender dataSender = new ListenableDataSender<TBase<?, ?>>("StatDataSender");
        return new DelegateProvider<DataSender>(dataSender);
    }

    @Override
    protected Provider<DataSender> newUdpSpanDataSenderProvider() {
        DataSender dataSender = new ListenableDataSender<TBase<?, ?>>("SpanDataSender");
        return new DelegateProvider<DataSender>(dataSender);
    }


    @Override
    protected Provider<StorageFactory> newStorageFactoryProvider(ProfilerConfig profilerConfig, DataSender spanDataSender, AgentInformation agentInformation) {
        StorageFactory storageFactory = new SimpleSpanStorageFactory(spanDataSender);
        return new DelegateProvider<StorageFactory>(storageFactory);
    }


    @Override
    protected Provider<PinpointClientFactory> newPinpointClientFactoryProvider(ProfilerConfig profilerConfig, AgentInformation agentInformation, CommandDispatcher commandDispatcher) {
        return new NullProvider<PinpointClientFactory>();
    }

    @Override
    protected Provider<PinpointClient> newPinpointClientProvider(ProfilerConfig profilerConfig, PinpointClientFactory clientFactory) {
        return new NullProvider<PinpointClient>();
    }

    @Override
    protected Provider<EnhancedDataSender> newTcpDataSenderProvider(PinpointClient client) {
        EnhancedDataSender enhancedDataSender = new TestTcpDataSender();
        return new DelegateProvider<EnhancedDataSender>(enhancedDataSender);
    }


    @Override
    protected Provider<ServerMetaDataHolder> newServerMetaDataHolderProvider() {
        List<String> vmArgs = RuntimeMXBeanUtils.getVmArgs();
        ServerMetaDataHolder serverMetaDataHolder = new ResettableServerMetaDataHolder(vmArgs);
        return new DelegateProvider<ServerMetaDataHolder>(serverMetaDataHolder);
    }
    
    @Override
    protected List<DefaultProfilerPluginContext> loadPlugins(AgentOption agentOption) {
        List<DefaultProfilerPluginContext> pluginContexts = new ArrayList<DefaultProfilerPluginContext>();
        ClassInjector classInjector = new TestProfilerPluginClassLoader();

        List<ProfilerPlugin> plugins = PluginLoader.load(ProfilerPlugin.class, ClassLoader.getSystemClassLoader());
        
        for (ProfilerPlugin plugin : plugins) {
            final DefaultProfilerPluginContext context = new DefaultProfilerPluginContext(this, classInjector);
            final GuardProfilerPluginContext guard = new GuardProfilerPluginContext(context);
            try {
                preparePlugin(plugin, context);
                plugin.setup(guard);
            } finally {
                guard.close();
            }
            pluginContexts.add(context);
        }
        
        
        return pluginContexts;

    }

    @Override
    public void close() {
        super.close();
        if (this.interceptorRegistryBinder != null) {
            interceptorRegistryBinder.unbind();
        }
    }

    /**
     * TODO duplicated code : com/navercorp/pinpoint/profiler/plugin/ProfilerPluginLoader.java
     * @param plugin
     * @param context
     */
    private void preparePlugin(ProfilerPlugin plugin, InstrumentContext context) {

        if (plugin instanceof TransformTemplateAware) {
            final TransformTemplate transformTemplate = new TransformTemplate(context);
            ((TransformTemplateAware) plugin).setTransformTemplate(transformTemplate);
        }
    }


    public static String toString(Span span) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(span.getServiceType());
        builder.append(", [");
        appendAnnotations(builder, span.getAnnotations());
        builder.append("])");
        
        return builder.toString();
    }

    public static String toString(SpanEvent span) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(span.getServiceType());
        builder.append(", [");
        appendAnnotations(builder, span.getAnnotations());
        builder.append("])");
        
        return builder.toString();
    }

    private static void appendAnnotations(StringBuilder builder, List<TAnnotation> annotations) {
        boolean first = true;
        
        if (annotations != null) {
            for (TAnnotation a : annotations) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                
                builder.append(toString(a));
            }
        }
    }

    private static String toString(TAnnotation a) {
        return a.getKey() + "=" + a.getValue().getFieldValue();
    }
    
    public static String toString(short serviceCode, ExpectedAnnotation...annotations) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(serviceCode);
        builder.append(", ");
        builder.append(Arrays.deepToString(annotations));
        builder.append(')');
        
        return builder.toString();
    }
}
