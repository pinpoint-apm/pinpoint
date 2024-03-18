/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.interceptor.scope;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ScopedInterceptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScopedInterceptorTest {

    @Mock
    InterceptorScope scope;

    @Spy
    InterceptorScopeInvocation scopeInvocation = new DefaultInterceptorScopeInvocation("test");

    @BeforeEach
    void setup(){
        when(scope.getCurrentInvocation()).thenReturn(scopeInvocation);
    }

    @Test
    void execute_always() {

        ExecutionPolicy executionPolicy = ExecutionPolicy.ALWAYS;

        AroundInterceptor innerInterceptor1 = mock(AroundInterceptor.class);
        ScopedInterceptor scopeInterceptor1 = new ScopedInterceptor(innerInterceptor1, scope, executionPolicy);

        AroundInterceptor innerInterceptor2 = mock(AroundInterceptor.class);
        ScopedInterceptor scopeInterceptor2 = new ScopedInterceptor(innerInterceptor2, scope, executionPolicy);


        Object target = new Object();
        scopeInterceptor1.before(target, null);
        scopeInterceptor1.after(target, null, null, null);

        scopeInterceptor2.before(target, null);
        scopeInterceptor2.after(target, null, null, null);

        verify(innerInterceptor1, times(1)).before(target, null);
        verify(innerInterceptor1, times(1)).after(target, null, null, null);

        verify(innerInterceptor2, times(1)).before(target, null);
        verify(innerInterceptor2, times(1)).after(target, null, null, null);
    }


    @Test
    void execute_always_exception() {

        ExecutionPolicy executionPolicy = ExecutionPolicy.ALWAYS;

        AroundInterceptor innerInterceptor1 = mock(AroundInterceptor.class);
        ScopedInterceptor scopeInterceptor1 = new ScopedInterceptor(innerInterceptor1, scope, executionPolicy);

        AroundInterceptor innerInterceptor2 = mock(AroundInterceptor.class);
        ScopedInterceptor scopeInterceptor2 = new ScopedInterceptor(innerInterceptor2, scope, executionPolicy);
        RuntimeException error = new RuntimeException("test");
        doThrow(error).when(innerInterceptor2).after(any(), any(), any(), any());


        Object target = new Object();
        scopeInterceptor1.before(target, null);
        scopeInterceptor1.after(target, null, null, null);

        scopeInterceptor2.before(target, null);
        try {
            scopeInterceptor2.after(target, null, null, null);
        } catch (Exception exception) {
            Assertions.assertSame(error, exception);
        }

        verify(scopeInvocation, times(2)).tryEnter(executionPolicy);
        verify(scopeInvocation, times(2)).leave(executionPolicy);
    }

    @Test
    void execute_boundary() {

        ExecutionPolicy executionPolicy = ExecutionPolicy.BOUNDARY;

        AroundInterceptor innerInterceptor1 = mock(AroundInterceptor.class);
        ScopedInterceptor scopeInterceptor1 = new ScopedInterceptor(innerInterceptor1, scope, executionPolicy);

        AroundInterceptor innerInterceptor2 = mock(AroundInterceptor.class);
        ScopedInterceptor scopeInterceptor2 = new ScopedInterceptor(innerInterceptor2, scope, executionPolicy);

        Object target = new Object();
        scopeInterceptor1.before(target, null);
        scopeInterceptor2.before(target, null);
        scopeInterceptor2.after(target, null, null, null);
        scopeInterceptor1.after(target, null, null, null);

        verify(innerInterceptor1, times(1)).before(target, null);
        verify(innerInterceptor1, times(1)).after(target, null, null, null);

        verify(innerInterceptor2, never()).before(target, null);
        verify(innerInterceptor2, never()).after(target, null, null, null);
    }

}