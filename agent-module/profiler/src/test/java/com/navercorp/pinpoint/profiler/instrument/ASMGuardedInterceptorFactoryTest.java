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
import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor2;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor3;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor4;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockAroundInterceptor5;
import com.navercorp.pinpoint.bootstrap.interceptor.BlockStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandler;
import com.navercorp.pinpoint.bootstrap.interceptor.InjectedAsyncContextApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ResultReplaceAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ResultReplaceBlockAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.profiler.logging.Log4j2LoggerBinderInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ASMGuardedInterceptorFactoryTest {
    private final ASMGuardedInterceptorFactory factory = new ASMGuardedInterceptorFactory(null);

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
        Interceptor wrapped = factory.wrap(delegate, guard);

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
        Interceptor wrapped = factory.wrap(new ThrowingAroundInterceptor(), guard);

        assertThat(wrapped).isNotNull();
        ((AroundInterceptor) wrapped).before(null, null);
        ((AroundInterceptor) wrapped).after(null, null, null, null);

        assertThat(handled).hasSize(2);
        assertThat(handled.get(0)).hasMessage("boom-before");
        assertThat(handled.get(1)).hasMessage("boom-after");
    }

    @Test
    public void rethrowHandlerPropagates() {
        Interceptor wrapped = factory.wrap(new ThrowingAroundInterceptor(), rethrow);

        assertThat(wrapped).isNotNull();
        assertThatThrownBy(() -> ((AroundInterceptor) wrapped).before(null, null))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    public void generatedClassIsReusedPerDelegateClass() {
        Interceptor first = factory.wrap(new RecordingAroundInterceptor(), guard);
        Interceptor second = factory.wrap(new RecordingAroundInterceptor(), guard);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first).isNotSameAs(second);
        assertThat(first.getClass()).isSameAs(second.getClass());
    }

    @Test
    public void injectedAsyncContextShapeCoversWideDescriptors() {
        RecordingInjectedInterceptor delegate = new RecordingInjectedInterceptor();
        Interceptor wrapped = factory.wrap(delegate, guard);

        assertThat(wrapped).isNotNull().isInstanceOf(InjectedAsyncContextApiIdAwareAroundInterceptor.class);

        Object[] args = {"x"};
        ((InjectedAsyncContextApiIdAwareAroundInterceptor) wrapped).before("t", null, 42, args);
        ((InjectedAsyncContextApiIdAwareAroundInterceptor) wrapped).after("t", null, 42, args, "r", null);

        assertThat(delegate.beforeApiId).isEqualTo(42);
        assertThat(delegate.afterResult).isEqualTo("r");
        assertThat(handled).isEmpty();
    }

    @Test
    public void resultReplaceAfterPassesThroughReplacedResult() {
        ReplacingResultInterceptor delegate = new ReplacingResultInterceptor();
        Interceptor wrapped = factory.wrap(delegate, guard);

        assertThat(wrapped).isNotNull().isInstanceOf(ResultReplaceAroundInterceptor.class);
        assertThat(wrapped.getClass().getName()).contains("GuardedInterceptor$$");

        Object[] args = {"a"};
        ((ResultReplaceAroundInterceptor) wrapped).before("t", String.class, args);
        Object replaced = ((ResultReplaceAroundInterceptor) wrapped).after("t", String.class, args, "original", null);

        assertThat(replaced).isSameAs(delegate.replacement);
        assertThat(delegate.afterResult).isEqualTo("original");
        assertThat(handled).isEmpty();
    }

    @Test
    public void resultReplaceAfterReturnsOriginalResultWhenDelegateThrows() {
        Interceptor wrapped = factory.wrap(new ThrowingResultReplaceInterceptor(), guard);

        assertThat(wrapped).isNotNull();
        Object original = new Object();
        Object returned = ((ResultReplaceAroundInterceptor) wrapped).after("t", Object.class, null, original, null);

        // the shared ExceptionHandleResultReplaceAroundInterceptor returns the result it was given, so
        // the woven method still returns its own value
        assertThat(returned).isSameAs(original);
        assertThat(handled).hasSize(1);
        assertThat(handled.get(0)).hasMessage("boom-after");
    }

    @Test
    public void resultReplaceBlockPassesThroughBlockAndReplacedResult() {
        ReplacingResultBlockInterceptor delegate = new ReplacingResultBlockInterceptor();
        Interceptor wrapped = factory.wrap(delegate, guard);

        assertThat(wrapped).isNotNull().isInstanceOf(ResultReplaceBlockAroundInterceptor.class);

        Object[] args = {"a"};
        TraceBlock block = ((ResultReplaceBlockAroundInterceptor) wrapped).before("t", String.class, args);
        Object replaced = ((ResultReplaceBlockAroundInterceptor) wrapped).after(block, "t", String.class, args, "original", null);

        assertThat(block).isSameAs(delegate.block);
        assertThat(delegate.afterBlock).isSameAs(delegate.block);
        assertThat(replaced).isSameAs(delegate.replacement);
        assertThat(handled).isEmpty();
    }

    @Test
    public void resultReplaceBlockFallsBackToNullBlockAndOriginalResultWhenDelegateThrows() {
        Interceptor wrapped = factory.wrap(new ThrowingResultReplaceBlockInterceptor(), guard);

        assertThat(wrapped).isNotNull();
        TraceBlock block = ((ResultReplaceBlockAroundInterceptor) wrapped).before("t", Object.class, null);
        Object original = new Object();
        Object returned = ((ResultReplaceBlockAroundInterceptor) wrapped).after(mock(TraceBlock.class), "t", Object.class, null, original, null);

        assertThat(block).isNull();
        assertThat(returned).isSameAs(original);
        assertThat(handled).hasSize(2);
        assertThat(handled.get(0)).hasMessage("boom-before");
        assertThat(handled.get(1)).hasMessage("boom-after");
    }

    /**
     * The rules return the {@code result} argument by index (3 for ResultReplace, 4 for the Block
     * variant); this pins the parameter lists those indexes were read from.
     */
    @Test
    public void resultReplaceShapesKeepResultAtTheRegisteredIndex() throws Exception {
        Method after = ResultReplaceAroundInterceptor.class.getMethod("after", Object.class, Class.class, Object[].class, Object.class, Throwable.class);
        assertThat(after.getReturnType()).isEqualTo(Object.class);
        assertThat(after.getParameterTypes()[3]).isEqualTo(Object.class);

        Method blockAfter = ResultReplaceBlockAroundInterceptor.class.getMethod("after", TraceBlock.class, Object.class, Class.class, Object[].class, Object.class, Throwable.class);
        assertThat(blockAfter.getReturnType()).isEqualTo(Object.class);
        assertThat(blockAfter.getParameterTypes()[4]).isEqualTo(Object.class);
        assertThat(ResultReplaceBlockAroundInterceptor.class.getMethod("before", Object.class, Class.class, Object[].class).getReturnType()).isEqualTo(TraceBlock.class);
    }

    @Test
    public void unregisteredNonVoidShapeStaysIneligible() {
        assertThat(factory.wrap(new UnregisteredNonVoidInterceptor(), guard)).isNull();
        assertThat(factory.wrapScoped(new UnregisteredNonVoidInterceptor(), mock(InterceptorScope.class), ExecutionPolicy.BOUNDARY, guard)).isNull();
    }

    @Test
    public void blockBeforePassesThroughReturnValue() {
        RecordingBlockInterceptor delegate = new RecordingBlockInterceptor();
        Interceptor wrapped = factory.wrap(delegate, guard);

        assertThat(wrapped).isNotNull().isInstanceOf(BlockAroundInterceptor.class);
        assertThat(wrapped.getClass().getName()).contains("GuardedInterceptor$$");

        Object target = new Object();
        Object[] args = {"a", 1};
        TraceBlock block = ((BlockAroundInterceptor) wrapped).before(target, args);
        ((BlockAroundInterceptor) wrapped).after(block, target, args, "result", null);

        assertThat(block).isSameAs(delegate.block);
        assertThat(delegate.beforeTarget).isSameAs(target);
        assertThat(delegate.beforeArgs).isSameAs(args);
        assertThat(delegate.afterBlock).isSameAs(delegate.block);
        assertThat(delegate.afterResult).isEqualTo("result");
        assertThat(handled).isEmpty();
    }

    @Test
    public void blockBeforeReturnsNullWhenDelegateThrows() {
        Interceptor wrapped = factory.wrap(new ThrowingBlockInterceptor(), guard);

        assertThat(wrapped).isNotNull();
        TraceBlock block = ((BlockAroundInterceptor) wrapped).before(null, null);

        // the shared ExceptionHandleBlockAroundInterceptor returns null here, which makes the
        // Block bases' after() a no-op for this invocation
        assertThat(block).isNull();
        assertThat(handled).hasSize(1);
        assertThat(handled.get(0)).hasMessage("boom-before");
    }

    @Test
    public void blockAfterSwallowsDelegateThrowable() {
        Interceptor wrapped = factory.wrap(new ThrowingBlockInterceptor(), guard);

        assertThat(wrapped).isNotNull();
        ((BlockAroundInterceptor) wrapped).after(mock(TraceBlock.class), null, null, null, null);

        assertThat(handled).hasSize(1);
        assertThat(handled.get(0)).hasMessage("boom-after");
    }

    @Test
    public void rethrowHandlerPropagatesForBlockShape() {
        Interceptor wrapped = factory.wrap(new ThrowingBlockInterceptor(), rethrow);

        assertThat(wrapped).isNotNull();
        assertThatThrownBy(() -> ((BlockAroundInterceptor) wrapped).before(null, null))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    public void blockApiIdAwareShapeIsEligible() {
        RecordingBlockApiIdAwareInterceptor delegate = new RecordingBlockApiIdAwareInterceptor();
        Interceptor wrapped = factory.wrap(delegate, guard);

        assertThat(wrapped).isNotNull().isInstanceOf(BlockApiIdAwareAroundInterceptor.class);

        Object[] args = {"x"};
        TraceBlock block = ((BlockApiIdAwareAroundInterceptor) wrapped).before("t", 42, args);
        ((BlockApiIdAwareAroundInterceptor) wrapped).after(block, "t", 42, args, "r", null);

        assertThat(block).isSameAs(delegate.block);
        assertThat(delegate.beforeApiId).isEqualTo(42);
        assertThat(delegate.afterApiId).isEqualTo(42);
        assertThat(delegate.afterResult).isEqualTo("r");
        assertThat(handled).isEmpty();
    }

    @Test
    public void generatedBlockClassIsReusedPerDelegateClass() {
        Interceptor first = factory.wrap(new RecordingBlockInterceptor(), guard);
        Interceptor second = factory.wrap(new RecordingBlockInterceptor(), guard);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first).isNotSameAs(second);
        assertThat(first.getClass()).isSameAs(second.getClass());
    }

    /**
     * The rules table looks non-void methods up by name, which is only unambiguous while every
     * Block shape keeps exactly one {@code before} (returning a reference) and one {@code after}.
     */
    @Test
    public void blockShapesDeclareOneReferenceReturningBeforeAndOneAfter() {
        List<Class<?>> blockShapes = Arrays.asList(
                BlockAroundInterceptor.class, BlockAroundInterceptor0.class, BlockAroundInterceptor1.class,
                BlockAroundInterceptor2.class, BlockAroundInterceptor3.class, BlockAroundInterceptor4.class,
                BlockAroundInterceptor5.class, BlockStaticAroundInterceptor.class, BlockApiIdAwareAroundInterceptor.class);
        for (Class<?> shape : blockShapes) {
            int before = 0;
            int after = 0;
            for (Method method : shape.getMethods()) {
                if (Modifier.isStatic(method.getModifiers()) || method.isDefault()) {
                    continue;
                }
                if (method.getName().equals("before")) {
                    assertThat(method.getReturnType()).as(shape.getName()).isEqualTo(TraceBlock.class);
                    before++;
                } else if (method.getName().equals("after")) {
                    assertThat(method.getReturnType()).as(shape.getName()).isEqualTo(void.class);
                    after++;
                } else {
                    throw new AssertionError(shape.getName() + " declares " + method);
                }
            }
            assertThat(before).as(shape.getName()).isEqualTo(1);
            assertThat(after).as(shape.getName()).isEqualTo(1);
        }
    }

    @Test
    public void multiShapeDelegateIsIneligible() {
        assertThat(factory.wrap(new MultiShapeInterceptor(), guard)).isNull();
    }

    @Test
    public void nonPublicDelegateIsIneligible() {
        assertThat(factory.wrap(new PackagePrivateInterceptor(), guard)).isNull();
    }

    @Test
    public void scopedDelegatesInsideScope() {
        InterceptorScope scope = mock(InterceptorScope.class);
        InterceptorScopeInvocation invocation = mock(InterceptorScopeInvocation.class);
        when(scope.getCurrentInvocation()).thenReturn(invocation);
        when(invocation.tryEnter(ExecutionPolicy.BOUNDARY)).thenReturn(true);
        when(invocation.canLeave(ExecutionPolicy.BOUNDARY)).thenReturn(true);

        RecordingAroundInterceptor delegate = new RecordingAroundInterceptor();
        Interceptor wrapped = factory.wrapScoped(delegate, scope, ExecutionPolicy.BOUNDARY, guard);

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
        Interceptor wrapped = factory.wrapScoped(delegate, scope, ExecutionPolicy.BOUNDARY, guard);

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

        Interceptor wrapped = factory.wrapScoped(new ThrowingAroundInterceptor(), scope, ExecutionPolicy.ALWAYS, guard);

        assertThat(wrapped).isNotNull();
        ((AroundInterceptor) wrapped).before(null, null);
        ((AroundInterceptor) wrapped).after(null, null, null, null);

        assertThat(handled).hasSize(2);
        verify(invocation).leave(ExecutionPolicy.ALWAYS);
    }

    @Test
    public void scopedGeneratedClassIsReusedPerDelegateClass() {
        InterceptorScope scope = mock(InterceptorScope.class);
        Interceptor first = factory.wrapScoped(new RecordingAroundInterceptor(), scope, ExecutionPolicy.BOUNDARY, guard);
        Interceptor second = factory.wrapScoped(new RecordingAroundInterceptor(), scope, ExecutionPolicy.BOUNDARY, guard);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first.getClass()).isSameAs(second.getClass());
    }

    @Test
    public void scopedResultReplaceBlockLeavesInFinallyAndReturnsOriginalResultWhenDelegateThrows() {
        InterceptorScope scope = mock(InterceptorScope.class);
        InterceptorScopeInvocation invocation = mock(InterceptorScopeInvocation.class);
        when(scope.getCurrentInvocation()).thenReturn(invocation);
        when(invocation.tryEnter(ExecutionPolicy.ALWAYS)).thenReturn(true);
        when(invocation.canLeave(ExecutionPolicy.ALWAYS)).thenReturn(true);

        Interceptor wrapped = factory.wrapScoped(new ThrowingResultReplaceBlockInterceptor(), scope, ExecutionPolicy.ALWAYS, guard);

        assertThat(wrapped).isNotNull().isInstanceOf(ResultReplaceBlockAroundInterceptor.class);
        assertThat(wrapped.getClass().getName()).contains("GuardedScopedInterceptor$$");

        Object original = new Object();
        TraceBlock block = ((ResultReplaceBlockAroundInterceptor) wrapped).before("t", Object.class, null);
        Object returned = ((ResultReplaceBlockAroundInterceptor) wrapped).after(block, "t", Object.class, null, original, null);

        assertThat(block).isNull();
        assertThat(returned).isSameAs(original);
        assertThat(handled).hasSize(2);
        verify(invocation).leave(ExecutionPolicy.ALWAYS);
    }

    @Test
    public void scopedBlockDelegatesInsideScopeAndLeavesInFinally() {
        InterceptorScope scope = mock(InterceptorScope.class);
        InterceptorScopeInvocation invocation = mock(InterceptorScopeInvocation.class);
        when(scope.getCurrentInvocation()).thenReturn(invocation);
        when(invocation.tryEnter(ExecutionPolicy.ALWAYS)).thenReturn(true);
        when(invocation.canLeave(ExecutionPolicy.ALWAYS)).thenReturn(true);

        RecordingBlockInterceptor delegate = new RecordingBlockInterceptor();
        Interceptor wrapped = factory.wrapScoped(delegate, scope, ExecutionPolicy.ALWAYS, guard);

        assertThat(wrapped).isNotNull().isInstanceOf(BlockAroundInterceptor.class);
        assertThat(wrapped.getClass().getName()).contains("GuardedScopedInterceptor$$");

        Object target = new Object();
        TraceBlock block = ((BlockAroundInterceptor) wrapped).before(target, null);
        assertThat(block).isSameAs(delegate.block);
        assertThat(delegate.beforeTarget).isSameAs(target);

        Interceptor throwing = factory.wrapScoped(new ThrowingBlockInterceptor(), scope, ExecutionPolicy.ALWAYS, guard);
        ((BlockAroundInterceptor) throwing).after(block, target, null, null, null);

        assertThat(handled).hasSize(1);
        assertThat(handled.get(0)).hasMessage("boom-after");
        verify(invocation).leave(ExecutionPolicy.ALWAYS);
    }

    @Test
    public void scopedBlockSkipsWhenScopeRejects() {
        InterceptorScope scope = mock(InterceptorScope.class);
        InterceptorScopeInvocation invocation = mock(InterceptorScopeInvocation.class);
        when(scope.getCurrentInvocation()).thenReturn(invocation);
        when(invocation.tryEnter(ExecutionPolicy.BOUNDARY)).thenReturn(false);
        when(invocation.canLeave(ExecutionPolicy.BOUNDARY)).thenReturn(false);

        RecordingBlockInterceptor delegate = new RecordingBlockInterceptor();
        Interceptor wrapped = factory.wrapScoped(delegate, scope, ExecutionPolicy.BOUNDARY, guard);

        assertThat(wrapped).isNotNull();
        TraceBlock block = ((BlockAroundInterceptor) wrapped).before(new Object(), null);
        ((BlockAroundInterceptor) wrapped).after(block, new Object(), null, null, null);

        assertThat(block).isNull();
        assertThat(delegate.beforeTarget).isNull();
        assertThat(delegate.afterBlock).isNull();
        verify(invocation, never()).leave(ExecutionPolicy.BOUNDARY);
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

    public static class ReplacingResultInterceptor implements ResultReplaceAroundInterceptor {
        final Object replacement = new Object();
        Object afterResult;

        @Override
        public void before(Object target, Class<?> returnType, Object[] args) {
        }

        @Override
        public Object after(Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
            this.afterResult = result;
            return replacement;
        }
    }

    public static class ThrowingResultReplaceInterceptor implements ResultReplaceAroundInterceptor {
        @Override
        public void before(Object target, Class<?> returnType, Object[] args) {
            throw new IllegalStateException("boom-before");
        }

        @Override
        public Object after(Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
            throw new IllegalStateException("boom-after");
        }
    }

    public static class ReplacingResultBlockInterceptor implements ResultReplaceBlockAroundInterceptor {
        final TraceBlock block = mock(TraceBlock.class);
        final Object replacement = new Object();
        TraceBlock afterBlock;

        @Override
        public TraceBlock before(Object target, Class<?> returnType, Object[] args) {
            return block;
        }

        @Override
        public Object after(TraceBlock block, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
            this.afterBlock = block;
            return replacement;
        }
    }

    public static class ThrowingResultReplaceBlockInterceptor implements ResultReplaceBlockAroundInterceptor {
        @Override
        public TraceBlock before(Object target, Class<?> returnType, Object[] args) {
            throw new IllegalStateException("boom-before");
        }

        @Override
        public Object after(TraceBlock block, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
            throw new IllegalStateException("boom-after");
        }
    }

    public static class RecordingBlockInterceptor implements BlockAroundInterceptor {
        final TraceBlock block = mock(TraceBlock.class);
        Object beforeTarget;
        Object[] beforeArgs;
        TraceBlock afterBlock;
        Object afterResult;

        @Override
        public TraceBlock before(Object target, Object[] args) {
            this.beforeTarget = target;
            this.beforeArgs = args;
            return block;
        }

        @Override
        public void after(TraceBlock block, Object target, Object[] args, Object result, Throwable throwable) {
            this.afterBlock = block;
            this.afterResult = result;
        }
    }

    public static class ThrowingBlockInterceptor implements BlockAroundInterceptor {
        @Override
        public TraceBlock before(Object target, Object[] args) {
            throw new IllegalStateException("boom-before");
        }

        @Override
        public void after(TraceBlock block, Object target, Object[] args, Object result, Throwable throwable) {
            throw new IllegalStateException("boom-after");
        }
    }

    public static class RecordingBlockApiIdAwareInterceptor implements BlockApiIdAwareAroundInterceptor {
        final TraceBlock block = mock(TraceBlock.class);
        int beforeApiId;
        int afterApiId;
        Object afterResult;

        @Override
        public TraceBlock before(Object target, int apiId, Object[] args) {
            this.beforeApiId = apiId;
            return block;
        }

        @Override
        public void after(TraceBlock block, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
            this.afterApiId = apiId;
            this.afterResult = result;
        }
    }

    /** A non-void shape the rules table does not know: must keep falling back to the shared wrapper. */
    public interface UnregisteredNonVoidShape extends Interceptor {
        Object probe(Object target);
    }

    public static class UnregisteredNonVoidInterceptor implements UnregisteredNonVoidShape {
        @Override
        public Object probe(Object target) {
            return target;
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
