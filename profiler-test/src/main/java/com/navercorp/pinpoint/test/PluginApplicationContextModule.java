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

package com.navercorp.pinpoint.test;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Providers;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.module.SpanDataSender;
import com.navercorp.pinpoint.profiler.context.module.StatDataSender;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginApplicationContextModule extends AbstractModule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public PluginApplicationContextModule() {
    }

    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());

        final DataSender<SpanType> spanDataSender = newUdpSpanDataSender();
        logger.debug("spanDataSender:{}", spanDataSender);
        TypeLiteral<DataSender<SpanType>> spanDataSenderType = new TypeLiteral<DataSender<SpanType>>() {};
        Key<DataSender<SpanType>> spanDataSenderKey = Key.get(spanDataSenderType, SpanDataSender.class);
        bind(spanDataSenderKey).toInstance(spanDataSender);

        final DataSender<MetricType> statDataSender = newUdpStatDataSender();
        logger.debug("statDataSender:{}", statDataSender);
        TypeLiteral<DataSender<MetricType>> statDataSenderType = new TypeLiteral<DataSender<MetricType>>() {};
        Key<DataSender<MetricType>> statDataSenderKey = Key.get(statDataSenderType, StatDataSender.class);
        bind(statDataSenderKey).toInstance(statDataSender);

        bind(StorageFactory.class).to(TestSpanStorageFactory.class);

        bind(PinpointClientFactory.class).toProvider(Providers.of((PinpointClientFactory)null));

        EnhancedDataSender<MetaDataType> enhancedDataSender = newTcpDataSender();
        logger.debug("enhancedDataSender:{}", enhancedDataSender);
        TypeLiteral<EnhancedDataSender<MetaDataType>> dataSenderTypeLiteral = new TypeLiteral<EnhancedDataSender<MetaDataType>>() {};
        bind(dataSenderTypeLiteral).toInstance(enhancedDataSender);

        ServerMetaDataRegistryService serverMetaDataRegistryService = newServerMetaDataRegistryService();
        bind(ServerMetaDataRegistryService.class).toInstance(serverMetaDataRegistryService);
        bind(ApiMetaDataService.class).toProvider(MockApiMetaDataServiceProvider.class).in(Scopes.SINGLETON);
    }


    private DataSender<MetricType> newUdpStatDataSender() {
        return new ListenableDataSender<>("StatDataSender");
    }

    private DataSender<SpanType> newUdpSpanDataSender() {

        ListenableDataSender<SpanType> sender = new ListenableDataSender<>("SpanDataSender");
        ListenableDataSender.Listener<SpanType> orderedSpanRecorder = new OrderedSpanRecorder();
        sender.setListener(orderedSpanRecorder);
        return sender;
    }

    private EnhancedDataSender<MetaDataType> newTcpDataSender() {
        return new TestTcpDataSender();
    }

    private ServerMetaDataRegistryService newServerMetaDataRegistryService() {
        List<String> vmArgs = RuntimeMXBeanUtils.getVmArgs();
        ServerMetaDataRegistryService serverMetaDataRegistryService = new DefaultServerMetaDataRegistryService(vmArgs);
        return serverMetaDataRegistryService;
    }


}
