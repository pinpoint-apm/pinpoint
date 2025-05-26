/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.grpc.interceptor.client;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.plugin.grpc.GrpcConstants;
import com.navercorp.pinpoint.plugin.grpc.field.accessor.MethodNameAccessor;
import com.navercorp.pinpoint.plugin.grpc.field.accessor.RemoteAddressAccessor;

import java.util.Objects;

public class GrpcClientRequestWrapper implements ClientRequestWrapper {

    private final Object target;

    public GrpcClientRequestWrapper(final Object target) {
        this.target = Objects.requireNonNull(target, "target");
    }

    @Override
    public String getDestinationId() {
        try {
            return getEndPoint(target);
        } catch (Exception ignored) {
        }

        return "UNKNOWN";
    }

    @Override
    public String getUrl() {
        try {
            String remoteAddress = getEndPoint(target);
            String methodName = getMethodName(target);
            return combineAddressAndMethodName(remoteAddress, methodName);
        } catch (Exception ignored) {
        }

        return null;
    }

    private String combineAddressAndMethodName(String remoteAddress, String methodName) {
        Objects.requireNonNull(remoteAddress, "remoteAddress");
        Objects.requireNonNull(methodName, "methodName");

        if (remoteAddress.startsWith("http")) {
            return remoteAddress + "/" + methodName;
        } else {
            return "http://" + remoteAddress + "/" + methodName;
        }
    }

    private String getMethodName(Object target) {
        if (target instanceof MethodNameAccessor) {
            String methodName = ((MethodNameAccessor) target)._$PINPOINT$_getMethodName();
            if (methodName != null) {
                return methodName;
            }
        }
        return GrpcConstants.UNKNOWN_METHOD;
    }

    public static String getEndPoint(Object target) {
        if (target instanceof RemoteAddressAccessor) {
            String remoteAddress = ((RemoteAddressAccessor) target)._$PINPOINT$_getRemoteAddress();
            if (remoteAddress != null) {
                return remoteAddress;
            }
        }
        return GrpcConstants.UNKNOWN_ADDRESS;
    }
}
