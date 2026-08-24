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


package com.navercorp.pinpoint.plugin.ktor.client;

final class KtorClientCoroutineSuspendedMarker {
    private static final String COROUTINE_SUSPENDED_CLASS = "kotlin.coroutines.intrinsics.CoroutineSingletons";
    private static final String COROUTINE_SUSPENDED_NAME = "COROUTINE_SUSPENDED";

    private KtorClientCoroutineSuspendedMarker() {
    }

    static boolean isSuspended(Object result) {
        if (result == null) {
            return false;
        }

        if (!COROUTINE_SUSPENDED_CLASS.equals(result.getClass().getName())) {
            return false;
        }

        if (result instanceof Enum) {
            return COROUTINE_SUSPENDED_NAME.equals(((Enum<?>) result).name());
        }

        return COROUTINE_SUSPENDED_NAME.equals(String.valueOf(result));
    }
}
