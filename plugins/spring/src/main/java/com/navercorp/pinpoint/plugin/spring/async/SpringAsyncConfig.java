/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.async;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jaehong.kim
 */
public class SpringAsyncConfig {
    private static final String[] DEFAULT_ASYNC_TASK_EXECUTOR = {
            "org.springframework.scheduling.concurrent.ConcurrentTaskExecutor",
            "org.springframework.core.task.SimpleAsyncTaskExecutor",
            "org.springframework.scheduling.quartz.SimpleThreadPoolTaskExecutor",
            "org.springframework.core.task.support.TaskExecutorAdapter",
            "org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor",
            "org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler",
            "org.springframework.jca.work.WorkManagerTaskExecutor",
            "org.springframework.scheduling.commonj.WorkManagerTaskExecutor"
    };

    private static final String[] DEFAULT_ASYNC_TASK = {
            "org.springframework.aop.interceptor.AsyncExecutionInterceptor$1",
            "org.springframework.aop.interceptor.AsyncExecutionInterceptor$$Lambda$" // for spring framework 5.0 or later.
    };

    private final Set<String> asyncTaskExecutorClassNameList = new HashSet<>(Arrays.asList(DEFAULT_ASYNC_TASK_EXECUTOR));

    private final Set<String> asyncTaskClassNameList = new HashSet<>(Arrays.asList(DEFAULT_ASYNC_TASK));

    private final boolean enable;

    public SpringAsyncConfig(ProfilerConfig config) {
        this.enable = config.readBoolean("profiler.spring.async.enable", true);

        final List<String> listExecutor = config.readList("profiler.spring.async.executor.class.names");
        this.asyncTaskExecutorClassNameList.addAll(listExecutor);

        final List<String> listTask = config.readList("profiler.spring.async.task.class.names");
        this.asyncTaskClassNameList.addAll(listTask);
    }

    public Set<String> getAsyncTaskExecutorClassNameList() {
        return asyncTaskExecutorClassNameList;
    }

    public Set<String> getAsyncTaskClassNameList() {
        return asyncTaskClassNameList;
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpringAsyncConfig{");
        sb.append("enable=").append(enable);
        sb.append(",asyncTaskClassNameList=").append(asyncTaskClassNameList);
        sb.append(",asyncTaskExecutorClassNameList=").append(asyncTaskExecutorClassNameList);
        sb.append('}');
        return sb.toString();
    }
}
