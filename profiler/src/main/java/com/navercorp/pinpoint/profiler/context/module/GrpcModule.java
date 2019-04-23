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
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.proto.SpanProtoMessageConverterProvider;
import com.navercorp.pinpoint.profiler.context.provider.CommandDispatcherProvider;
import com.navercorp.pinpoint.profiler.context.provider.ConnectionFactoryProviderProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.DnsExecutorServiceProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.GrpcDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.HeaderTBaseSerializerProvider;
import com.navercorp.pinpoint.profiler.context.provider.MetadataMessageConverterProvider;
import com.navercorp.pinpoint.profiler.context.provider.PinpointClientFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.SpanStatClientFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.StatDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.TcpDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.GrpcSpanProcessorProvider;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.client.ConnectionFactoryProvider;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import org.apache.thrift.TBase;

import java.util.concurrent.ExecutorService;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcModule extends PrivateModule {
    @Override
    protected void configure() {
        Key<CommandDispatcher> commandDispatcher = Key.get(CommandDispatcher.class);
        bind(commandDispatcher).toProvider(CommandDispatcherProvider.class).in(Scopes.SINGLETON);

        TypeLiteral<SpanProcessor<PSpan.Builder, PSpanChunk.Builder>> spanPostProcessorType = new TypeLiteral<SpanProcessor<PSpan.Builder, PSpanChunk.Builder>>() {};
        bind(spanPostProcessorType).toProvider(GrpcSpanProcessorProvider.class).in(Scopes.SINGLETON);

        // dns executor
        bind(ExecutorService.class).toProvider(DnsExecutorServiceProvider.class).in(Scopes.SINGLETON);

        bind(ConnectionFactoryProvider.class).toProvider(ConnectionFactoryProviderProvider.class).in(Scopes.SINGLETON);

        Key<PinpointClientFactory> pinpointClientFactory = Key.get(PinpointClientFactory.class, DefaultClientFactory.class);
        bind(pinpointClientFactory).toProvider(PinpointClientFactoryProvider.class).in(Scopes.SINGLETON);

        bind(HeaderTBaseSerializer.class).toProvider(HeaderTBaseSerializerProvider.class).in(Scopes.SINGLETON);

        TypeLiteral<EnhancedDataSender<Object>> dataSenderTypeLiteral = new TypeLiteral<EnhancedDataSender<Object>>() {};
        bind(dataSenderTypeLiteral).toProvider(TcpDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(dataSenderTypeLiteral);

        Key<PinpointClientFactory> pinpointStatClientFactory = Key.get(PinpointClientFactory.class, SpanStatClientFactory.class);
        bind(pinpointStatClientFactory).toProvider(SpanStatClientFactoryProvider.class).in(Scopes.SINGLETON);


        TypeLiteral<MessageConverter<GeneratedMessageV3>> protoMessageConverter = new TypeLiteral<MessageConverter<GeneratedMessageV3>>() {};
        Key<MessageConverter<GeneratedMessageV3>> spanMessageConverterKey = Key.get(protoMessageConverter, SpanConverter.class);
        // not singletone
        bind(spanMessageConverterKey).toProvider(SpanProtoMessageConverterProvider.class);

        TypeLiteral<MessageConverter<TBase<?, ?>>> thriftMessageConverter = new TypeLiteral<MessageConverter<TBase<?, ?>>>() {};
        Key<MessageConverter<TBase<?, ?>>> metadataMessageConverterKey = Key.get(thriftMessageConverter, MetadataConverter.class);
        bind(metadataMessageConverterKey).toProvider(MetadataMessageConverterProvider.class ).in(Scopes.SINGLETON);


        Key<DataSender> spanDataSender = Key.get(DataSender.class, SpanDataSender.class);
        bind(spanDataSender).toProvider(GrpcDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(spanDataSender);

        Key<DataSender> statDataSender = Key.get(DataSender.class, StatDataSender.class);
        bind(DataSender.class).annotatedWith(StatDataSender.class)
                .toProvider(StatDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(statDataSender);


        Key<ModuleLifeCycle> rpcModuleLifeCycleKey = Key.get(ModuleLifeCycle.class, Names.named("RPC-MODULE"));
        bind(rpcModuleLifeCycleKey).to(GrpcModuleLifeCycle.class).in(Scopes.SINGLETON);
        expose(rpcModuleLifeCycleKey);
    }

}
