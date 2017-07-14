/*
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
package com.navercorp.pinpoint.plugin.hystrix.transformer;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.plugin.hystrix.HystrixPluginConstants;

/**
 * @author Jiaqi Feng
 */

public class HystrixCommandTransformer implements TransformCallback {

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

        target.addField("com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor");

        InstrumentMethod queue = target.getDeclaredMethod("queue");
        if (queue != null) {
            queue.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandQueueInterceptor");
        }

        // pre 1.4.0 - R executeCommand()
        InstrumentMethod executeCommand = target.getDeclaredMethod("executeCommand");
        if (executeCommand != null) {
            executeCommand.addScopedInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandExecuteCommandInterceptor", HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE);
        }
        // pre 1.4.0 - R getFallbackOrThrowException(HystrixEventType, FailureType, String, Exception)
        InstrumentMethod getFallbackOrThrowException = target.getDeclaredMethod(
                "getFallbackOrThrowException",
                "com.netflix.hystrix.HystrixEventType",
                "com.netflix.hystrix.exception.HystrixRuntimeException$FailureType",
                "java.lang.String",
                "java.lang.Exception");
        if (getFallbackOrThrowException != null) {
            getFallbackOrThrowException.addScopedInterceptor(
                    "com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandGetFallbackOrThrowExceptionInterceptor",
                    HystrixPluginConstants.HYSTRIX_COMMAND_EXECUTION_SCOPE,
                    ExecutionPolicy.INTERNAL);
        }

        return target.toBytecode();
    }
}
