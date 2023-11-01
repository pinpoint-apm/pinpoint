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

package com.navercorp.pinpoint.common.server.thread;

import com.navercorp.pinpoint.common.server.executor.ExecutorProperties;
import jakarta.validation.constraints.Positive;

public class MonitoringExecutorProperties extends ExecutorProperties {

    protected boolean monitorEnable;

    @Positive
    protected int logRate = 100;

    public boolean isMonitorEnable() {
        return monitorEnable;
    }

    public void setMonitorEnable(boolean monitorEnable) {
        this.monitorEnable = monitorEnable;
    }

    public int getLogRate() {
        return logRate;
    }

    public void setLogRate(int logRate) {
        this.logRate = logRate;
    }

    @Override
    public String toString() {
        return "MonitoringExecutorProperties{" +
                "monitorEnable=" + monitorEnable +
                ", logRate=" + logRate +
                ", corePoolSize=" + corePoolSize +
                ", maxPoolSize=" + maxPoolSize +
                ", keepAliveSeconds=" + keepAliveSeconds +
                ", prestartAllCoreThreads=" + prestartAllCoreThreads +
                ", queueCapacity=" + queueCapacity +
                ", threadNamePrefix='" + threadNamePrefix + '\'' +
                ", daemon=" + daemon +
                ", waitForTasksToCompleteOnShutdown=" + waitForTasksToCompleteOnShutdown +
                ", awaitTerminationSeconds=" + awaitTerminationSeconds +
                '}';
    }
}
