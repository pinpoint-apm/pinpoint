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

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

        target.addField("com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor");

        InstrumentMethod queue = target.getDeclaredMethod("queue");
        queue.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandQueueInterceptor");

        InstrumentMethod executeCommand = target.getDeclaredMethod("executeCommand");
        executeCommand.addInterceptor("com.navercorp.pinpoint.plugin.hystrix.interceptor.HystrixCommandExecuteCommandInterceptor");

        return target.toBytecode();
    }
}
