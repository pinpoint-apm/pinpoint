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
import com.navercorp.pinpoint.common.trace.AnnotationKeyLocator;
import com.navercorp.pinpoint.common.trace.AnnotationKeyProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
public class TraceMetadataRegistrarTest {

    @Test
    public void undefinedServiceTypeShouldThrowException() {
        final ServiceType unknownServiceType = ServiceType.UNDEFINED;
        ServiceTypeLocator serviceTypeRegistry = mock(ServiceTypeLocator.class);
        when(serviceTypeRegistry.findServiceType(unknownServiceType.getCode())).thenReturn(unknownServiceType);
        when(serviceTypeRegistry.findServiceTypeByName(unknownServiceType.getName())).thenReturn(unknownServiceType);

        TraceMetadataRegistrar.registerServiceTypes(serviceTypeRegistry);

        Assertions.assertThrowsExactly(IllegalStateException.class, () -> {
            ServiceTypeProvider.getByCode(unknownServiceType.getCode());
        }, "Retrieving UNDEFINED ServiceType by code should throw IllegalStateException");
        Assertions.assertThrowsExactly(IllegalStateException.class, () -> {
            ServiceTypeProvider.getByName(unknownServiceType.getName());
        }, "Retrieving UNDEFINED ServiceType by name should throw IllegalStateException");
    }

    @Test
    public void registeredServiceTypes() {
        final ServiceType plugin1ServiceType = ServiceTypeFactory.of(999, "TEST_SERVICE_TYPE_1");
        final ServiceType plugin2ServiceType = ServiceTypeFactory.of(998, "TEST_SERVICE_TYPE_2");
        ServiceTypeLocator serviceTypeRegistry = mock(ServiceTypeLocator.class);
        when(serviceTypeRegistry.findServiceType(plugin1ServiceType.getCode())).thenReturn(plugin1ServiceType);
        when(serviceTypeRegistry.findServiceTypeByName(plugin1ServiceType.getName())).thenReturn(plugin1ServiceType);
        when(serviceTypeRegistry.findServiceType(plugin2ServiceType.getCode())).thenReturn(plugin2ServiceType);
        when(serviceTypeRegistry.findServiceTypeByName(plugin2ServiceType.getName())).thenReturn(plugin2ServiceType);

        TraceMetadataRegistrar.registerServiceTypes(serviceTypeRegistry);
        Assertions.assertSame(plugin1ServiceType, ServiceTypeProvider.getByCode(plugin1ServiceType.getCode()));
        Assertions.assertSame(plugin1ServiceType, ServiceTypeProvider.getByName(plugin1ServiceType.getName()));
        Assertions.assertSame(plugin2ServiceType, ServiceTypeProvider.getByCode(plugin2ServiceType.getCode()));
        Assertions.assertSame(plugin2ServiceType, ServiceTypeProvider.getByName(plugin2ServiceType.getName()));
    }

    @Test
    public void unknownAnnotationKeyShouldThrowException() {
        final AnnotationKey unknownAnnotationKey = AnnotationKey.UNKNOWN;
        AnnotationKeyLocator annotationKeyRegistry = mock(AnnotationKeyLocator.class);
        when(annotationKeyRegistry.findAnnotationKey(unknownAnnotationKey.getCode())).thenReturn(unknownAnnotationKey);

        TraceMetadataRegistrar.registerAnnotationKeys(annotationKeyRegistry);

        Assertions.assertThrowsExactly(IllegalStateException.class, () -> {
            AnnotationKeyProvider.getByCode(unknownAnnotationKey.getCode());
        }, "Retrieving UNKNOWN AnnotationKey should throw IllegalStateException");
    }

    @Test
    public void registeredAnnotationKey() {
        final AnnotationKey annotationKey1 = AnnotationKey.API;
        final AnnotationKey annotationKey2 = AnnotationKey.SQL;
        AnnotationKeyLocator annotationKeyRegistry = mock(AnnotationKeyLocator.class);
        when(annotationKeyRegistry.findAnnotationKey(annotationKey1.getCode())).thenReturn(annotationKey1);
        when(annotationKeyRegistry.findAnnotationKey(annotationKey2.getCode())).thenReturn(annotationKey2);

        TraceMetadataRegistrar.registerAnnotationKeys(annotationKeyRegistry);
        Assertions.assertSame(annotationKey1, AnnotationKeyProvider.getByCode(annotationKey1.getCode()));
        Assertions.assertSame(annotationKey2, AnnotationKeyProvider.getByCode(annotationKey2.getCode()));
    }
}
