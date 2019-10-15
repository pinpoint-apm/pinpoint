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

package com.navercorp.pinpoint.common.profiler.trace;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.DefaultServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeInfo;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author HyunGil Jeong
 */
public class TraceMetadataLoaderTest {

    private static final CommonLoggerFactory LOGGER_FACTORY = StdoutCommonLoggerFactory.INSTANCE;

    @Test
    public void staticLookUpMetadataShouldBeLoaded() {
        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(LOGGER_FACTORY);
        ServiceTypeRegistry serviceTypeRegistry = traceMetadataLoader.createServiceTypeRegistry();
        AnnotationKeyRegistry annotationKeyRegistry = traceMetadataLoader.createAnnotationKeyRegistry();
        AnnotationKeyMatcherRegistry annotationKeyMatcherRegistry = traceMetadataLoader.createAnnotationKeyMatcherRegistry();

        StaticFieldLookUp<ServiceType> serviceTypeLookup = new StaticFieldLookUp<ServiceType>(ServiceType.class, ServiceType.class);
        List<ServiceType> staticServiceTypes = serviceTypeLookup.lookup();

        StaticFieldLookUp<AnnotationKey> annotationKeyLookup = new StaticFieldLookUp<AnnotationKey>(AnnotationKey.class, AnnotationKey.class);
        List<AnnotationKey> staticAnnotationKeys = annotationKeyLookup.lookup();

        StaticFieldLookUp<DisplayArgumentMatcher> displayArgumentMatcherLookup = new StaticFieldLookUp<DisplayArgumentMatcher>(DefaultDisplayArgument.class, DisplayArgumentMatcher.class);
        List<DisplayArgumentMatcher> staticDisplayArgumentMatchers = displayArgumentMatcherLookup.lookup();

        for (ServiceType staticServiceType : staticServiceTypes) {
            verifyServiceType(serviceTypeRegistry, staticServiceType);
        }
        for (AnnotationKey staticAnnotationKey : staticAnnotationKeys) {
            verifyAnnotationKey(annotationKeyRegistry, staticAnnotationKey);
        }
        for (DisplayArgumentMatcher staticDisplayArgumentMatcher : staticDisplayArgumentMatchers) {
            ServiceType matcherServiceType = staticDisplayArgumentMatcher.getServiceType();
            AnnotationKeyMatcher annotationKeyMatcher = staticDisplayArgumentMatcher.getAnnotationKeyMatcher();
            verifyAnnotationKeyMatcher(annotationKeyMatcherRegistry, matcherServiceType, annotationKeyMatcher);
        }
    }

    @Test
    public void registeredMetadataShouldBeLoaded() {
        final ServiceType plugin1ServiceType = ServiceTypeFactory.of(999, "TEST_SERVICE_TYPE_1");
        final AnnotationKey plugin1AnnotationKey = AnnotationKeyFactory.of(Integer.MAX_VALUE, "plugin1Annotation");
        final TraceMetadataProvider traceMetadataProvider1 = createTraceMetadataProvider(new DefaultServiceTypeInfo(plugin1ServiceType), plugin1AnnotationKey);

        final ServiceType plugin2ServiceType = ServiceTypeFactory.of(998, "TEST_SERVICE_TYPE_2", "TEST_SERVICE_TYPE_2_DESC", ServiceTypeProperty.RECORD_STATISTICS);
        final AnnotationKeyMatcher plugin2AnnotationKeyMatcher = AnnotationKeyMatchers.exact(Integer.MAX_VALUE - 1);
        final AnnotationKey plugin2AnnotationKey = AnnotationKeyFactory.of(Integer.MAX_VALUE - 1, "plugin2Annotation");
        final TraceMetadataProvider traceMetadataProvider2 = createTraceMetadataProvider(new DefaultServiceTypeInfo(plugin2ServiceType, plugin2AnnotationKeyMatcher), plugin2AnnotationKey);

        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(LOGGER_FACTORY);
        traceMetadataLoader.load(Arrays.asList(traceMetadataProvider1, traceMetadataProvider2));

        ServiceTypeRegistry serviceTypeRegistry = traceMetadataLoader.createServiceTypeRegistry();
        AnnotationKeyRegistry annotationKeyRegistry = traceMetadataLoader.createAnnotationKeyRegistry();
        AnnotationKeyMatcherRegistry annotationKeyMatcherRegistry = traceMetadataLoader.createAnnotationKeyMatcherRegistry();

        verifyServiceType(serviceTypeRegistry, plugin1ServiceType);
        verifyAnnotationKey(annotationKeyRegistry, plugin1AnnotationKey);

        verifyServiceType(serviceTypeRegistry, plugin2ServiceType);
        verifyAnnotationKey(annotationKeyRegistry, plugin2AnnotationKey);
        verifyAnnotationKeyMatcher(annotationKeyMatcherRegistry, plugin2ServiceType, plugin2AnnotationKeyMatcher);
    }

    @Test
    public void unregisteredServiceTypeTest() {
        final ServiceType registeredServiceType = ServiceTypeFactory.of(999, "REGISTERED_SERVICE_TYPE", ServiceTypeProperty.RECORD_STATISTICS);
        final ServiceType unregisteredServiceType = ServiceTypeFactory.of(998, "UNREGISTERED_SERVICE_TYPE", ServiceTypeProperty.RECORD_STATISTICS);

        final TraceMetadataProvider traceMetadataProvider = createTraceMetadataProvider(new DefaultServiceTypeInfo(registeredServiceType));

        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(LOGGER_FACTORY);
        traceMetadataLoader.load(Collections.singletonList(traceMetadataProvider));

        ServiceTypeRegistry serviceTypeRegistry = traceMetadataLoader.createServiceTypeRegistry();
        verifyServiceType(serviceTypeRegistry, registeredServiceType);

        Assert.assertSame(ServiceType.UNDEFINED, serviceTypeRegistry.findServiceType(unregisteredServiceType.getCode()));
        Assert.assertSame(ServiceType.UNDEFINED, serviceTypeRegistry.findServiceTypeByName(unregisteredServiceType.getName()));
    }

    @Test
    public void unregisteredAnnotationKeyTest() {
        final AnnotationKey registeredAnnotationKey = AnnotationKeyFactory.of(Integer.MAX_VALUE, "registeredAnnotationKey");
        final AnnotationKey unregisteredAnnotationKey = AnnotationKeyFactory.of(Integer.MAX_VALUE - 1, "unregisteredAnnotationKey");

        final TraceMetadataProvider traceMetadataProvider = createTraceMetadataProvider(registeredAnnotationKey);

        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(LOGGER_FACTORY);
        traceMetadataLoader.load(Collections.singletonList(traceMetadataProvider));

        AnnotationKeyRegistry annotationKeyRegistry = traceMetadataLoader.createAnnotationKeyRegistry();
        verifyAnnotationKey(annotationKeyRegistry, registeredAnnotationKey);

        Assert.assertSame(AnnotationKey.UNKNOWN, annotationKeyRegistry.findAnnotationKey(unregisteredAnnotationKey.getCode()));
        try {
            annotationKeyRegistry.findAnnotationKeyByName(unregisteredAnnotationKey.getName());
            Assert.fail();
        } catch (NoSuchElementException expected) {}
    }

    @Test
    public void unregisteredAnnotationKeyMatcherTest() {
        final ServiceType registeredServiceType = ServiceTypeFactory.of(999, "REGISTERED_SERVICE_TYPE", ServiceTypeProperty.RECORD_STATISTICS);
        final AnnotationKeyMatcher registeredAnnotationKeyMatcher = AnnotationKeyMatchers.ARGS_MATCHER;
        final ServiceType unregisteredServiceType = ServiceTypeFactory.of(998, "UNREGISTERED_SERVICE_TYPE");

        final TraceMetadataProvider traceMetadataProvider = createTraceMetadataProvider(new DefaultServiceTypeInfo(registeredServiceType, registeredAnnotationKeyMatcher));

        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(LOGGER_FACTORY);
        traceMetadataLoader.load(Collections.singletonList(traceMetadataProvider));

        AnnotationKeyMatcherRegistry annotationKeyMatcherRegistry = traceMetadataLoader.createAnnotationKeyMatcherRegistry();
        verifyAnnotationKeyMatcher(annotationKeyMatcherRegistry, registeredServiceType, registeredAnnotationKeyMatcher);

        Assert.assertNull(annotationKeyMatcherRegistry.findAnnotationKeyMatcher(unregisteredServiceType.getCode()));
    }

    private void verifyServiceType(ServiceTypeRegistry serviceTypeRegistry, ServiceType serviceType) {
        Assert.assertSame(serviceType, serviceTypeRegistry.findServiceType(serviceType.getCode()));
        Assert.assertSame(serviceType, serviceTypeRegistry.findServiceTypeByName(serviceType.getName()));
        if (serviceType.isRecordStatistics()) {
            boolean found = false;
            List<ServiceType> descMatchedServiceTypes = serviceTypeRegistry.findDesc(serviceType.getDesc());
            for (ServiceType descMatchedServiceType : descMatchedServiceTypes) {
                if (serviceType.getDesc().equals(descMatchedServiceType.getDesc())) {
                    found = true;
                }
            }
            Assert.assertTrue(found);
            try {
                descMatchedServiceTypes.add(serviceType);
                Assert.fail("Adding to unmodifiable list should have failed");
            } catch (Exception expected) {
            }
        }
    }

    private void verifyAnnotationKey(AnnotationKeyRegistry annotationKeyRegistry, AnnotationKey annotationKey) {
        Assert.assertSame(annotationKey, annotationKeyRegistry.findAnnotationKey(annotationKey.getCode()));
        Assert.assertSame(annotationKey, annotationKeyRegistry.findAnnotationKeyByName(annotationKey.getName()));
    }

    private void verifyAnnotationKeyMatcher(AnnotationKeyMatcherRegistry annotationKeyMatcherRegistry, ServiceType serviceType, AnnotationKeyMatcher annotationKeyMatcher) {
        Assert.assertSame(annotationKeyMatcher, annotationKeyMatcherRegistry.findAnnotationKeyMatcher(serviceType.getCode()));
    }

    private TraceMetadataProvider createTraceMetadataProvider(ServiceTypeInfo serviceTypeInfo) {
        return createTraceMetadataProvider(serviceTypeInfo, null);
    }

    private TraceMetadataProvider createTraceMetadataProvider(AnnotationKey annotationKey) {
        return createTraceMetadataProvider(null, annotationKey);
    }

    private TraceMetadataProvider createTraceMetadataProvider(final ServiceTypeInfo serviceTypeInfo,
                                                              final AnnotationKey annotationKey) {
        return new TraceMetadataProvider() {
            @Override
            public void setup(TraceMetadataSetupContext context) {
                addServiceTypeInfo(context, serviceTypeInfo);
                addAnnotationKey(context, annotationKey);
            }
        };
    }

    private void addServiceTypeInfo(TraceMetadataSetupContext context, ServiceTypeInfo serviceTypeInfo) {
        if (serviceTypeInfo == null) {
            return;
        }
        ServiceType serviceType = serviceTypeInfo.getServiceType();
        AnnotationKeyMatcher annotationKeyMatcher = serviceTypeInfo.getPrimaryAnnotationKeyMatcher();
        if (annotationKeyMatcher == null) {
            context.addServiceType(serviceType);
        } else {
            context.addServiceType(serviceType, annotationKeyMatcher);
        }
    }

    private void addAnnotationKey(TraceMetadataSetupContext context, AnnotationKey annotationKey) {
        if (annotationKey == null) {
            return;
        }
        context.addAnnotationKey(annotationKey);
    }
}
