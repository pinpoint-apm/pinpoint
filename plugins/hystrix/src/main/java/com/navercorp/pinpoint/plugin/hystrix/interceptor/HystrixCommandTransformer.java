/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.hystrix.interceptor;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.hystrix.HystrixPluginConstants;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jiaqi Feng
 */

public class HystrixCommandTransformer implements TransformCallback {
    private static final String SCOPE_NAME = "Hystrix";

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        InterceptorScope scope = instrumentor.getInterceptorScope(SCOPE_NAME);
        // TODO this could not printout in mvn integration test
        System.out.println("scope="+scope);

        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

        target.addField("com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor");

        InstrumentMethod constructor = target.getConstructor(
                "com.netflix.hystrix.HystrixCommandGroupKey",
                "com.netflix.hystrix.HystrixCommandKey",
                "com.netflix.hystrix.HystrixThreadPoolKey",
                "com.netflix.hystrix.HystrixCircuitBreaker",
                "com.netflix.hystrix.HystrixThreadPool",
                "com.netflix.hystrix.HystrixCommandProperties$Setter",
                "com.netflix.hystrix.HystrixThreadPoolProperties$Setter",
                "com.netflix.hystrix.HystrixCommandMetrics",
                "com.netflix.hystrix.HystrixCommand$TryableSemaphore",
                "com.netflix.hystrix.HystrixCommand$TryableSemaphore",
                "com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy",
                "com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook");

        constructor.addScopedInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandConstructorInterceptor",
                scope,
                ExecutionPolicy.INTERNAL);

        InstrumentMethod execute = target.getDeclaredMethod("execute");
        execute.addScopedInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandExecuteInterceptor", scope);

        InstrumentMethod executeCommand = target.getDeclaredMethod("executeCommand");
        executeCommand.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandExecuteCommandInterceptor");

        return target.toBytecode();
    }
}
