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

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.SpanDataSender;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ApplicationContextHandler {

    private final OrderedSpanRecorder orderedSpanRecorder;
    private final TestTcpDataSender tcpDataSender;
    private final ServerMetaDataRegistryService serverMetaDataRegistryService;
    private final AnnotationKeyRegistryService annotationKeyRegistryService;
    private final ServiceTypeRegistryService serviceTypeRegistry;

    public ApplicationContextHandler(DefaultApplicationContext defaultApplicationContext) {
        Injector injector = defaultApplicationContext.getInjector();
        this.orderedSpanRecorder = findRecorder(injector);
        this.tcpDataSender = findTestTcpDataSender(injector);
        this.serverMetaDataRegistryService = findServerMetaDataRegistryService(injector);
        this.annotationKeyRegistryService = findAnnotationKeyRegistryService(injector);
        this.serviceTypeRegistry = findServiceTypeRegistry(injector);
    }

    private OrderedSpanRecorder findRecorder(Injector injector) {
        Key<DataSender> dataSenderKey = Key.get(DataSender.class, SpanDataSender.class);
        DataSender dataSender = injector.getInstance(dataSenderKey);
        if (dataSender instanceof ListenableDataSender) {
            ListenableDataSender listenableDataSender = (ListenableDataSender) dataSender;
            ListenableDataSender.Listener listener = listenableDataSender.getListener();
            if (listener instanceof OrderedSpanRecorder) {
                return (OrderedSpanRecorder) listener;
            }
        }

        throw new IllegalStateException("unexpected datasender:" + dataSender);
    }

    private TestTcpDataSender findTestTcpDataSender(Injector injector) {
        TypeLiteral<EnhancedDataSender<Object>> dataSenderTypeLiteral = new TypeLiteral<EnhancedDataSender<Object>>() {
        };
        Key<EnhancedDataSender<Object>> dataSenderKey = Key.get(dataSenderTypeLiteral);
        EnhancedDataSender dataSender = injector.getInstance(dataSenderKey);
        if (dataSender instanceof TestTcpDataSender) {
            return (TestTcpDataSender) dataSender;
        }
        throw new IllegalStateException("unexpected dataSender" + dataSender);
    }

    private ServerMetaDataRegistryService findServerMetaDataRegistryService(Injector injector) {
        return injector.getInstance(ServerMetaDataRegistryService.class);
    }

    private AnnotationKeyRegistryService findAnnotationKeyRegistryService(Injector injector) {
        return injector.getInstance(AnnotationKeyRegistryService.class);
    }

    private ServiceTypeRegistryService findServiceTypeRegistry(Injector injector) {
        return injector.getInstance(ServiceTypeRegistryService.class);
    }

    public OrderedSpanRecorder getOrderedSpanRecorder() {
        return orderedSpanRecorder;
    }

    public TestTcpDataSender getTcpDataSender() {
        return tcpDataSender;
    }

    public ServerMetaDataRegistryService getServerMetaDataRegistryService() {
        return serverMetaDataRegistryService;
    }

    public AnnotationKeyRegistryService getAnnotationKeyRegistryService() {
        return annotationKeyRegistryService;
    }

    public ServiceTypeRegistryService getServiceTypeRegistry() {
        return serviceTypeRegistry;
    }
}
