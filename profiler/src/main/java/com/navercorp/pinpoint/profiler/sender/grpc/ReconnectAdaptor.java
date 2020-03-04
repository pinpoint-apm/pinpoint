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

import java.util.concurrent.Executor;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ReconnectAdaptor implements Reconnector {
    private final Executor executor;
    private final ReconnectJob reconnectJob;

    public ReconnectAdaptor(Executor executor, ReconnectJob reconnectJob) {
        this.executor = Assert.requireNonNull(executor, "executor");
        this.reconnectJob = Assert.requireNonNull(reconnectJob, "reconnectJob");
    }


    @Override
    public void reset() {
        reconnectJob.resetBackoffNanos();
    }

    @Override
    public void reconnect() {
        executor.execute(reconnectJob);
    }
}
