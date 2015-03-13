/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.common;

import static com.navercorp.pinpoint.common.ServiceTypeProperty.*;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.common.service.*;
import com.navercorp.pinpoint.common.util.StaticFieldLookUp;
import org.junit.Test;

import com.navercorp.pinpoint.common.plugin.TypeProvider;
import com.navercorp.pinpoint.common.plugin.TypeSetupContext;

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public class ServiceTypeInitializerTest {
    private static final ServiceType[] TEST_TYPES = {
        ServiceType.of(1209, "FOR_UNIT_TEST", "UNDEFINED", HistogramSchema.NORMAL_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID)
    };
    
    private static final AnnotationKey[] TEST_KEYS = {
        new AnnotationKey(1209, "Duplicate-API")
    };

    private static final ServiceType[] DUPLICATED_CODE_WITH_DEFAULT_TYPE = {
        ServiceType.of(ServiceType.USER.getCode(), "FOR_UNIT_TEST", "UNDEFINED", HistogramSchema.NORMAL_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID)
    };
    
    private static final ServiceType[] DUPLICATED_NAME_WITH_DEFAULT_TYPE = {
        ServiceType.of(1209, ServiceType.USER.getName(), "UNDEFINED", HistogramSchema.NORMAL_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID)
    };
    
    private static final AnnotationKey[] DUPLICATED_CODE_WITH_DEFAULT_KEY = {
        new AnnotationKey(AnnotationKey.ARGS0.getCode(), "API")
    };

    private void verifyAnnotationKeys(List<AnnotationKey> annotationKeys, AnnotationKeyRegistryService annotationKeyRegistryService) {
        for (AnnotationKey key : annotationKeys) {
            assertSame(key, annotationKeyRegistryService.findAnnotationKey(key.getCode()));
        }
    }


    @Test
    public void testWithPlugins() {

        List<TypeProvider> typeProviders = Arrays.<TypeProvider>asList(new TestProvider(TEST_TYPES, TEST_KEYS));
        TypeLoaderService typeLoaderService = new DefaultTypeLoaderService(typeProviders);
        AnnotationKeyRegistryService annotationKeyRegistryService = new DefaultAnnotationKeyRegistryService(typeLoaderService);

        StaticFieldLookUp<AnnotationKey> lookUp = new StaticFieldLookUp<AnnotationKey>(AnnotationKey.class, AnnotationKey.class);
        verifyAnnotationKeys(lookUp.lookup(), annotationKeyRegistryService);


        verifyAnnotationKeys(Arrays.asList(TEST_KEYS), annotationKeyRegistryService);
    }
    
    @Test(expected=RuntimeException.class)
    public void testDuplicated() {

        List<TypeProvider> providers = Arrays.<TypeProvider>asList(
                new TestProvider(TEST_TYPES, TEST_KEYS),
                new TestProvider(new ServiceType[0], TEST_KEYS)
        );

        TypeProviderLoader loader = new TypeProviderLoader();
        loader.load(providers);
    }
    
    @Test(expected=RuntimeException.class)
    public void testDuplicated2() {
        List<TypeProvider> providers = Arrays.<TypeProvider>asList(
                new TestProvider(TEST_TYPES, TEST_KEYS),
                new TestProvider(TEST_TYPES, new AnnotationKey[0])
        );

        TypeProviderLoader loader = new TypeProviderLoader();
        loader.load(providers);
    }
    
    @Test(expected=RuntimeException.class)
    public void testDuplicated3() {
        List<TypeProvider> providers = Arrays.<TypeProvider>asList(
                new TestProvider(TEST_TYPES, TEST_KEYS),
                new TestProvider(TEST_TYPES, new AnnotationKey[0])
        );

        TypeProviderLoader loader = new TypeProviderLoader();
        loader.load(providers);
    }

    @Test(expected=RuntimeException.class)
    public void testDuplicatedWithDefault() {
        List<TypeProvider> providers = Arrays.<TypeProvider>asList(
                new TestProvider(DUPLICATED_CODE_WITH_DEFAULT_TYPE, TEST_KEYS)
        );

        TypeLoaderService loaderService = new DefaultTypeLoaderService(providers);
        ServiceTypeRegistryService serviceTypeRegistryService = new DefaultServiceTypeRegistryService(loaderService);
    }

    @Test(expected=RuntimeException.class)
    public void testDuplicatedWithDefault2() {
        List<TypeProvider> providers = Arrays.<TypeProvider>asList(
                new TestProvider(DUPLICATED_NAME_WITH_DEFAULT_TYPE, TEST_KEYS)
        );

        TypeLoaderService loaderService = new DefaultTypeLoaderService(providers);
        ServiceTypeRegistryService serviceTypeRegistryService = new DefaultServiceTypeRegistryService(loaderService);
    }

    @Test(expected=RuntimeException.class)
    public void testDuplicatedWithDefault3() {
        List<TypeProvider> providers = Arrays.<TypeProvider>asList(
                new TestProvider(TEST_TYPES, DUPLICATED_CODE_WITH_DEFAULT_KEY)
        );

        TypeLoaderService loaderService = new DefaultTypeLoaderService(providers);
        AnnotationKeyRegistryService annotationKeyRegistryService = new DefaultAnnotationKeyRegistryService(loaderService);

    }
    
    
    private static class TestProvider implements TypeProvider {
        private final ServiceType[] serviceTypes;
        private final AnnotationKey[] annotationKeys;
        
        public TestProvider(ServiceType[] serviceTypes, AnnotationKey[] annotationKeys) {
            this.serviceTypes = serviceTypes;
            this.annotationKeys = annotationKeys;
        }
        
        @Override
        public void setUp(TypeSetupContext context) {
            for (ServiceType type : serviceTypes) {
                context.addType(type);
            }

            for (AnnotationKey key : annotationKeys) {
                context.addAnnotationKey(key);
            }
        }
    }
}
