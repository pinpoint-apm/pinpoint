/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.spring.async;

import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class AsyncService {

    private final CountDownLatch latch = new CountDownLatch(1);

    @Async
    public CompletableFuture<String> completable(String input) {
        return CompletableFuture.completedFuture(input + ":done");
    }

    @Async
    public Future<String> future(String input) {
        return CompletableFuture.completedFuture(input + ":done");
    }

    @Async
    public void fireAndForget(String input) {
        latch.countDown();
    }

    public CountDownLatch latch() {
        return latch;
    }
}
