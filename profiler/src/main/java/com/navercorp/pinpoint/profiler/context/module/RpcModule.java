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

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.navercorp.pinpoint.profiler.context.provider.CommandDispatcherProvider;
import com.navercorp.pinpoint.profiler.context.provider.ConnectionFactoryProviderProvider;
import com.navercorp.pinpoint.profiler.context.provider.HeaderTBaseSerializerProvider;
import com.navercorp.pinpoint.profiler.context.provider.MetadataMessageConverterProvider;
import com.navercorp.pinpoint.profiler.context.provider.PinpointClientFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.SpanClientFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.SpanDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.SpanStatChannelFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.SpanStatConnectTimerProvider;
import com.navercorp.pinpoint.profiler.context.provider.SpanThriftMessageConverterProvider;
import com.navercorp.pinpoint.profiler.context.provider.StatClientFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.StatDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.TcpDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.client.ConnectionFactoryProvider;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import org.apache.thrift.TBase;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.util.Timer;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RpcModule extends PrivateModule {
    @Override
    protected void configure() {
        Key<CommandDispatcher> commandDispatcher = Key.get(CommandDispatcher.class);
        bind(commandDispatcher).toProvider(CommandDispatcherProvider.class).in(Scopes.SINGLETON);
        expose(commandDispatcher);

        bind(ConnectionFactoryProvider.class).toProvider(ConnectionFactoryProviderProvider.class).in(Scopes.SINGLETON);

        Key<PinpointClientFactory> pinpointClientFactory = Key.get(PinpointClientFactory.class, DefaultClientFactory.class);
        bind(pinpointClientFactory).toProvider(PinpointClientFactoryProvider.class).in(Scopes.SINGLETON);
        expose(pinpointClientFactory);

        bind(HeaderTBaseSerializer.class).toProvider(HeaderTBaseSerializerProvider.class).in(Scopes.SINGLETON);

        TypeLiteral<EnhancedDataSender<Object>> dataSenderTypeLiteral = new TypeLiteral<EnhancedDataSender<Object>>() {};
        bind(dataSenderTypeLiteral).toProvider(TcpDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(dataSenderTypeLiteral);

        Key<Timer> spanStatConnectTimer = Key.get(Timer.class, SpanStatConnectTimer.class);
        bind(spanStatConnectTimer).toProvider(SpanStatConnectTimerProvider.class).in(Scopes.SINGLETON);
        expose(spanStatConnectTimer);

        Key<ChannelFactory> spanStatChannelFactory = Key.get(ChannelFactory.class, SpanStatChannelFactory.class);
        bind(spanStatChannelFactory).toProvider(SpanStatChannelFactoryProvider.class).in(Scopes.SINGLETON);
        expose(spanStatChannelFactory);

        Key<PinpointClientFactory> spanClientFactory = Key.get(PinpointClientFactory.class, SpanClientFactory.class);
        bind(spanClientFactory).toProvider(SpanClientFactoryProvider.class).in(Scopes.SINGLETON);
        expose(spanClientFactory);

        Key<PinpointClientFactory> statClientFactory = Key.get(PinpointClientFactory.class, StatClientFactory.class);
        bind(statClientFactory).toProvider(StatClientFactoryProvider.class).in(Scopes.SINGLETON);
        expose(statClientFactory);

        TypeLiteral<MessageConverter<TBase<?, ?>>> thriftMessageConverter = new TypeLiteral<MessageConverter<TBase<?, ?>>>() {};
        Key<MessageConverter<TBase<?, ?>>> spanMessageConverterKey = Key.get(thriftMessageConverter, SpanConverter.class);
        bind(spanMessageConverterKey).toProvider(SpanThriftMessageConverterProvider.class ).in(Scopes.SINGLETON);
        expose(spanMessageConverterKey);

        Key<MessageConverter<TBase<?, ?>>> metadataMessageConverterKey = Key.get(thriftMessageConverter, MetadataConverter.class);
        bind(metadataMessageConverterKey).toProvider(MetadataMessageConverterProvider.class ).in(Scopes.SINGLETON);
        expose(metadataMessageConverterKey);

        Key<DataSender> spanDataSender = Key.get(DataSender.class, SpanDataSender.class);
        bind(spanDataSender).toProvider(SpanDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(spanDataSender);

        Key<DataSender> statDataSender = Key.get(DataSender.class, StatDataSender.class);
        bind(DataSender.class).annotatedWith(StatDataSender.class)
                .toProvider(StatDataSenderProvider.class).in(Scopes.SINGLETON);
        expose(statDataSender);
    }

}
