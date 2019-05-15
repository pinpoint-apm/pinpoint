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

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.common.util.Assert;

import io.grpc.internal.ExponentialBackoffPolicy;

import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class ExponentialBackoffReconnectJob implements ReconnectJob {

    private final long maxBackOffNanos;

    private volatile ExponentialBackoffPolicy exponentialBackoffPolicy = new ExponentialBackoffPolicy();

    public ExponentialBackoffReconnectJob() {
        this(TimeUnit.SECONDS.toNanos(30));
    }

    public ExponentialBackoffReconnectJob(long maxBackOffNanos) {
        Assert.isTrue(maxBackOffNanos > 0, "maxBackOffNanos > 0");

        if (TimeUnit.SECONDS.toNanos(3) > maxBackOffNanos) {
            this.maxBackOffNanos = TimeUnit.SECONDS.toNanos(3);
        } else {
            this.maxBackOffNanos = maxBackOffNanos;
        }
    }

    @Override
    public final void resetBackoffNanos() {
        exponentialBackoffPolicy = new ExponentialBackoffPolicy();
    }

    @Override
    public long nextBackoffNanos() {
        return Math.min(exponentialBackoffPolicy.nextBackoffNanos(), maxBackOffNanos);
    }

    @Override
    public void run() {

    }
}
