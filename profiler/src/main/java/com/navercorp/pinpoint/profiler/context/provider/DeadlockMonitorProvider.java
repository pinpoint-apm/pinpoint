/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.monitor.DeadlockMonitor;
import com.navercorp.pinpoint.profiler.monitor.DeadlockThreadRegistry;
import com.navercorp.pinpoint.profiler.monitor.DefaultDeadlockMonitor;
import com.navercorp.pinpoint.profiler.monitor.DisabledDeadlockMonitor;

/**
 * @author Taejin Koo
 */
public class DeadlockMonitorProvider implements Provider<DeadlockMonitor> {

    private final ProfilerConfig profilerConfig;
    private final DeadlockThreadRegistry deadlockThreadRegistry;

    @Inject
    public DeadlockMonitorProvider(ProfilerConfig profilerConfig, DeadlockThreadRegistry deadlockThreadRegistry) {
        this.profilerConfig = profilerConfig;
        this.deadlockThreadRegistry = deadlockThreadRegistry;
    }

    @Override
    public DeadlockMonitor get() {
        if (profilerConfig.isDeadlockMonitorEnable()) {
            return new DefaultDeadlockMonitor(deadlockThreadRegistry, profilerConfig.getDeadlockMonitorInterval());
        } else {
            return new DisabledDeadlockMonitor();
        }
    }

}
