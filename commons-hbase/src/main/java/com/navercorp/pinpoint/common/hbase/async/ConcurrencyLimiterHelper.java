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

package com.navercorp.pinpoint.common.hbase.async;

import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

public class ConcurrencyLimiterHelper implements LimiterHelper {
    private final Semaphore semaphore;
    private final long maxCurrent;

    public ConcurrencyLimiterHelper(int maxCurrent) {
        this.semaphore = new Semaphore(maxCurrent);
        this.maxCurrent = maxCurrent;
    }

    @Override
    public boolean acquire(int permits) {
        return semaphore.tryAcquire(permits);
    }

    public long count() {
        return maxCurrent - semaphore.availablePermits();
    }


    @Override
    public void release(int permits) {
        semaphore.release(permits);
    }


    @Override
    public <R> BiConsumer<R, Throwable> release() {
        return new Release<>(semaphore);
    }

    private static class Release<R> implements BiConsumer<R, Throwable> {
        private final Semaphore counter;
        public Release(Semaphore counter) {
            this.counter = counter;
        }

        @Override
        public void accept(R result, Throwable throwable) {
            counter.release();
        }
    }

    @Override
    public String toString() {
        return "ConcurrencyLimiterHelper{" +
                "counter=" + semaphore +
                '}';
    }
}
