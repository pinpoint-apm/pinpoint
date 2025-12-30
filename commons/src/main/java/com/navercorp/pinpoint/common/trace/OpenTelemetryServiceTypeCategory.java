/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

public class OpenTelemetryServiceTypeCategory {
    public static boolean isServer(int code) {
        return ServiceType.OPENTELEMETY_SERVER.getCode() == code;
    }

    public static boolean isClient(int code) {
        return ServiceType.OPENTELEMETY_CLIENT.getCode() == code;
    }

    public static boolean isInternal(int code) {
        return ServiceType.OPENTELEMETY_INTERNAL.getCode() == code;
    }

    public static boolean contains(int code) {
        return isServer(code) || isClient(code) || isInternal(code);
    }

    public static boolean contains(ServiceType type) {
        return contains(type.getCode());
    }
}
