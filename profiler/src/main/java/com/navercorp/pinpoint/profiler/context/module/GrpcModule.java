/*
 * Copyright 2019 NAVER Corp.
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

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.protobuf.GeneratedMessageV3;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcTransportConfig;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcMessageToResultConverterProvider;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcMetadataMessageConverterProvider;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcSpanMessageConverterProvider;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcStatMessageConverterProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.AgentGrpcDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.AgentHeaderFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.DnsExecutorServiceProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.GrpcNameResolverProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.GrpcSpanProcessorProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.GrpcTransportConfigProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.MetadataGrpcDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.ReconnectExecutorProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.ReconnectSchedulerProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.SpanGrpcDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.StatGrpcDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.ResultResponse;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;
import io.grpc.NameResolverProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcModule extends PrivateModule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;

    public GrpcModule(ProfilerConfig profilerConfig) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
    }

    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());

        bind(GrpcTransportConfig.class).toProvider(GrpcTransportConfigProvider.class).in(Scopes.SINGLETON);
        // dns executor
        bind(ExecutorService.class).toProvider(DnsExecutorServiceProvider.class).in(Scopes.SINGLETON);
        bind(NameResolverProvider.class).toProvider(GrpcNameResolverProvider.class).in(Scopes.SINGLETON);
        bind(HeaderFactory.class).toProvider(AgentHeaderFactoryProvider.class).in(Scopes.SINGLETON);

        bind(ScheduledExecutorService.class).toProvider(ReconnectSchedulerProvider.class).in(Scopes.SINGLETON);

        // not singleton
        bind(ReconnectExecutor.class).toProvider(ReconnectExecutorProvider.class);

        // Agent
        TypeLiteral<MessageConverter<GeneratedMessageV3>> metadataMessageConverter = new TypeLiteral<MessageConverter<GeneratedMessageV3>>() {};
        Key<MessageConverter<GeneratedMessageV3>> metadataMessageConverterKey = Key.get(metadataMessageConverter, MetadataConverter.class);
        bind(metadataMessageConverterKey).toProvider(GrpcMetadataMessageConverterProvider.class ).in(Scopes.SINGLETON);

        TypeLiteral<MessageConverter<ResultResponse>> resultMessageConverter = new TypeLiteral<MessageConverter<ResultResponse>>() {};
        Key<MessageConverter<ResultResponse>> resultMessageConverterKey = Key.get(resultMessageConverter, ResultConverter.class);
        bind(resultMessageConverterKey).toProvider(GrpcMessageToResultConverterProvider.class ).in(Scopes.SINGLETON);
        expose(resultMessageConverterKey);

        TypeLiteral<EnhancedDataSender<Object>> dataSenderTypeLiteral = new TypeLiteral<EnhancedDataSender<Object>>() {};
        Key<EnhancedDataSender<Object>> agentDataSender = Key.get(dataSenderTypeLiteral, AgentDataSender.class);
        bind(agentDataSender).toProvider(AgentGrpcDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(agentDataSender);

        Key<EnhancedDataSender<Object>> metadataDataSender = Key.get(dataSenderTypeLiteral, MetadataDataSender.class);
        bind(metadataDataSender).toProvider(MetadataGrpcDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(metadataDataSender);

        // Span
        TypeLiteral<MessageConverter<GeneratedMessageV3>> protoMessageConverter = new TypeLiteral<MessageConverter<GeneratedMessageV3>>() {};
        Key<MessageConverter<GeneratedMessageV3>> spanMessageConverterKey = Key.get(protoMessageConverter, SpanConverter.class);
        // not singleton
        bind(spanMessageConverterKey).toProvider(GrpcSpanMessageConverterProvider.class);

        TypeLiteral<SpanProcessor<PSpan.Builder, PSpanChunk.Builder>> spanPostProcessorType = new TypeLiteral<SpanProcessor<PSpan.Builder, PSpanChunk.Builder>>() {};
        bind(spanPostProcessorType).toProvider(GrpcSpanProcessorProvider.class).in(Scopes.SINGLETON);

        Key<DataSender> spanDataSender = Key.get(DataSender.class, SpanDataSender.class);
        bind(spanDataSender).toProvider(SpanGrpcDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(spanDataSender);

        // Stat
        TypeLiteral<MessageConverter<GeneratedMessageV3>> statMessageConverter = new TypeLiteral<MessageConverter<GeneratedMessageV3>>() {};
        Key<MessageConverter<GeneratedMessageV3>> statMessageConverterKey = Key.get(statMessageConverter, StatConverter.class);
        bind(statMessageConverterKey).toProvider(GrpcStatMessageConverterProvider.class ).in(Scopes.SINGLETON);

        Key<DataSender> statDataSender = Key.get(DataSender.class, StatDataSender.class);
        bind(DataSender.class).annotatedWith(StatDataSender.class).toProvider(StatGrpcDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(statDataSender);

        Key<ModuleLifeCycle> rpcModuleLifeCycleKey = Key.get(ModuleLifeCycle.class, Names.named("RPC-MODULE"));
        bind(rpcModuleLifeCycleKey).to(GrpcModuleLifeCycle.class).in(Scopes.SINGLETON);
        expose(rpcModuleLifeCycleKey);

        NettyPlatformDependent nettyPlatformDependent = new NettyPlatformDependent(profilerConfig, System.getProperties());
        nettyPlatformDependent.setup();
    }

}