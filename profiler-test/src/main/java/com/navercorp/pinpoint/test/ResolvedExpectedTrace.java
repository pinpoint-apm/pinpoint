/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedAnnotation;
import com.navercorp.pinpoint.bootstrap.plugin.test.ExpectedTraceField;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.DefaultLocalAsyncId;
import com.navercorp.pinpoint.profiler.context.LocalAsyncId;

import java.util.Arrays;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class ResolvedExpectedTrace {
    final Class<?> type;
    final ServiceType serviceType;
    final LocalAsyncId localAsyncId;
    final Integer apiId;
    final Exception exception;
    final ExpectedTraceField rpc;
    final ExpectedTraceField endPoint;
    final ExpectedTraceField remoteAddr;
    final ExpectedTraceField destinationId;
    final ExpectedAnnotation[] annotations;

    public ResolvedExpectedTrace(Class<?> type, ServiceType serviceType, Integer apiId, Exception exception, ExpectedTraceField rpc, ExpectedTraceField endPoint, ExpectedTraceField remoteAddr, ExpectedTraceField destinationId, ExpectedAnnotation[] annotations, Integer asyncId) {
        this.type = type;
        this.serviceType = serviceType;
        this.apiId = apiId;
        this.exception = exception;
        this.rpc = rpc;
        this.endPoint = endPoint;
        this.remoteAddr = remoteAddr;
        this.destinationId = destinationId;
        this.annotations = annotations;
        this.localAsyncId = newLocalAsyncId(asyncId);
    }

    private LocalAsyncId newLocalAsyncId(Integer asyncId) {
        if (asyncId == null) {
            return null;
        }
        return new DefaultLocalAsyncId(asyncId, (short) 0);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(type.getSimpleName());
        builder.append("(serviceType: ");
        builder.append(serviceType.getCode());
        builder.append(", apiId: ");
        builder.append(apiId);
        builder.append(", exception: ");
        builder.append(exception);
        builder.append(", rpc: ");
        builder.append(rpc);
        builder.append(", endPoint: ");
        builder.append(endPoint);
        builder.append(", remoteAddr: ");
        builder.append(remoteAddr);
        builder.append(", destinationId: ");
        builder.append(destinationId);
        builder.append(", annotations: ");
        builder.append(Arrays.deepToString(annotations));
        builder.append(", localAsyncId: ");
        builder.append(localAsyncId);
        builder.append(")");

        return builder.toString();
    }
}
