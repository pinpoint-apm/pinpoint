/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.instrument.classloading;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandler;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.profiler.instrument.ASMGuardedInterceptorFactory;
import com.navercorp.pinpoint.profiler.logging.Log4j2LoggerBinderInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the interceptor guard codegen end-to-end (ASM emit + DefineClassFactory define + invoke)
 * on the JVM executing this test. The profiler module's own unit suite covers the guard
 * semantics, but its default fork is a JDK 8 JVM, so the JDK 9+ define path
 * ({@link Java9DefineClass}) is never exercised there - this module is the only unit-level
 * classpath that has it. Semantics are asserted only as far as needed to prove the generated
 * class actually ran.
 *
 * Enabled only under the {@code jdk9-define-test} profile: the JDK 9+ define path reaches
 * jdk.internal SharedSecrets, which a plain test JVM cannot access without the profile's
 * add-exports flags (the agent opens them itself at runtime via its module support).
 */
@EnabledIfSystemProperty(named = "pinpoint.jdk9.define.test", matches = "true")
public class GuardCodegenJdk9DefineTest {

    private final List<Throwable> handled = new ArrayList<>();
    private final ExceptionHandler guard = handled::add;

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
    public void emittedGuardIsDefinedAndRuns() {
        RecordingAroundInterceptor delegate = new RecordingAroundInterceptor();
        Interceptor wrapped = ASMGuardedInterceptorFactory.wrap(delegate, guard);

        // non-null proves emit + define succeeded on this JVM (every failure falls back to null)
        assertThat(wrapped).isNotNull().isInstanceOf(AroundInterceptor.class);
        assertThat(wrapped.getClass().getName()).contains("GuardedInterceptor$$");

        Object target = new Object();
        Object[] args = {"a", 1};
        ((AroundInterceptor) wrapped).before(target, args);
        ((AroundInterceptor) wrapped).after(target, args, "result", null);

        assertThat(delegate.beforeTarget).isSameAs(target);
        assertThat(delegate.afterResult).isEqualTo("result");
        assertThat(handled).isEmpty();
    }

    @Test
    public void definedGuardStillSwallowsThrowable() {
        Interceptor wrapped = ASMGuardedInterceptorFactory.wrap(new ThrowingAroundInterceptor(), guard);

        assertThat(wrapped).isNotNull();
        ((AroundInterceptor) wrapped).before(null, null);
        ((AroundInterceptor) wrapped).after(null, null, null, null);

        assertThat(handled).hasSize(2);
        assertThat(handled.get(0)).hasMessage("boom-before");
        assertThat(handled.get(1)).hasMessage("boom-after");
    }

    @Test
    public void rewrittenScopedTemplateIsDefinedAndRuns() {
        RecordingScope scope = new RecordingScope();

        RecordingAroundInterceptor delegate = new RecordingAroundInterceptor();
        Interceptor wrapped = ASMGuardedInterceptorFactory.wrapScoped(delegate, scope, ExecutionPolicy.BOUNDARY, guard);

        assertThat(wrapped).isNotNull().isInstanceOf(AroundInterceptor.class);
        assertThat(wrapped.getClass().getName()).contains("GuardedScopedInterceptor$$");

        Object target = new Object();
        ((AroundInterceptor) wrapped).before(target, null);
        ((AroundInterceptor) wrapped).after(target, null, "result", null);

        assertThat(delegate.beforeTarget).isSameAs(target);
        assertThat(delegate.afterResult).isEqualTo("result");
        // the scope is entered in before and left once in after
        assertThat(scope.leaveCount).isEqualTo(1);
        assertThat(handled).isEmpty();
    }

    public static class RecordingAroundInterceptor implements AroundInterceptor {
        Object beforeTarget;
        Object afterResult;

        @Override
        public void before(Object target, Object[] args) {
            this.beforeTarget = target;
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

    private static class RecordingScope implements InterceptorScope {
        int leaveCount;
        private final InterceptorScopeInvocation invocation = new InterceptorScopeInvocation() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public boolean tryEnter(ExecutionPolicy policy) {
                return true;
            }

            @Override
            public void leave(ExecutionPolicy policy) {
                leaveCount++;
            }

            @Override
            public boolean canLeave(ExecutionPolicy policy) {
                return true;
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public Object setAttachment(Object attachment) {
                return null;
            }

            @Override
            public Object getAttachment() {
                return null;
            }

            @Override
            public Object getOrCreateAttachment(com.navercorp.pinpoint.bootstrap.interceptor.scope.AttachmentFactory factory) {
                return null;
            }

            @Override
            public Object removeAttachment() {
                return null;
            }
        };

        @Override
        public String getName() {
            return "test-scope";
        }

        @Override
        public InterceptorScopeInvocation getCurrentInvocation() {
            return invocation;
        }
    }
}
