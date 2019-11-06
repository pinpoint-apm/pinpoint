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

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKeyLocator;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;
import org.junit.Test;

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public class ServiceTypeInitializerTest {

    private CommonLoggerFactory loggerFactory = StdoutCommonLoggerFactory.INSTANCE;

    private static final ServiceType[] TEST_TYPES = {
        ServiceTypeFactory.of(1209, "FOR_UNIT_TEST", "UNDEFINED", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID)
    };
    
    private static final AnnotationKey[] TEST_KEYS = {
        AnnotationKeyFactory.of(1209, "Duplicate-API")
    };

    private static final ServiceType[] DUPLICATED_CODE_WITH_DEFAULT_TYPE = {
        ServiceTypeFactory.of(ServiceType.USER.getCode(), "FOR_UNIT_TEST", "UNDEFINED", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID)
    };
    
    private static final ServiceType[] DUPLICATED_NAME_WITH_DEFAULT_TYPE = {
        ServiceTypeFactory.of(1209, ServiceType.USER.getName(), "UNDEFINED", TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID)
    };
    
    private static final AnnotationKey[] DUPLICATED_CODE_WITH_DEFAULT_KEY = {
        AnnotationKeyFactory.of(AnnotationKey.ARGS0.getCode(), "API")
    };

    private void verifyAnnotationKeys(List<AnnotationKey> annotationKeys, AnnotationKeyLocator annotationKeyRegistry) {
        for (AnnotationKey key : annotationKeys) {
            assertSame(key, annotationKeyRegistry.findAnnotationKey(key.getCode()));
        }
    }

    @Test
    public void testWithPlugins() {
        List<TraceMetadataProvider> typeProviders = Arrays.<TraceMetadataProvider>asList(new TestProvider(TEST_TYPES, TEST_KEYS));
        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(loggerFactory);
        traceMetadataLoader.load(typeProviders);

        AnnotationKeyLocator annotationKeyRegistry = traceMetadataLoader.createAnnotationKeyRegistry();

        StaticFieldLookUp<AnnotationKey> lookUp = new StaticFieldLookUp<AnnotationKey>(AnnotationKey.class, AnnotationKey.class);
        verifyAnnotationKeys(lookUp.lookup(), annotationKeyRegistry);


        verifyAnnotationKeys(Arrays.asList(TEST_KEYS), annotationKeyRegistry);
    }
    
    @Test(expected=RuntimeException.class)
    public void testDuplicated() {

        List<TraceMetadataProvider> providers = Arrays.<TraceMetadataProvider>asList(
                new TestProvider(TEST_TYPES, TEST_KEYS),
                new TestProvider(new ServiceType[0], TEST_KEYS)
        );

        TraceMetadataLoader loader = new TraceMetadataLoader(StdoutCommonLoggerFactory.INSTANCE);
        loader.load(providers);
    }
    
    @Test(expected=RuntimeException.class)
    public void testDuplicated2() {
        List<TraceMetadataProvider> providers = Arrays.<TraceMetadataProvider>asList(
                new TestProvider(TEST_TYPES, TEST_KEYS),
                new TestProvider(TEST_TYPES, new AnnotationKey[0])
        );

        TraceMetadataLoader loader = new TraceMetadataLoader(StdoutCommonLoggerFactory.INSTANCE);
        loader.load(providers);
    }
    
    @Test(expected=RuntimeException.class)
    public void testDuplicated3() {
        List<TraceMetadataProvider> providers = Arrays.<TraceMetadataProvider>asList(
                new TestProvider(TEST_TYPES, TEST_KEYS),
                new TestProvider(TEST_TYPES, new AnnotationKey[0])
        );

        TraceMetadataLoader loader = new TraceMetadataLoader(StdoutCommonLoggerFactory.INSTANCE);
        loader.load(providers);
    }

    @Test(expected=RuntimeException.class)
    public void testDuplicatedWithDefault() {
        List<TraceMetadataProvider> providers = Arrays.<TraceMetadataProvider>asList(
                new TestProvider(DUPLICATED_CODE_WITH_DEFAULT_TYPE, TEST_KEYS)
        );

        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(loggerFactory);
        traceMetadataLoader.load(providers);
        ServiceTypeLocator unused = traceMetadataLoader.createServiceTypeRegistry();
    }

    @Test(expected=RuntimeException.class)
    public void testDuplicatedWithDefault2() {
        List<TraceMetadataProvider> providers = Arrays.<TraceMetadataProvider>asList(
                new TestProvider(DUPLICATED_NAME_WITH_DEFAULT_TYPE, TEST_KEYS)
        );

        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(loggerFactory);
        traceMetadataLoader.load(providers);
        ServiceTypeLocator unused = traceMetadataLoader.createServiceTypeRegistry();
    }

    @Test(expected=RuntimeException.class)
    public void testDuplicatedWithDefault3() {
        List<TraceMetadataProvider> providers = Arrays.<TraceMetadataProvider>asList(
                new TestProvider(TEST_TYPES, DUPLICATED_CODE_WITH_DEFAULT_KEY)
        );

        TraceMetadataLoader traceMetadataLoader = new TraceMetadataLoader(loggerFactory);
        traceMetadataLoader.load(providers);
        AnnotationKeyLocator unused = traceMetadataLoader.createAnnotationKeyRegistry();
    }

    private static class TestProvider implements TraceMetadataProvider {
        private final ServiceType[] serviceTypes;
        private final AnnotationKey[] annotationKeys;
        
        public TestProvider(ServiceType[] serviceTypes, AnnotationKey[] annotationKeys) {
            this.serviceTypes = serviceTypes;
            this.annotationKeys = annotationKeys;
        }
        
        @Override
        public void setup(TraceMetadataSetupContext context) {
            for (ServiceType type : serviceTypes) {
                context.addServiceType(type);
            }

            for (AnnotationKey key : annotationKeys) {
                context.addAnnotationKey(key);
            }
        }
    }
}
