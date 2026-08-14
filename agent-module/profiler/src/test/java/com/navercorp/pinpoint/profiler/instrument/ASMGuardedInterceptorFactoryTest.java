/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandler;
import com.navercorp.pinpoint.bootstrap.interceptor.InjectedAsyncContextApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ResultReplaceAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.profiler.logging.Log4j2LoggerBinderInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ASMGuardedInterceptorFactoryTest {

    private final List<Throwable> handled = new ArrayList<>();
    private final ExceptionHandler guard = handled::add;
    private final ExceptionHandler rethrow = t -> {
        throw new RuntimeException(t);
    };

    // the scoped guard template initializes a PluginLogger in its constructor.
    @BeforeAll
    public static void beforeAll() {
        Log4j2LoggerBinderInitializer.beforeClass();
    }

    @AfterAll
    public static void afterAll() {
        Log4j2LoggerBinderInitializer.afterClass();
    }

    @Test
    public void delegatesWithArguments() {
        RecordingAroundInterceptor delegate = new RecordingAroundInterceptor();
        Interceptor wrapped = ASMGuardedInterceptorFactory.wrap(delegate, guard);

        assertThat(wrapped).isNotNull().isInstanceOf(AroundInterceptor.class);
        assertThat(wrapped.getClass()).isNotEqualTo(RecordingAroundInterceptor.class);

        Object target = new Object();
        Object[] args = {"a", 1};
        ((AroundInterceptor) wrapped).before(target, args);
        ((AroundInterceptor) wrapped).after(target, args, "result", null);

        assertThat(delegate.beforeTarget).isSameAs(target);
        assertThat(delegate.beforeArgs).isSameAs(args);
        assertThat(delegate.afterResult).isEqualTo("result");
        assertThat(handled).isEmpty();
    }

    @Test
    public void guardSwallowsDelegateThrowable() {
        Interceptor wrapped = ASMGuardedInterceptorFactory.wrap(new ThrowingAroundInterceptor(), guard);

        assertThat(wrapped).isNotNull();
        ((AroundInterceptor) wrapped).before(null, null);
        ((AroundInterceptor) wrapped).after(null, null, null, null);

        assertThat(handled).hasSize(2);
        assertThat(handled.get(0)).hasMessage("boom-before");
        assertThat(handled.get(1)).hasMessage("boom-after");
    }

    @Test
    public void rethrowHandlerPropagates() {
        Interceptor wrapped = ASMGuardedInterceptorFactory.wrap(new ThrowingAroundInterceptor(), rethrow);

        assertThat(wrapped).isNotNull();
        assertThatThrownBy(() -> ((AroundInterceptor) wrapped).before(null, null))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    public void generatedClassIsReusedPerDelegateClass() {
        Interceptor first = ASMGuardedInterceptorFactory.wrap(new RecordingAroundInterceptor(), guard);
        Interceptor second = ASMGuardedInterceptorFactory.wrap(new RecordingAroundInterceptor(), guard);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first).isNotSameAs(second);
        assertThat(first.getClass()).isSameAs(second.getClass());
    }

    @Test
    public void injectedAsyncContextShapeCoversWideDescriptors() {
        RecordingInjectedInterceptor delegate = new RecordingInjectedInterceptor();
        Interceptor wrapped = ASMGuardedInterceptorFactory.wrap(delegate, guard);

        assertThat(wrapped).isNotNull().isInstanceOf(InjectedAsyncContextApiIdAwareAroundInterceptor.class);

        Object[] args = {"x"};
        ((InjectedAsyncContextApiIdAwareAroundInterceptor) wrapped).before("t", null, 42, args);
        ((InjectedAsyncContextApiIdAwareAroundInterceptor) wrapped).after("t", null, 42, args, "r", null);

        assertThat(delegate.beforeApiId).isEqualTo(42);
        assertThat(delegate.afterResult).isEqualTo("r");
        assertThat(handled).isEmpty();
    }

    @Test
    public void nonVoidShapeIsIneligible() {
        assertThat(ASMGuardedInterceptorFactory.wrap(new ResultReplacingInterceptor(), guard)).isNull();
    }

    @Test
    public void multiShapeDelegateIsIneligible() {
        assertThat(ASMGuardedInterceptorFactory.wrap(new MultiShapeInterceptor(), guard)).isNull();
    }

    @Test
    public void nonPublicDelegateIsIneligible() {
        assertThat(ASMGuardedInterceptorFactory.wrap(new PackagePrivateInterceptor(), guard)).isNull();
    }

    @Test
    public void scopedDelegatesInsideScope() {
        InterceptorScope scope = mock(InterceptorScope.class);
        InterceptorScopeInvocation invocation = mock(InterceptorScopeInvocation.class);
        when(scope.getCurrentInvocation()).thenReturn(invocation);
        when(invocation.tryEnter(ExecutionPolicy.BOUNDARY)).thenReturn(true);
        when(invocation.canLeave(ExecutionPolicy.BOUNDARY)).thenReturn(true);

        RecordingAroundInterceptor delegate = new RecordingAroundInterceptor();
        Interceptor wrapped = ASMGuardedInterceptorFactory.wrapScoped(delegate, scope, ExecutionPolicy.BOUNDARY, guard);

        assertThat(wrapped).isNotNull().isInstanceOf(AroundInterceptor.class);
        assertThat(wrapped.getClass().getName()).contains("GuardedScopedInterceptor$$");

        Object target = new Object();
        Object[] args = {"a"};
        ((AroundInterceptor) wrapped).before(target, args);
        ((AroundInterceptor) wrapped).after(target, args, "result", null);

        assertThat(delegate.beforeTarget).isSameAs(target);
        assertThat(delegate.afterResult).isEqualTo("result");
        verify(invocation).leave(ExecutionPolicy.BOUNDARY);
        assertThat(handled).isEmpty();
    }

    @Test
    public void scopedSkipsWhenScopeRejects() {
        InterceptorScope scope = mock(InterceptorScope.class);
        InterceptorScopeInvocation invocation = mock(InterceptorScopeInvocation.class);
        when(scope.getCurrentInvocation()).thenReturn(invocation);
        when(invocation.tryEnter(ExecutionPolicy.BOUNDARY)).thenReturn(false);
        when(invocation.canLeave(ExecutionPolicy.BOUNDARY)).thenReturn(false);

        RecordingAroundInterceptor delegate = new RecordingAroundInterceptor();
        Interceptor wrapped = ASMGuardedInterceptorFactory.wrapScoped(delegate, scope, ExecutionPolicy.BOUNDARY, guard);

        assertThat(wrapped).isNotNull();
        ((AroundInterceptor) wrapped).before(new Object(), null);
        ((AroundInterceptor) wrapped).after(new Object(), null, null, null);

        assertThat(delegate.beforeTarget).isNull();
        assertThat(delegate.afterResult).isNull();
        verify(invocation, never()).leave(ExecutionPolicy.BOUNDARY);
    }

    @Test
    public void scopedLeaveRunsEvenWhenDelegateThrows() {
        InterceptorScope scope = mock(InterceptorScope.class);
        InterceptorScopeInvocation invocation = mock(InterceptorScopeInvocation.class);
        when(scope.getCurrentInvocation()).thenReturn(invocation);
        when(invocation.tryEnter(ExecutionPolicy.ALWAYS)).thenReturn(true);
        when(invocation.canLeave(ExecutionPolicy.ALWAYS)).thenReturn(true);

        Interceptor wrapped = ASMGuardedInterceptorFactory.wrapScoped(new ThrowingAroundInterceptor(), scope, ExecutionPolicy.ALWAYS, guard);

        assertThat(wrapped).isNotNull();
        ((AroundInterceptor) wrapped).before(null, null);
        ((AroundInterceptor) wrapped).after(null, null, null, null);

        assertThat(handled).hasSize(2);
        verify(invocation).leave(ExecutionPolicy.ALWAYS);
    }

    @Test
    public void scopedGeneratedClassIsReusedPerDelegateClass() {
        InterceptorScope scope = mock(InterceptorScope.class);
        Interceptor first = ASMGuardedInterceptorFactory.wrapScoped(new RecordingAroundInterceptor(), scope, ExecutionPolicy.BOUNDARY, guard);
        Interceptor second = ASMGuardedInterceptorFactory.wrapScoped(new RecordingAroundInterceptor(), scope, ExecutionPolicy.BOUNDARY, guard);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first.getClass()).isSameAs(second.getClass());
    }

    @Test
    public void scopedNonVoidShapeIsIneligible() {
        InterceptorScope scope = mock(InterceptorScope.class);
        assertThat(ASMGuardedInterceptorFactory.wrapScoped(new ResultReplacingInterceptor(), scope, ExecutionPolicy.BOUNDARY, guard)).isNull();
    }

    public static class RecordingAroundInterceptor implements AroundInterceptor {
        Object beforeTarget;
        Object[] beforeArgs;
        Object afterResult;

        @Override
        public void before(Object target, Object[] args) {
            this.beforeTarget = target;
            this.beforeArgs = args;
        }

        @Override
        public void after(Object target, Object[] args, Object result, Throwable throwable) {
            this.afterResult = result;
        }
    }

    public static class ThrowingAroundInterceptor implements AroundInterceptor {
        @Override
        public void before(Object target, Object[] args) {
            throw new IllegalStateException("boom-before");
        }

        @Override
        public void after(Object target, Object[] args, Object result, Throwable throwable) {
            throw new IllegalStateException("boom-after");
        }
    }

    public static class RecordingInjectedInterceptor implements InjectedAsyncContextApiIdAwareAroundInterceptor {
        int beforeApiId;
        Object afterResult;

        @Override
        public void before(Object target, AsyncContext asyncContext, int apiId, Object[] args) {
            this.beforeApiId = apiId;
        }

        @Override
        public void after(Object target, AsyncContext asyncContext, int apiId, Object[] args, Object result, Throwable throwable) {
            this.afterResult = result;
        }
    }

    public static class ResultReplacingInterceptor implements ResultReplaceAroundInterceptor {
        @Override
        public void before(Object target, Class<?> returnType, Object[] args) {
        }

        @Override
        public Object after(Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
            return result;
        }
    }

    public static class MultiShapeInterceptor implements AroundInterceptor, InjectedAsyncContextApiIdAwareAroundInterceptor {
        @Override
        public void before(Object target, Object[] args) {
        }

        @Override
        public void after(Object target, Object[] args, Object result, Throwable throwable) {
        }

        @Override
        public void before(Object target, AsyncContext asyncContext, int apiId, Object[] args) {
        }

        @Override
        public void after(Object target, AsyncContext asyncContext, int apiId, Object[] args, Object result, Throwable throwable) {
        }
    }

    static class PackagePrivateInterceptor implements AroundInterceptor {
        @Override
        public void before(Object target, Object[] args) {
        }

        @Override
        public void after(Object target, Object[] args, Object result, Throwable throwable) {
        }
    }

    // The packaged agent cannot read the scoped templates through Class.getResourceAsStream
    // (boot-appended jars serve classes, not resources) and falls back to reading the entry from
    // the bootstrap jar list. Verifies that lookup against a jar built like bootstrap-core.
    @Test
    public void scopedTemplate_readableThroughBootstrapJarList() throws Exception {
        Class<?> template = com.navercorp.pinpoint.bootstrap.interceptor.scope.ExceptionHandleScopedInterceptor.class;
        String resourceName = com.navercorp.pinpoint.profiler.util.JavaAssistUtils.javaNameToJvmName(template.getName()) + ".class";

        byte[] classpathBytes;
        try (java.io.InputStream in = template.getResourceAsStream(template.getSimpleName() + ".class")) {
            classpathBytes = com.navercorp.pinpoint.common.util.IOUtils.toByteArray(in);
        }

        java.nio.file.Path jar = java.nio.file.Files.createTempFile("fake-bootstrap-core", ".jar");
        try {
            try (java.util.jar.JarOutputStream out = new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(jar))) {
                out.putNextEntry(new java.util.jar.JarEntry(resourceName));
                out.write(classpathBytes);
                out.closeEntry();
            }

            com.navercorp.pinpoint.profiler.instrument.classloading.BootstrapCore bootstrapCore =
                    new com.navercorp.pinpoint.profiler.instrument.classloading.BootstrapCore(java.util.Collections.singletonList(jar));
            try (java.io.InputStream in = bootstrapCore.openStream(resourceName)) {
                assertThat(in).isNotNull();
                byte[] jarBytes = com.navercorp.pinpoint.common.util.IOUtils.toByteArray(in);
                assertThat(jarBytes).isEqualTo(classpathBytes);
            }
        } finally {
            // the scanner may still hold the jar open on Windows - best effort
            try {
                java.nio.file.Files.deleteIfExists(jar);
            } catch (java.io.IOException ignored) {
                jar.toFile().deleteOnExit();
            }
        }
    }
}
