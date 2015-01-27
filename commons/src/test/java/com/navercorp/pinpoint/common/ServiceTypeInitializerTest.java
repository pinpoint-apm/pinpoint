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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.navercorp.pinpoint.common.plugin.ServiceTypeProvider;

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public class ServiceTypeInitializerTest {
    private static final ServiceType[] TEST_TYPES = {
        ServiceType.of(1209, "FOR_UNIT_TEST", "UNDEFINED", HistogramSchema.NORMAL_SCHEMA, TERMINAL, RECORD_STATISTICS, INCLUDE_DESTINATION_ID)
    };
    
    private static final AnnotationKey[] TEST_KEYS = {
        new AnnotationKey(1209, "API")
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
    
    private static Field serviceTypeValues;
    private static Field annotationKeyValues;
    
    @BeforeClass
    public static void init() throws Exception {
        serviceTypeValues = ServiceType.class.getDeclaredField("VALUES");
        serviceTypeValues.setAccessible(true);
        
        annotationKeyValues = AnnotationKey.class.getDeclaredField("VALUES");
        annotationKeyValues.setAccessible(true);
    }

    @Before
    public void reset() throws Exception {
        serviceTypeValues.set(null, null);
        annotationKeyValues.set(null, null);
    }
    
    @Test
    public void testDefaults() {
        ServiceTypeInitializer.initialize();
        verifyServiceTypes(ServiceType.DEFAULT_VALUES);
        verifyAnnotationKeys(AnnotationKey.DEFAULT_VALUES);
    }

    private void verifyAnnotationKeys(AnnotationKey... annotationKeys) {
        for (AnnotationKey key : annotationKeys) {
            assertSame(key, AnnotationKey.findAnnotationKey(key.getCode()));
        }
    }

    private void verifyServiceTypes(ServiceType... serviceTypes) {
        for (ServiceType type : serviceTypes) {
            assertSame(type, ServiceType.findServiceType(type.getCode()));
            
            if (type.isRecordStatistics()) {
                assertTrue(ServiceType.findDesc(type.getDesc()).contains(type));
            }
        }
    }

    @Test
    public void testWithPlugins() {
        ServiceTypeInitializer.initialize(Arrays.<ServiceTypeProvider>asList(new TestProvider(TEST_TYPES, TEST_KEYS)));
        
        verifyServiceTypes(ServiceType.DEFAULT_VALUES);
        verifyAnnotationKeys(AnnotationKey.DEFAULT_VALUES);
        
        verifyServiceTypes(TEST_TYPES);
        verifyAnnotationKeys(TEST_KEYS);
    }
    
    @Test(expected=RuntimeException.class)
    public void testDuplicated() {
        ServiceTypeInitializer.initialize(Arrays.<ServiceTypeProvider>asList(
                new TestProvider(TEST_TYPES, TEST_KEYS),
                new TestProvider(new ServiceType[0], TEST_KEYS)
        ));
    }
    
    @Test(expected=RuntimeException.class)
    public void testDuplicated2() {
        ServiceTypeInitializer.initialize(Arrays.<ServiceTypeProvider>asList(
                new TestProvider(TEST_TYPES, TEST_KEYS),
                new TestProvider(TEST_TYPES, new AnnotationKey[0])
        ));
    }
    
    @Test(expected=RuntimeException.class)
    public void testDuplicated3() {
        ServiceTypeInitializer.initialize(Arrays.<ServiceTypeProvider>asList(
                new TestProvider(TEST_TYPES, TEST_KEYS),
                new TestProvider(TEST_TYPES, new AnnotationKey[0])
        ));
    }

    @Test(expected=RuntimeException.class)
    public void testDuplicatedWithDefault() {
        ServiceTypeInitializer.initialize(Arrays.<ServiceTypeProvider>asList(
                new TestProvider(DUPLICATED_CODE_WITH_DEFAULT_TYPE, TEST_KEYS)
        ));
    }

    @Test(expected=RuntimeException.class)
    public void testDuplicatedWithDefault2() {
        ServiceTypeInitializer.initialize(Arrays.<ServiceTypeProvider>asList(
                new TestProvider(DUPLICATED_NAME_WITH_DEFAULT_TYPE, TEST_KEYS)
        ));
    }

    @Test(expected=RuntimeException.class)
    public void testDuplicatedWithDefault3() {
        ServiceTypeInitializer.initialize(Arrays.<ServiceTypeProvider>asList(
                new TestProvider(TEST_TYPES, DUPLICATED_CODE_WITH_DEFAULT_KEY)
        ));
    }
    
    
    private static class TestProvider implements ServiceTypeProvider {
        private final ServiceType[] serviceTypes;
        private final AnnotationKey[] annotationKeys;
        
        public TestProvider(ServiceType[] serviceTypes, AnnotationKey[] annotationKeys) {
            this.serviceTypes = serviceTypes;
            this.annotationKeys = annotationKeys;
        }

        @Override
        public ServiceType[] getServiceTypes() {
            return serviceTypes;
        }

        @Override
        public AnnotationKey[] getAnnotationKeys() {
            return annotationKeys;
        }
    }
}
