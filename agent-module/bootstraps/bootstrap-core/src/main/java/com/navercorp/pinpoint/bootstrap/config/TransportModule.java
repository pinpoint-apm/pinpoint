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

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public enum TransportModule {
    GRPC;

    public static TransportModule parse(String transportModule) {
        Objects.requireNonNull(transportModule, "transportModule");

        if (equalsIgnoreCase(TransportModule.GRPC.name(), transportModule)) {
            return GRPC;
        }
        return GRPC;
    }

    private static boolean equalsIgnoreCase(String str1, String str2) {
        return str1.equalsIgnoreCase(str2);
    }

    public static TransportModule parse(String transportModule, TransportModule defaultModule) {
        Objects.requireNonNull(transportModule, "transportModule");
        Objects.requireNonNull(defaultModule, "defaultModule");

        final TransportModule resolvedModule = parse(transportModule);
        if (resolvedModule == null) {
            return defaultModule;
        }
        return resolvedModule;
    }
}
