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

package com.navercorp.pinpoint.profiler.context.provider.grpc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ReconnectExecutorProvider implements Provider<ReconnectExecutor> {
    private final ScheduledExecutorService scheduledExecutorService;

    @Inject
    public ReconnectExecutorProvider(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = Assert.requireNonNull(scheduledExecutorService, "scheduledExecutorService");
    }

    @Override
    public ReconnectExecutor get() {
        // TODO custom timeout~~
        return new ReconnectExecutor(scheduledExecutorService);
    }
}
