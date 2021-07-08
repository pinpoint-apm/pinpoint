package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.trace.ApiParserProvider;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.vo.callstacks.RecordFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class RecorderFactoryProvider {

    private final ServiceTypeRegistryService registry;

    private final AnnotationKeyMatcherService annotationKeyMatcherService;

    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    private final ProxyRequestTypeRegistryService proxyRequestTypeRegistryService;

    private final ApiParserProvider apiParserProvider;

    public RecorderFactoryProvider(ServiceTypeRegistryService registry,
                                   AnnotationKeyMatcherService annotationKeyMatcherService,
                                   AnnotationKeyRegistryService annotationKeyRegistryService,
                                   ProxyRequestTypeRegistryService proxyRequestTypeRegistryService,
                                   ApiParserProvider apiParserProvider) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.annotationKeyMatcherService = Objects.requireNonNull(annotationKeyMatcherService, "annotationKeyMatcherService");
        this.annotationKeyRegistryService = Objects.requireNonNull(annotationKeyRegistryService, "annotationKeyRegistryService");
        this.proxyRequestTypeRegistryService = Objects.requireNonNull(proxyRequestTypeRegistryService, "proxyRequestTypeRegistryService");
        this.apiParserProvider = Objects.requireNonNull(apiParserProvider, "apiParserRegistry");
    }

    public RecordFactory getRecordFactory()  {
        return new RecordFactory(annotationKeyMatcherService, registry, annotationKeyRegistryService, proxyRequestTypeRegistryService, apiParserProvider);
    }
}
