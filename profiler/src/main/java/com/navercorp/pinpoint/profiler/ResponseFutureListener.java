/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ResponseFutureListener<T, U  extends Throwable> implements BiConsumer<T, U> {

    private final CompletableFuture<T> future;

    public ResponseFutureListener() {
        this.future = new CompletableFuture<>();
    }

    @Override
    public void accept(T message, U th) {
        if (message != null) {
            future.complete(message);
        } else {
            future.completeExceptionally(th);
        }
    }

    public CompletableFuture<T> getResponseFuture() {
        return future;
    }
}
