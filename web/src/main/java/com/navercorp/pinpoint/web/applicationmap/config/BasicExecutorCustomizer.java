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

import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class BasicExecutorCustomizer implements ExecutorCustomizer {

    private final TaskDecorator taskDecorator;

    public BasicExecutorCustomizer(TaskDecorator taskDecorator) {
        this.taskDecorator = taskDecorator;
    }

    public void applyDefaultConfiguration(ThreadPoolTaskExecutor executor) {
        if (taskDecorator != null) {
            executor.setTaskDecorator(taskDecorator);
        }
        executor.setDaemon(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
    }

    @Override
    public void customize(ThreadPoolTaskExecutor executor, ExecutorProperties executorProperties) {

        applyDefaultConfiguration(executor);

        executor.setCorePoolSize(executorProperties.getThreadSize());
        executor.setMaxPoolSize(executorProperties.getThreadSize());
        executor.setQueueCapacity(executorProperties.getQueueSize());
    }
}
