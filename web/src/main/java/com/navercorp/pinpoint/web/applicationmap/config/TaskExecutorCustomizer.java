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

package com.navercorp.pinpoint.web.applicationmap.config;

import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ExecutorProperties;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class TaskExecutorCustomizer implements ExecutorCustomizer<ThreadPoolTaskExecutor> {

    private final TaskDecorator taskDecorator;

    public TaskExecutorCustomizer(TaskDecorator taskDecorator) {
        this.taskDecorator = taskDecorator;
    }


    @Override
    public void customize(ThreadPoolTaskExecutor executor, ExecutorProperties properties) {

        if (taskDecorator != null) {
            executor.setTaskDecorator(taskDecorator);
        }

        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());

        executor.setPrestartAllCoreThreads(properties.isPrestartAllCoreThreads());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.setPrestartAllCoreThreads(properties.isPrestartAllCoreThreads());
        executor.setDaemon(properties.isDaemon());

        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        executor.setWaitForTasksToCompleteOnShutdown(properties.isWaitForTasksToCompleteOnShutdown());
        executor.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());

    }
}
