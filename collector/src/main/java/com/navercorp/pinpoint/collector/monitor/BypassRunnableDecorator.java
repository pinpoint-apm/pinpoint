/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.monitor;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author Taejin Koo
 */
public class BypassRunnableDecorator implements RunnableDecorator {

    @SuppressWarnings("unused") // for debug
    private final String executorName;
    public BypassRunnableDecorator(String executorName) {
        this.executorName = Objects.requireNonNull(executorName, "name");
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        return runnable;
    }

    @Override
    public <T> Callable<T> decorate(Callable<T> task) {
        Objects.requireNonNull(task, "task");
        return task;
    }

    @Override
    public <T> Collection<? extends Callable<T>> decorate(Collection<? extends Callable<T>> tasks) {
        Objects.requireNonNull(tasks, "tasks");
        return tasks;
    }

}
