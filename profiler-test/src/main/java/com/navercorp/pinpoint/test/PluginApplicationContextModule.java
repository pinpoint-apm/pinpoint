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

package com.navercorp.pinpoint.test;

import com.google.inject.AbstractModule;
import com.google.inject.util.Providers;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.profiler.context.module.SpanDataSender;
import com.navercorp.pinpoint.profiler.context.module.StatDataSender;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginApplicationContextModule extends AbstractModule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private TestableServerMetaDataListener serverMetaDataListener;
    private TestTcpDataSender tcpDataSender;
    private OrderedSpanRecorder orderedSpanRecorder;
    private ServerMetaDataHolder serverMetaDataHolder;

    public PluginApplicationContextModule() {
    }

    @Override
    protected void configure() {

        final DataSender spanDataSender = newUdpSpanDataSender();
        logger.debug("spanDataSender:{}", spanDataSender);
        bind(DataSender.class).annotatedWith(SpanDataSender.class).toInstance(spanDataSender);

        final DataSender statDataSender = newUdpStatDataSender();
        logger.debug("statDataSender:{}", statDataSender);
        bind(DataSender.class).annotatedWith(StatDataSender.class).toInstance(statDataSender);

        StorageFactory storageFactory = newStorageFactory(spanDataSender);
        logger.debug("spanFactory:{}", spanDataSender);
        bind(StorageFactory.class).toInstance(storageFactory);


        bind(PinpointClientFactory.class).toProvider(Providers.of((PinpointClientFactory)null));
        bind(PinpointClient.class).toProvider(Providers.of((PinpointClient)null));

        EnhancedDataSender enhancedDataSender = newTcpDataSender();
        logger.debug("enhancedDataSender:{}", enhancedDataSender);
        bind(EnhancedDataSender.class).toInstance(enhancedDataSender);

        ServerMetaDataHolder serverMetaDataHolder = newServerMetaDataHolder();
        logger.debug("serverMetaDataHolder:{}", serverMetaDataHolder);
        bind(ServerMetaDataHolder.class).toInstance(serverMetaDataHolder);

    }


    private DataSender newUdpStatDataSender() {
        return new ListenableDataSender<TBase<?, ?>>("StatDataSender");
    }

    private DataSender newUdpSpanDataSender() {

        ListenableDataSender<TBase<?, ?>> sender = new ListenableDataSender<TBase<?, ?>>("SpanDataSender");
        OrderedSpanRecorder orderedSpanRecorder = new OrderedSpanRecorder();
        sender.setListener(orderedSpanRecorder);
        this.orderedSpanRecorder = orderedSpanRecorder;
        return sender;
    }

    protected EnhancedDataSender newTcpDataSender() {
        TestTcpDataSender tcpDataSender = new TestTcpDataSender();
        this.tcpDataSender = tcpDataSender;
        return tcpDataSender;
    }


    private ServerMetaDataHolder newServerMetaDataHolder() {
        List<String> vmArgs = RuntimeMXBeanUtils.getVmArgs();
        ServerMetaDataHolder serverMetaDataHolder = new ResettableServerMetaDataHolder(vmArgs);
        this.serverMetaDataListener = new TestableServerMetaDataListener();
        this.serverMetaDataHolder = serverMetaDataHolder;
        serverMetaDataHolder.addListener(this.serverMetaDataListener);
        return serverMetaDataHolder;
    }

    protected StorageFactory newStorageFactory(DataSender spanDataSender) {
        logger.debug("newStorageFactory dataSender:{}", spanDataSender);
        StorageFactory storageFactory = new SimpleSpanStorageFactory(spanDataSender);
        return storageFactory;
    }

    public TestableServerMetaDataListener getServerMetaDataListener() {
        return serverMetaDataListener;
    }

    public TestTcpDataSender getTcpDataSender() {
        return tcpDataSender;
    }

    public OrderedSpanRecorder getOrderedSpanRecorder() {
        return orderedSpanRecorder;
    }
}
