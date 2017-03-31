/*
 * Copyright 2017 NAVER Corp.
 *
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
package com.navercorp.pinpoint.profiler.instrument.classreading;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class DefaultInternalClassMetadataTest {

    @Test
    public void base() throws Exception {
        DefaultInternalClassMetadata classMetadata = new DefaultInternalClassMetadata("java/lang/String", "java/lang/Object", Arrays.asList("java/lang/Comparable", "java/lang/Serializable"), Collections.EMPTY_LIST, false, false, false, false);
        // name
        assertEquals("java/lang/String", classMetadata.getClassInternalName());
        // super
        assertEquals("java/lang/Object", classMetadata.getSuperClassInternalName());
        // interfaces
        assertTrue(classMetadata.getInterfaceInternalNames().contains("java/lang/Comparable"));
        assertTrue(classMetadata.getInterfaceInternalNames().contains("java/lang/Serializable"));
        // annotation
        assertEquals(0, classMetadata.getAnnotationInternalNames().size());
    }

    @Test
    public void interfaceNamesNull() throws Exception {
        DefaultInternalClassMetadata classMetadata = new DefaultInternalClassMetadata("java/lang/String", "java/lang/Object", null, Collections.EMPTY_LIST, false, false, false, false);
        assertEquals(0, classMetadata.getInterfaceInternalNames().size());
    }

    @Test
    public void annotationNamesNull() throws Exception {
        DefaultInternalClassMetadata classMetadata = new DefaultInternalClassMetadata("java/lang/String", "java/lang/Object", Arrays.asList("java/lang/Comparable", "java/lang/Serializable"), null, false, false, false, false);
        assertEquals(0, classMetadata.getAnnotationInternalNames().size());
    }
}