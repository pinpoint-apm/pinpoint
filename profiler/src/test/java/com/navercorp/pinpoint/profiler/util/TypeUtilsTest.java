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

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;


/**
 * @author Woonduk Kang(emeroad)
 */
public class TypeUtilsTest {
    @Test
    public void testGetWrapperOf() {
        Assert.assertSame(TypeUtils.getWrapperOf(boolean.class), Boolean.class);
        Assert.assertSame(TypeUtils.getWrapperOf(byte.class), Byte.class);
        Assert.assertSame(TypeUtils.getWrapperOf(char.class), Character.class);
        Assert.assertSame(TypeUtils.getWrapperOf(short.class), Short.class);
        Assert.assertSame(TypeUtils.getWrapperOf(int.class), Integer.class);
        Assert.assertSame(TypeUtils.getWrapperOf(long.class), Long.class);

        Assert.assertSame(TypeUtils.getWrapperOf(float.class), Float.class);
        Assert.assertSame(TypeUtils.getWrapperOf(float.class), Float.class);
        Assert.assertSame(TypeUtils.getWrapperOf(double.class), Double.class);
        Assert.assertSame(TypeUtils.getWrapperOf(void.class), Void.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWrapperOf_unknown_type() {
        TypeUtils.getWrapperOf(this.getClass());
    }

    @Test
    public void testFindAnnotation() throws Exception {
        Method method = this.getClass().getDeclaredMethod("testFindAnnotation");
        Annotation[] annotations = method.getAnnotations();

        Test annotation = TypeUtils.findAnnotation(annotations, Test.class);
        Assert.assertEquals(annotation.annotationType(), Test.class);

    }
}