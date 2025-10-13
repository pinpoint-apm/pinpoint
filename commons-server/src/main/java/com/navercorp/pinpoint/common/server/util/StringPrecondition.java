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

package com.navercorp.pinpoint.common.server.util;

import java.util.function.Supplier;

public final class StringPrecondition {
    private StringPrecondition() {
    }

    public static String requireHasLength(String str, String message) {
        if (hasLength(str)) {
            return str;
        }
        throw new IllegalArgumentException(message);
    }

    public static String requireHasLength(String str, Supplier<String> messageSupplier) {
        if (hasLength(str)) {
            return str;
        }
        throw new IllegalArgumentException(getMessage(messageSupplier));
    }

    public static String requireHasText(String str, String message) {
        if (hasText(str)) {
            return str;
        }
        throw new IllegalArgumentException(message);
    }

    public static String requireHasText(String str, Supplier<String> messageSupplier) {
        if (hasText(str)) {
            return str;
        }
        throw new IllegalArgumentException(getMessage(messageSupplier));
    }


    private static String getMessage(Supplier<String> messageSupplier) {
        if (messageSupplier == null) {
            return null;
        }
        return messageSupplier.get();
    }

    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}

