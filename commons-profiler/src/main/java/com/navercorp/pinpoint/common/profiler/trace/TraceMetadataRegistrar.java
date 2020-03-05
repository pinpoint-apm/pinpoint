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

import com.navercorp.pinpoint.common.trace.AnnotationKeyLocator;
import com.navercorp.pinpoint.common.trace.AnnotationKeyProvider;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

import java.lang.reflect.Field;

/**
 * @author HyunGil Jeong
 */
public class TraceMetadataRegistrar {

    private static final Field serviceTypeLocatorField = getRegistryField(ServiceTypeProvider.class, "registry");
    private static final Field annotationKeyLocatorField = getRegistryField(AnnotationKeyProvider.class, "registry");

    private static Field getRegistryField(Class<?> providerClazz, String fieldName) {
        try {
            Field registryField = providerClazz.getDeclaredField(fieldName);
            registryField.setAccessible(true);
            return registryField;
        } catch (NoSuchFieldException e) {
            throw new AssertionError("Expected field '" + fieldName + "' not found in " + providerClazz.getName());
        }
    }

    private TraceMetadataRegistrar() {
        throw new AssertionError();
    }

    public static void registerServiceTypes(ServiceTypeLocator serviceTypeLocator) {
        try {
            serviceTypeLocatorField.set(null, serviceTypeLocator);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set registry for ServiceTypeProvider", e);
        }
    }

    public static void registerAnnotationKeys(AnnotationKeyLocator annotationKeyLocator) {
        try {
            annotationKeyLocatorField.set(null, annotationKeyLocator);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set registry for AnnotationKeyProvider", e);
        }
    }
}