/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.test;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.SpanDataSender;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ApplicationContextHandler {

    private final OrderedSpanRecorder orderedSpanRecorder;
    private final TestDataSender testDataSender;
    private final ServerMetaDataRegistryService serverMetaDataRegistryService;
    private final AnnotationKeyRegistryService annotationKeyRegistryService;
    private final ServiceTypeRegistryService serviceTypeRegistry;

    public ApplicationContextHandler(DefaultApplicationContext defaultApplicationContext) {
        Injector injector = defaultApplicationContext.getInjector();
        this.orderedSpanRecorder = findRecorder(injector);
        this.testDataSender = findTestTcpDataSender(injector);
        this.serverMetaDataRegistryService = findServerMetaDataRegistryService(injector);
        this.annotationKeyRegistryService = findAnnotationKeyRegistryService(injector);
        this.serviceTypeRegistry = findServiceTypeRegistry(injector);
    }

    private OrderedSpanRecorder findRecorder(Injector injector) {
        TypeLiteral<DataSender<SpanType>> spanDataSenderType = new TypeLiteral<DataSender<SpanType>>() {};
        Key<DataSender<SpanType>> spanDataSenderKey = Key.get(spanDataSenderType, SpanDataSender.class);
        DataSender<SpanType> dataSender = injector.getInstance(spanDataSenderKey);
        if (dataSender instanceof ListenableDataSender) {
            ListenableDataSender<SpanType> listenableDataSender = (ListenableDataSender<SpanType>) dataSender;
            ListenableDataSender.Listener<SpanType> listener = listenableDataSender.getListener();
            if (listener instanceof OrderedSpanRecorder) {
                return (OrderedSpanRecorder) listener;
            }
        }

        throw new IllegalStateException("unexpected dataSender:" + dataSender);
    }

    private TestDataSender findTestTcpDataSender(Injector injector) {
        TypeLiteral<EnhancedDataSender<MetaDataType>> dataSenderTypeLiteral = new TypeLiteral<EnhancedDataSender<MetaDataType>>() {
        };
        Key<EnhancedDataSender<MetaDataType>> dataSenderKey = Key.get(dataSenderTypeLiteral);
        EnhancedDataSender<MetaDataType> dataSender = injector.getInstance(dataSenderKey);
        if (dataSender instanceof TestDataSender) {
            return (TestDataSender) dataSender;
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


    public List<String> getExecutedMethod() {
        List<String> list = new ArrayList<>();
        for (SpanType item : orderedSpanRecorder) {
            if (item instanceof Span) {
                Span span = (Span) item;
                List<SpanEvent> spanEventList = span.getSpanEventList();
                addApiDescription(list, spanEventList);
            } else if (item instanceof SpanChunk) {
                SpanChunk spanChunk = (SpanChunk) item;
                List<SpanEvent> spanEventList = spanChunk.getSpanEventList();
                addApiDescription(list, spanEventList);
            }
        }
        return list;
    }

    private void addApiDescription(List<String> list, List<SpanEvent> spanEventList) {
        for (SpanEvent spanEvent : spanEventList) {
            int apiId = spanEvent.getApiId();
            String apiDescription = this.testDataSender.getApiDescription(apiId);
            list.add(apiDescription);
        }
    }


    public OrderedSpanRecorder getOrderedSpanRecorder() {
        return orderedSpanRecorder;
    }

    public TestDataSender getTestDataSender() {
        return testDataSender;
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
