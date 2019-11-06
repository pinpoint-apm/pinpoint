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

package com.navercorp.pinpoint.web.task;

import org.springframework.core.task.TaskDecorator;

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class ChainedTaskDecorator implements TaskDecorator {

    private final List<TaskDecorator> taskDecorators;

    public ChainedTaskDecorator(List<TaskDecorator> taskDecorators) {
        this.taskDecorators = Objects.requireNonNull(taskDecorators, "taskDecorators");
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        Runnable decoratedRunnable = runnable;
        for (TaskDecorator taskDecorator : taskDecorators) {
            decoratedRunnable = taskDecorator.decorate(decoratedRunnable);
        }
        return decoratedRunnable;
    }
}
