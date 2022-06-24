/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TypeUtilsTest {
    @Test
    public void testGetWrapperOf() {
        Assertions.assertSame(TypeUtils.getWrapperOf(boolean.class), Boolean.class);
        Assertions.assertSame(TypeUtils.getWrapperOf(byte.class), Byte.class);
        Assertions.assertSame(TypeUtils.getWrapperOf(char.class), Character.class);
        Assertions.assertSame(TypeUtils.getWrapperOf(short.class), Short.class);
        Assertions.assertSame(TypeUtils.getWrapperOf(int.class), Integer.class);
        Assertions.assertSame(TypeUtils.getWrapperOf(long.class), Long.class);

        Assertions.assertSame(TypeUtils.getWrapperOf(float.class), Float.class);
        Assertions.assertSame(TypeUtils.getWrapperOf(float.class), Float.class);
        Assertions.assertSame(TypeUtils.getWrapperOf(double.class), Double.class);
        Assertions.assertSame(TypeUtils.getWrapperOf(void.class), Void.class);
    }

    @Test
    public void testGetWrapperOf_unknown_type() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            TypeUtils.getWrapperOf(this.getClass());
        });
    }

    @Test
    public void testFindAnnotation() throws Exception {
        Method method = this.getClass().getDeclaredMethod("testFindAnnotation");
        Annotation[] annotations = method.getAnnotations();

        Test annotation = TypeUtils.findAnnotation(annotations, Test.class);
        Assertions.assertEquals(annotation.annotationType(), Test.class);

    }
}