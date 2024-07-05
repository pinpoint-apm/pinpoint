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
import com.navercorp.pinpoint.common.profiler.message.AsyncDataSender;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.common.profiler.message.ResultResponse;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcMetadataMessageConverterProvider;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcSpanMessageConverterProvider;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcStatMessageConverterProvider;
import com.navercorp.pinpoint.profiler.context.grpc.config.GrpcTransportConfig;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanUriGetter;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanUriGetterProvider;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AgentInfoMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AgentStatMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.AnnotationValueMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.CustomMetricMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.ExceptionMetaDataMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.JvmGcTypeMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.MetaDataMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.SpanMessageMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.ThreadDumpMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.UriStatMapper;
import com.navercorp.pinpoint.profiler.context.provider.grpc.AgentGrpcDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.AgentHeaderFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.DnsExecutorServiceProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.GrpcNameResolverProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.GrpcSpanProcessorProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.MetadataGrpcDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.ReconnectExecutorProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.ReconnectSchedulerProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.SSLContextProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.SpanGrpcDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.StatGrpcDataSenderProvider;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;
import com.navercorp.pinpoint.profiler.sender.grpc.SubconnectionExpiringLoadBalancerProvider;
import com.navercorp.pinpoint.profiler.sender.grpc.metric.ChannelzScheduledReporter;
import com.navercorp.pinpoint.profiler.sender.grpc.metric.ChannelzScheduledReporterBuilder;
import io.grpc.LoadBalancerRegistry;
import io.grpc.NameResolverProvider;
import io.netty.handler.ssl.SslContext;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapstruct.factory.Mappers;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcModule extends PrivateModule {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;

    private final ChannelzScheduledReporter reporter;

    public GrpcModule(ProfilerConfig profilerConfig) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.reporter = new ChannelzScheduledReporterBuilder().acceptConfig(this.profilerConfig).build();
    }

    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());
        GrpcTransportConfig grpcTransportConfig = loadGrpcTransportConfig();
        bind(GrpcTransportConfig.class).toInstance(grpcTransportConfig);

        bind(ChannelzScheduledReporter.class).toInstance(this.reporter);

        // dns executor
        bind(ExecutorService.class).toProvider(DnsExecutorServiceProvider.class).in(Scopes.SINGLETON);
        bind(NameResolverProvider.class).toProvider(GrpcNameResolverProvider.class).in(Scopes.SINGLETON);
        bind(HeaderFactory.class).toProvider(AgentHeaderFactoryProvider.class).in(Scopes.SINGLETON);

        bind(ScheduledExecutorService.class).toProvider(ReconnectSchedulerProvider.class).in(Scopes.SINGLETON);

        registerGrpcProviders(grpcTransportConfig);

        bind(SslContext.class).toProvider(SSLContextProvider.class).in(Scopes.SINGLETON);

        // not singleton
        bind(ReconnectExecutor.class).toProvider(ReconnectExecutorProvider.class);

        bindMappers();

        bindAgentDataSender();

        bindSpanDataSender();

        bindStatDataSender();

        Key<ModuleLifeCycle> rpcModuleLifeCycleKey = Key.get(ModuleLifeCycle.class, Names.named("RPC-MODULE"));
        bind(rpcModuleLifeCycleKey).to(GrpcModuleLifeCycle.class).in(Scopes.SINGLETON);
        expose(rpcModuleLifeCycleKey);

        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);

        NettyPlatformDependent nettyPlatformDependent = new NettyPlatformDependent(profilerConfig, System.getProperties());
        nettyPlatformDependent.setup();
    }

    private void registerGrpcProviders(GrpcTransportConfig grpcTransportConfig) {
        LoadBalancerRegistry.getDefaultRegistry().register(new SubconnectionExpiringLoadBalancerProvider(grpcTransportConfig));
    }

    private void bindMappers() {
        bind(SpanUriGetter.class).toProvider(SpanUriGetterProvider.class).in(Scopes.SINGLETON);

        bind(AnnotationValueMapper.class).toInstance(Mappers.getMapper(AnnotationValueMapper.class));
        bind(JvmGcTypeMapper.class).toInstance(Mappers.getMapper(JvmGcTypeMapper.class));

        bind(CustomMetricMapper.class).toInstance(Mappers.getMapper(CustomMetricMapper.class));
        bind(ExceptionMetaDataMapper.class).toInstance(Mappers.getMapper(ExceptionMetaDataMapper.class));
        bind(MetaDataMapper.class).toInstance(Mappers.getMapper(MetaDataMapper.class));
        bind(ThreadDumpMapper.class).toInstance(Mappers.getMapper(ThreadDumpMapper.class));
        bind(UriStatMapper.class).toInstance(Mappers.getMapper(UriStatMapper.class));


        bind(SpanMessageMapper.class).to(com.navercorp.pinpoint.profiler.context.grpc.mapper.SpanMessageMapperImpl.class);
        bind(AgentInfoMapper.class).to(com.navercorp.pinpoint.profiler.context.grpc.mapper.AgentInfoMapperImpl.class);
        bind(AgentStatMapper.class).to(com.navercorp.pinpoint.profiler.context.grpc.mapper.AgentStatMapperImpl.class);
    }

    private void bindAgentDataSender() {
        // Agent
        TypeLiteral<MessageConverter<MetaDataType, GeneratedMessageV3>> metadataMessageConverter = new TypeLiteral<MessageConverter<MetaDataType, GeneratedMessageV3>>() {
        };
        Key<MessageConverter<MetaDataType, GeneratedMessageV3>> metadataMessageConverterKey = Key.get(metadataMessageConverter, MetadataDataSender.class);
        bind(metadataMessageConverterKey).toProvider(GrpcMetadataMessageConverterProvider.class).in(Scopes.SINGLETON);

        TypeLiteral<AsyncDataSender<MetaDataType, ResultResponse>> agentDataSenderTypeLiteral = new TypeLiteral<AsyncDataSender<MetaDataType, ResultResponse>>() {
        };
        Key<AsyncDataSender<MetaDataType, ResultResponse>> agentDataSender = Key.get(agentDataSenderTypeLiteral, AgentDataSender.class);
        bind(agentDataSender).toProvider(AgentGrpcDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(agentDataSender);

        TypeLiteral<EnhancedDataSender<MetaDataType>> metaDataSenderTypeLiteral = new TypeLiteral<EnhancedDataSender<MetaDataType>>() {
        };
        Key<EnhancedDataSender<MetaDataType>> metadataDataSender = Key.get(metaDataSenderTypeLiteral, MetadataDataSender.class);
        bind(metadataDataSender).toProvider(MetadataGrpcDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(metadataDataSender);
    }

    private void bindStatDataSender() {
        // Stat
        TypeLiteral<MessageConverter<MetricType, GeneratedMessageV3>> statMessageConverter = new TypeLiteral<MessageConverter<MetricType, GeneratedMessageV3>>() {
        };
        Key<MessageConverter<MetricType, GeneratedMessageV3>> statMessageConverterKey = Key.get(statMessageConverter, StatDataSender.class);
        bind(statMessageConverterKey).toProvider(GrpcStatMessageConverterProvider.class).in(Scopes.SINGLETON);

        TypeLiteral<DataSender<MetricType>> statDataSenderType = new TypeLiteral<DataSender<MetricType>>() {
        };
        Key<DataSender<MetricType>> statDataSender = Key.get(statDataSenderType, StatDataSender.class);
        bind(statDataSender).toProvider(StatGrpcDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(statDataSender);
    }

    private void bindSpanDataSender() {
        // Span
        TypeLiteral<MessageConverter<SpanType, GeneratedMessageV3>> protoMessageConverter = new TypeLiteral<MessageConverter<SpanType, GeneratedMessageV3>>() {
        };
        Key<MessageConverter<SpanType, GeneratedMessageV3>> spanMessageConverterKey = Key.get(protoMessageConverter, SpanDataSender.class);

        // not singleton
        bind(spanMessageConverterKey).toProvider(GrpcSpanMessageConverterProvider.class);
        TypeLiteral<SpanProcessor<PSpan.Builder, PSpanChunk.Builder>> spanPostProcessorType = new TypeLiteral<SpanProcessor<PSpan.Builder, PSpanChunk.Builder>>() {
        };
        bind(spanPostProcessorType).toProvider(GrpcSpanProcessorProvider.class).in(Scopes.SINGLETON);

        TypeLiteral<DataSender<SpanType>> spanDataSenderType = new TypeLiteral<DataSender<SpanType>>() {
        };
        Key<DataSender<SpanType>> spanDataSender = Key.get(spanDataSenderType, SpanDataSender.class);
        bind(spanDataSender).toProvider(SpanGrpcDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(spanDataSender);
    }


    private GrpcTransportConfig loadGrpcTransportConfig() {
        GrpcTransportConfig grpcTransportConfig = new GrpcTransportConfig();
        grpcTransportConfig.read(profilerConfig.getProperties());
        logger.info("{}", grpcTransportConfig);
        return grpcTransportConfig;
    }

}