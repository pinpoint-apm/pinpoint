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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
public class InterceptorDefinitionFactoryTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testGetInterceptorType_BasicType() {
        InterceptorDefinitionFactory typeDetector = new InterceptorDefinitionFactory();

        Assertions.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor.class).getCaptureType(), CaptureType.AROUND);

        Assertions.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor0.class).getCaptureType(), CaptureType.AROUND);
        Assertions.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor1.class).getCaptureType(), CaptureType.AROUND);
        Assertions.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor2.class).getCaptureType(), CaptureType.AROUND);
        Assertions.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor3.class).getCaptureType(), CaptureType.AROUND);
        Assertions.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor4.class).getCaptureType(), CaptureType.AROUND);
        Assertions.assertSame(typeDetector.createInterceptorDefinition(AroundInterceptor5.class).getCaptureType(), CaptureType.AROUND);

        Assertions.assertSame(typeDetector.createInterceptorDefinition(StaticAroundInterceptor.class).getCaptureType(), CaptureType.AROUND);

        Assertions.assertSame(typeDetector.createInterceptorDefinition(ApiIdAwareAroundInterceptor.class).getCaptureType(), CaptureType.AROUND);
    }


    @Test
    public void testGetInterceptorType_Inherited() {
        InterceptorDefinitionFactory typeDetector = new InterceptorDefinitionFactory();

        Assertions.assertSame(typeDetector.createInterceptorDefinition(InheritedAroundInterceptor.class).getCaptureType(), CaptureType.AROUND);
    }

    @Test
    public void testDeclaredMethods() {

        Class<String> stringClass = String.class;
        for (Method method : stringClass.getDeclaredMethods()) {
            logger.debug("{}", method);
        }

    }


    @Test
    public void testGetType_Error() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            InterceptorDefinitionFactory typeDetector = new InterceptorDefinitionFactory();
            typeDetector.createInterceptorDefinition(Interceptor.class);
        });
    }


    @Test
    public void testGetInterceptorCaptureType() {
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
        Assertions.assertSame(aroundType, aroundType, "Type");

        if (beforeName == null) {
            Assertions.assertNull(interceptor.getBeforeMethod(), "before is null");
        } else {
            Assertions.assertNotNull(interceptor.getBeforeMethod(), "after is not null");
            Assertions.assertEquals(interceptor.getBeforeMethod().getName(), beforeName, "check beforeName");
        }

        if (afterName == null) {
            Assertions.assertNull(interceptor.getAfterMethod(), "after is null");
        } else {
            Assertions.assertNotNull(interceptor.getAfterMethod(), "after is not null");
            Assertions.assertEquals(interceptor.getAfterMethod().getName(), afterName, "check afterName");
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