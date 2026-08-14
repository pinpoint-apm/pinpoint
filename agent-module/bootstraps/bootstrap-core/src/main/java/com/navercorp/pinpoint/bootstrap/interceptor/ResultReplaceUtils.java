/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.interceptor;

/**
 * Invoked from code woven for {@link ResultReplaceAroundInterceptor}. Selecting the value here in
 * plain Java keeps the woven bytecode branch-free: the weaver emits a single
 * {@code INVOKESTATIC replace} followed by a {@code CHECKCAST} to the method's declared return
 * type, which is guaranteed to succeed for either selected value.
 */
public final class ResultReplaceUtils {

    private ResultReplaceUtils() {
    }

    /**
     * Returns {@code replacement} when it can safely stand in for the original return value,
     * otherwise the original {@code result}. A null or type-incompatible replacement silently
     * keeps the original so an interceptor bug degrades to a no-op instead of a
     * {@code ClassCastException} inside the application.
     */
    public static Object replace(Object result, Object replacement, Class<?> returnType) {
        if (returnType != null && returnType.isInstance(replacement)) {
            return replacement;
        }
        return result;
    }
}
