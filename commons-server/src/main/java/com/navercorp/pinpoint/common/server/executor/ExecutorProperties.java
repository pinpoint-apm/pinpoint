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

package com.navercorp.pinpoint.common.server.executor;

import com.navercorp.pinpoint.common.util.CpuUtils;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class ExecutorProperties {

    @PositiveOrZero
    protected int corePoolSize = CpuUtils.cpuCount();

    @Positive
    protected int maxPoolSize = CpuUtils.workerCount();

    @PositiveOrZero
    protected int keepAliveSeconds = 60;

    protected boolean prestartAllCoreThreads = true;

    @PositiveOrZero
    protected int queueCapacity = 1024 * 10;

    protected String threadNamePrefix;

    protected boolean daemon = true;

    protected boolean waitForTasksToCompleteOnShutdown = false;

    @PositiveOrZero
    protected int awaitTerminationSeconds = 0;

    public ExecutorProperties() {
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }


    public boolean isPrestartAllCoreThreads() {
        return prestartAllCoreThreads;
    }

    public void setPrestartAllCoreThreads(boolean prestartAllCoreThreads) {
        this.prestartAllCoreThreads = prestartAllCoreThreads;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public boolean isWaitForTasksToCompleteOnShutdown() {
        return waitForTasksToCompleteOnShutdown;
    }

    public void setWaitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
    }

    public int getAwaitTerminationSeconds() {
        return awaitTerminationSeconds;
    }

    public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
        this.awaitTerminationSeconds = awaitTerminationSeconds;
    }


    @Override
    public String toString() {
        return "ExecutorProperties{" +
                "corePoolSize=" + corePoolSize +
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
