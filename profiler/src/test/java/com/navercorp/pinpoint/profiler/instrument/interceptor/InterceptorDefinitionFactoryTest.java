/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
public class InterceptorDefinitionFactoryTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Test
    public void testGetInterceptorType_BasicType() throws Exception {
        InterceptorDefinitionFactory typeDetector = new InterceptorDefinitionFactory();

        Assert.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor.class).getCaptureType(), CaptureType.AROUND);

        Assert.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor0.class).getCaptureType(), CaptureType.AROUND);
        Assert.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor1.class).getCaptureType(), CaptureType.AROUND);
        Assert.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor2.class).getCaptureType(), CaptureType.AROUND);
        Assert.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor3.class).getCaptureType(), CaptureType.AROUND);
        Assert.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor4.class).getCaptureType(), CaptureType.AROUND);
        Assert.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor5.class).getCaptureType(), CaptureType.AROUND);

        Assert.assertSame(typeDetector.createInterceptorDefinition(StaticAroundInterceptor.class).getCaptureType(), CaptureType.AROUND);

        Assert.assertSame(typeDetector.createInterceptorDefinition(ApiIdAwareAroundInterceptor.class).getCaptureType(), CaptureType.AROUND);
    }


    @Test
    public void testGetInterceptorType_Inherited() throws Exception {
        InterceptorDefinitionFactory typeDetector = new InterceptorDefinitionFactory();

        Assert.assertSame(typeDetector.createInterceptorDefinition(InheritedAroundInterceptor.class).getCaptureType(), CaptureType.AROUND);
    }

    @Test
    public void testDeclaredMethods() {

        Class<String> stringClass = String.class;
        for (Method method : stringClass.getDeclaredMethods()) {
            logger.debug("{}", method);
        }

    }


    @Test(expected = RuntimeException.class)
    public void testGetType_Error() throws Exception {
        InterceptorDefinitionFactory typeDetector = new InterceptorDefinitionFactory();
        typeDetector.createInterceptorDefinition(Interceptor.class);
    }


    @Test
    public void testGetInterceptorCaptureType() throws Exception {
        InterceptorDefinitionFactory interceptorDefinitionFactory = new InterceptorDefinitionFactory();

        final InterceptorDefinition before = interceptorDefinitionFactory.createInterceptorDefinition(TestBeforeInterceptor.class);
        assertInterceptorType(before, CaptureType.BEFORE, "before", null);

        final InterceptorDefinition after = interceptorDefinitionFactory.createInterceptorDefinition(TestAfterInterceptor.class);
        assertInterceptorType(after, CaptureType.AFTER, null, "after");

        final InterceptorDefinition around = interceptorDefinitionFactory.createInterceptorDefinition(TestAroundInterceptor.class);
        assertInterceptorType(around, CaptureType.AROUND, "before", "after");

        final InterceptorDefinition ignore = interceptorDefinitionFactory.createInterceptorDefinition(TestIgnoreInterceptor.class);
        assertInterceptorType(ignore, CaptureType.NON, null, null);
    }

    private void assertInterceptorType(InterceptorDefinition interceptor, CaptureType aroundType, String beforeName, String afterName) {
        Assert.assertSame("Type", aroundType, aroundType);

        if (beforeName == null) {
            Assert.assertNull("before is null", interceptor.getBeforeMethod());
        } else {
            Assert.assertNotNull("after is not null", interceptor.getBeforeMethod());
            Assert.assertEquals("check beforeName", interceptor.getBeforeMethod().getName(), beforeName);
        }

        if (afterName == null) {
            Assert.assertNull("after is null", interceptor.getAfterMethod());
        } else {
            Assert.assertNotNull("after is not null", interceptor.getAfterMethod());
            Assert.assertEquals("check afterName", interceptor.getAfterMethod().getName(), afterName);
        }
    }


    public static class TestBeforeInterceptor implements AroundInterceptor {

        @Override
        public void before(Object target, Object[] args) {

        }
        @IgnoreMethod
        @Override
        public void after(Object target, Object[] args, Object result, Throwable throwable) {

        }
    }

    public static class TestAfterInterceptor implements AroundInterceptor {
        @IgnoreMethod
        @Override
        public void before(Object target, Object[] args) {

        }

        @Override
        public void after(Object target, Object[] args, Object result, Throwable throwable) {

        }
    }

    public static class TestAroundInterceptor implements AroundInterceptor {
        @Override
        public void before(Object target, Object[] args) {

        }

        @Override
        public void after(Object target, Object[] args, Object result, Throwable throwable) {

        }
    }

    public static class TestIgnoreInterceptor implements AroundInterceptor {
        @IgnoreMethod
        @Override
        public void before(Object target, Object[] args) {

        }
        @IgnoreMethod
        @Override
        public void after(Object target, Object[] args, Object result, Throwable throwable) {

        }
    }

    public static class InheritedAroundInterceptor extends TestAroundInterceptor {

    }
}