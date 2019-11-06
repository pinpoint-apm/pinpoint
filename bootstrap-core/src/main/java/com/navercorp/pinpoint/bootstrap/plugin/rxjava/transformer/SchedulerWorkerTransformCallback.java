/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.rxjava.transformer;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.rxjava.RxJavaScopes;
import com.navercorp.pinpoint.bootstrap.plugin.rxjava.interceptor.WorkerScheduleInterceptor;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author HyunGil Jeong
 */
public class SchedulerWorkerTransformCallback implements TransformCallback {

    private final ServiceType serviceType;

    public SchedulerWorkerTransformCallback(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public static SchedulerWorkerTransformCallback createFor(ServiceType serviceType) {
        return new SchedulerWorkerTransformCallback(serviceType);
    }

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
        // transform schedule(...) methods
        for (InstrumentMethod schedule : target.getDeclaredMethods(MethodFilters.chain(
                MethodFilters.name("schedule"), MethodFilters.argAt(0, "rx.functions.Action0")))) {
            if (schedule != null) {
                schedule.addScopedInterceptor(WorkerScheduleInterceptor.class, va(serviceType), RxJavaScopes.SCHEDULER_WORKER_SCOPE, ExecutionPolicy.BOUNDARY);
            }
        }
        return target.toBytecode();
    }
}
