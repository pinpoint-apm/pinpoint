/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.config;

import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Woonduk Kang(emeroad)
 */
public enum TransportModule {
    THRIFT,
    GRPC;

    public static TransportModule parse(String transportModule) {
        Assert.requireNonNull(transportModule, "transportModule");

        if (isEquals(THRIFT, transportModule)) {
            return THRIFT;
        }
        if (isEquals(GRPC, transportModule)) {
            return GRPC;
        }
        return null;
    }

    private static boolean isEquals(TransportModule transportModule, String transportModuleString) {
        final String transportModuleName = transportModule.name();
        return transportModuleName.equalsIgnoreCase(transportModuleString);
    }

    public static TransportModule parse(String transportModule, TransportModule defaultModule) {
        Assert.requireNonNull(transportModule, "transportModule");
        Assert.requireNonNull(defaultModule, "defaultModule");

        final TransportModule resolvedModule = parse(transportModule);
        if (resolvedModule == null) {
            return defaultModule;
        }
        return resolvedModule;
    }
}
