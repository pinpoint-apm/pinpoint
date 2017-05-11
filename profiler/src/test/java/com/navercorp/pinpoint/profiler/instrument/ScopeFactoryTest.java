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
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.profiler.interceptor.scope.DefaultInterceptorScope;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ScopeFactoryTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void newScopeInfo_annotation() throws Exception {
        InstrumentContext context = Mockito.mock(InstrumentContext.class);
        Mockito.when(context.getInterceptorScope("test")).thenReturn(new DefaultInterceptorScope("test"));
        Class<?> scopeTestInterceptorClass = Interceptor_Annotation.class;
        Mockito.doReturn(scopeTestInterceptorClass).when(context).injectClass(null, scopeTestInterceptorClass.getName());

        ScopeFactory scopeFactory = new ScopeFactory();
        ScopeInfo scopeInfo = scopeFactory.newScopeInfo(null, context, scopeTestInterceptorClass.getName(), null, null);

        logger.debug("scopeInfo:{}", scopeInfo);
        Assert.assertEquals(scopeInfo.getInterceptorScope().getName(), "test");
        Assert.assertEquals(scopeInfo.getExecutionPolicy(), ExecutionPolicy.BOUNDARY);
    }

    @Test
    public void newScopeInfo_annotation_and_policy() throws Exception {
        InstrumentContext context = Mockito.mock(InstrumentContext.class);
        Mockito.when(context.getInterceptorScope("test")).thenReturn(new DefaultInterceptorScope("test"));
        Class<?> scopeTestInterceptorClass = Interceptor_AnnotationAndPolicy.class;
        Mockito.doReturn(scopeTestInterceptorClass).when(context).injectClass(null, scopeTestInterceptorClass.getName());

        ScopeFactory scopeFactory = new ScopeFactory();
        ScopeInfo scopeInfo = scopeFactory.newScopeInfo(null, context, scopeTestInterceptorClass.getName(), null, null);


        logger.debug("scopeInfo:{}", scopeInfo);
        Assert.assertEquals(scopeInfo.getInterceptorScope().getName(), "test");
        Assert.assertEquals(scopeInfo.getExecutionPolicy(), ExecutionPolicy.ALWAYS);
    }


    @Test
    public void newScopeInfo_policyIsNull() throws Exception {
        InstrumentContext context = Mockito.mock(InstrumentContext.class);
        InterceptorScope interceptorScope = new DefaultInterceptorScope("test");
        Mockito.when(context.getInterceptorScope("test")).thenReturn(interceptorScope);
        Class<?> scopeTestInterceptorClass = Interceptor_AnnotationAndPolicy.class;
        Mockito.doReturn(scopeTestInterceptorClass).when(context).injectClass(null, scopeTestInterceptorClass.getName());

        ScopeFactory scopeFactory = new ScopeFactory();
        ScopeInfo scopeInfo = scopeFactory.newScopeInfo(null, context, scopeTestInterceptorClass.getName(), interceptorScope, null);


        logger.debug("scopeInfo:{}", scopeInfo);
        Assert.assertEquals(scopeInfo.getInterceptorScope().getName(), "test");
        Assert.assertEquals(scopeInfo.getExecutionPolicy(), ExecutionPolicy.BOUNDARY);
    }



    @Test
    public void newScopeInfo_compatibility_check1() throws Exception {
        InstrumentContext context = Mockito.mock(InstrumentContext.class);
        Class<?> scopeTestInterceptorClass = TestInterceptor_NoAnnotation.class;
        String scopeTestInterceptorClassName = scopeTestInterceptorClass.getName();
        Mockito.doReturn(scopeTestInterceptorClass).when(context).injectClass(null, scopeTestInterceptorClassName);

        ScopeFactory scopeFactory = new ScopeFactory();
        ScopeInfo scopeInfo = scopeFactory.newScopeInfo(null, context, scopeTestInterceptorClassName, null, null);
        ScopeInfo old_scopeInfo = old_resolveScopeInfo(context, null, scopeTestInterceptorClassName, null, null);

        logger.debug("scopeInfo:{}", scopeInfo);
        logger.debug("scopeInfo:{}", old_scopeInfo);
        Assert.assertTrue(deepEquals(scopeInfo, old_scopeInfo));

    }



    @Test
    public void newScopeInfo_compatibility_check2() throws Exception {
        InstrumentContext context = Mockito.mock(InstrumentContext.class);
        InterceptorScope interceptorScope = new DefaultInterceptorScope("test");
        Class<?> scopeTestInterceptorClass = TestInterceptor_NoAnnotation.class;
        String scopeTestInterceptorClassName = scopeTestInterceptorClass.getName();
        Mockito.doReturn(scopeTestInterceptorClass).when(context).injectClass(null, scopeTestInterceptorClassName);

        ScopeFactory scopeFactory = new ScopeFactory();
        ScopeInfo scopeInfo = scopeFactory.newScopeInfo(null, context, scopeTestInterceptorClassName, interceptorScope, null);
        ScopeInfo old_scopeInfo = old_resolveScopeInfo(context, null, scopeTestInterceptorClassName, interceptorScope, null);

        logger.debug("scopeInfo:{}", scopeInfo);
        logger.debug("scopeInfo:{}", old_scopeInfo);
        Assert.assertTrue(deepEquals(scopeInfo, old_scopeInfo));

    }

    private boolean deepEquals(ScopeInfo scopeInfoA, ScopeInfo scopeInfoB) {
        if (same(scopeInfoA, scopeInfoB)) {
            return true;
        }
        if (!scopeEquals(scopeInfoA, scopeInfoB)) {
            return false;
        }
        if (!policyEquals(scopeInfoA, scopeInfoB)) {
            return false;
        }
        return true;
    }


    private boolean scopeEquals(ScopeInfo scopeInfoA, ScopeInfo scopeInfoB) {
        final InterceptorScope interceptorScopeA = scopeInfoA.getInterceptorScope();
        final InterceptorScope interceptorScopeB = scopeInfoB.getInterceptorScope();
        if (same(interceptorScopeA, interceptorScopeB)) {
            return true;
        }
        if (interceptorScopeA.getName().equals(interceptorScopeB.getName())) {
            return true;
        }
        return false;
    }

    private boolean policyEquals(ScopeInfo scopeInfoA, ScopeInfo scopeInfoB) {
        final ExecutionPolicy executionPolicyA = scopeInfoA.getExecutionPolicy();
        final ExecutionPolicy executionPolicyB = scopeInfoB.getExecutionPolicy();
        if (same(executionPolicyA, executionPolicyB)) {
            return true;
        }
        return false;
    }

    private <T> boolean same(T a, T b) {
        return a == b;
    }


    // original source
    private ScopeInfo old_resolveScopeInfo(InstrumentContext instrumentContext, ClassLoader classLoader, String interceptorClassName, InterceptorScope scope, ExecutionPolicy policy) {

        if (scope == null) {
            final Class<? extends Interceptor> interceptorType = instrumentContext.injectClass(classLoader, interceptorClassName);
            final Scope interceptorScope = interceptorType.getAnnotation(Scope.class);

            if (interceptorScope != null) {
                final String scopeName = interceptorScope.value();
                scope = instrumentContext.getInterceptorScope(scopeName);
                // @NotNull
                policy = interceptorScope.executionPolicy();
            }
        }

        if (scope == null) {
            policy = null;
        } else if (policy == null) {
            policy = ExecutionPolicy.BOUNDARY;
        }

        return new ScopeInfo(scope, policy);
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