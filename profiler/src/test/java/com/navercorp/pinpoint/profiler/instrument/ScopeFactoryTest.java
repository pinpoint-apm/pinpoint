/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.profiler.interceptor.scope.DefaultInterceptorScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ScopeFactoryTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void newScopeInfo_annotation() {
        InstrumentContext context = Mockito.mock(InstrumentContext.class);
        Mockito.when(context.getInterceptorScope("test")).thenReturn(new DefaultInterceptorScope("test"));


        ScopeFactory scopeFactory = new ScopeFactory();
        ScopeInfo scopeInfo = scopeFactory.newScopeInfo(context, Interceptor_Annotation.class, null, null);

        logger.debug("scopeInfo:{}", scopeInfo);
        Assertions.assertEquals(scopeInfo.getInterceptorScope().getName(), "test");
        Assertions.assertEquals(scopeInfo.getExecutionPolicy(), ExecutionPolicy.BOUNDARY);
    }

    @Test
    public void newScopeInfo_annotation_and_policy() {
        InstrumentContext context = Mockito.mock(InstrumentContext.class);
        Mockito.when(context.getInterceptorScope("test")).thenReturn(new DefaultInterceptorScope("test"));

        ScopeFactory scopeFactory = new ScopeFactory();
        ScopeInfo scopeInfo = scopeFactory.newScopeInfo(context, Interceptor_AnnotationAndPolicy.class, null, null);


        logger.debug("scopeInfo:{}", scopeInfo);
        Assertions.assertEquals(scopeInfo.getInterceptorScope().getName(), "test");
        Assertions.assertEquals(scopeInfo.getExecutionPolicy(), ExecutionPolicy.ALWAYS);
    }


    @Test
    public void newScopeInfo_policyIsNull() {
        InstrumentContext context = Mockito.mock(InstrumentContext.class);
        InterceptorScope interceptorScope = new DefaultInterceptorScope("test");
        Mockito.when(context.getInterceptorScope("test")).thenReturn(interceptorScope);

        ScopeFactory scopeFactory = new ScopeFactory();
        ScopeInfo scopeInfo = scopeFactory.newScopeInfo(context, Interceptor_AnnotationAndPolicy.class, interceptorScope, null);


        logger.debug("scopeInfo:{}", scopeInfo);
        Assertions.assertEquals(scopeInfo.getInterceptorScope().getName(), "test");
        Assertions.assertEquals(scopeInfo.getExecutionPolicy(), ExecutionPolicy.BOUNDARY);
    }

    @Scope("test")
    public static class Interceptor_Annotation {
    }


    @Scope(value = "test", executionPolicy = ExecutionPolicy.ALWAYS)
    public static class Interceptor_AnnotationAndPolicy {
    }

    public class TestInterceptor_NoAnnotation {
    }
}