/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.util;


import java.lang.reflect.Array;
import java.util.concurrent.CompletableFuture;

public final class FutureUtils {

    public static <T> T[] allJoin(CompletableFuture<T>[] futures, Class<T> joinClass) {
        @SuppressWarnings("unchecked")
        final T[] result = (T[]) Array.newInstance(joinClass, futures.length);
        for (int i = 0; i < futures.length; i++) {
            final CompletableFuture<T> future = futures[i];
            result[i] = future.join();
        }
        return result;
    }
}
