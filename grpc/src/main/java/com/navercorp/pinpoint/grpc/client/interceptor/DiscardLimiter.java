/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.client.interceptor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jaehong.kim
 */
public class DiscardLimiter {
    private final AtomicBoolean isDone = new AtomicBoolean(Boolean.FALSE);
    private final AtomicBoolean isReadyState = new AtomicBoolean(Boolean.TRUE);
    private final AtomicLong discardCounter = new AtomicLong();
    private final AtomicBoolean reachDiscardCountForReconnect = new AtomicBoolean(Boolean.FALSE);
    private final AtomicBoolean reachNotReadyTimeout = new AtomicBoolean(Boolean.FALSE);
    private final long discardCountForReconnect;
    private final long notReadyTimeoutMillis;

    private final AtomicLong notReadyStartTimeMillis = new AtomicLong();

    public DiscardLimiter(long discardCountForReconnect, long notReadyTimeoutMillis) {
        this.discardCountForReconnect = discardCountForReconnect;
        this.notReadyTimeoutMillis = notReadyTimeoutMillis;
    }

    public void reset() {
        this.isDone.set(Boolean.FALSE);
        this.isReadyState.set(Boolean.TRUE);
        this.discardCounter.set(0);
        this.reachDiscardCountForReconnect.set(Boolean.FALSE);
        this.reachNotReadyTimeout.set(Boolean.FALSE);
        this.notReadyStartTimeMillis.set(System.currentTimeMillis());
    }

    public void discard(final boolean ready) throws DiscardLimiterException {
        if (ready) {
            if (this.isReadyState.compareAndSet(Boolean.FALSE, Boolean.TRUE)) {
                reset();
            }
            return;
        }

        if (this.isDone.get()) {
            // Done
            return;
        }

        if (this.isReadyState.compareAndSet(Boolean.TRUE, Boolean.FALSE)) {
            this.notReadyStartTimeMillis.set(System.currentTimeMillis());
        }

        if (checkDiscardCountForReconnect() && checkNotReadyTimeout()) {
            this.isDone.set(Boolean.TRUE);
            final long durationTimeMillis = System.currentTimeMillis() - this.notReadyStartTimeMillis.get();
            throw new DiscardLimiterException(this.discardCounter.get(), this.discardCountForReconnect, durationTimeMillis, this.notReadyTimeoutMillis);
        }
    }

    boolean checkDiscardCountForReconnect() {
        final long discardCount = this.discardCounter.incrementAndGet();
        if (this.reachDiscardCountForReconnect.get()) {
            // Reach the limit
            return true;
        }

        if (discardCount > this.discardCountForReconnect) {
            this.reachDiscardCountForReconnect.set(Boolean.TRUE);
            return true;
        }
        return false;
    }

    boolean checkNotReadyTimeout() {
        if (this.reachNotReadyTimeout.get()) {
            // Reach the limit
            return true;
        }

        final long notReadyDurationTimeMillis = System.currentTimeMillis() - this.notReadyStartTimeMillis.get();
        if (notReadyDurationTimeMillis > this.notReadyTimeoutMillis) {
            this.reachNotReadyTimeout.set(Boolean.TRUE);
            return true;
        }
        return false;
    }

    static class DiscardLimiterException extends Exception {
        public DiscardLimiterException(long discardCount, long discardCountForReconnect, long durationTimeMillis, long timeoutMillis) {
            super("reach the limit, discard=" + discardCount + "/" + discardCountForReconnect + ", duration=" + durationTimeMillis + "ms, timeout=" + timeoutMillis + "ms");
        }
    }
}
