/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.test.rpc;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.module.AgentDataSender;
import com.navercorp.pinpoint.profiler.context.module.MetadataDataSender;
import com.navercorp.pinpoint.profiler.context.module.ModuleLifeCycle;
import com.navercorp.pinpoint.profiler.context.module.ResultConverter;
import com.navercorp.pinpoint.profiler.context.module.SpanDataSender;
import com.navercorp.pinpoint.profiler.context.module.StatDataSender;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.ThriftMessageToResultConverterProvider;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.ResultResponse;
import com.navercorp.pinpoint.test.ListenableDataSender;
import com.navercorp.pinpoint.test.TestTcpDataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockRpcModule extends PrivateModule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MockRpcModule() {
    }


    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());

        TypeLiteral<DataSender<SpanType>> spanDataSenderType = new TypeLiteral<DataSender<SpanType>>() {};
        Key<DataSender<SpanType>> spanDataSenderKey = Key.get(spanDataSenderType, SpanDataSender.class);
        final DataSender<SpanType> spanDataSender = new ListenableDataSender<>("SpanDataSender");
        logger.debug("spanDataSender:{}", spanDataSender);
        bind(spanDataSenderKey).toInstance(spanDataSender);
        expose(spanDataSenderKey);

        TypeLiteral<DataSender<MetricType>> statDataSenderType = new TypeLiteral<DataSender<MetricType>>() {};
        Key<DataSender<MetricType>> statDataSenderKey = Key.get(statDataSenderType, StatDataSender.class);
        final DataSender<MetricType> statDataSender = new ListenableDataSender<>("StatDataSender");
        logger.debug("statDataSender:{}", statDataSender);
        bind(statDataSenderKey).toInstance(statDataSender);
        expose(statDataSenderKey);

        EnhancedDataSender<MetaDataType> enhancedDataSender = new TestTcpDataSender();
        logger.debug("enhancedDataSender:{}", enhancedDataSender);
        TypeLiteral<EnhancedDataSender<MetaDataType>> dataSenderTypeLiteral = new TypeLiteral<EnhancedDataSender<MetaDataType>>() {
        };
        bind(dataSenderTypeLiteral).toInstance(enhancedDataSender);
        expose(dataSenderTypeLiteral);

        Key<EnhancedDataSender<MetaDataType>> agentDataSender = Key.get(dataSenderTypeLiteral, AgentDataSender.class);
        bind(agentDataSender).to(dataSenderTypeLiteral).in(Scopes.SINGLETON);
        expose(agentDataSender);

        Key<EnhancedDataSender<MetaDataType>> metadataDataSender = Key.get(dataSenderTypeLiteral, MetadataDataSender.class);
        bind(metadataDataSender).to(dataSenderTypeLiteral).in(Scopes.SINGLETON);
        expose(metadataDataSender);


        TypeLiteral<MessageConverter<Object, ResultResponse>> resultMessageConverter = new TypeLiteral<MessageConverter<Object, ResultResponse>>() {};
        Key<MessageConverter<Object, ResultResponse>> resultMessageConverterKey = Key.get(resultMessageConverter, ResultConverter.class);
        bind(resultMessageConverterKey).toProvider(ThriftMessageToResultConverterProvider.class ).in(Scopes.SINGLETON);
        expose(resultMessageConverterKey);


        Key<ModuleLifeCycle> rpcModuleLifeCycleKey = Key.get(ModuleLifeCycle.class, Names.named("RPC-MODULE"));
        bind(rpcModuleLifeCycleKey).to(MockModuleLifeCycle.class).in(Scopes.SINGLETON);
        expose(rpcModuleLifeCycleKey);
    }

}