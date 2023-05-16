/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.sdk.v1.concurrent;

import com.navercorp.pinpoint.sdk.v1.concurrent.wrapper.DefaultCommandWrapper;
import com.navercorp.pinpoint.sdk.v1.concurrent.wrapper.DisableCommandWrapper;
import com.navercorp.pinpoint.sdk.v1.concurrent.wrapper.CommandWrapper;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Utility class for Executor
 */
public class TraceExecutors {

    public static Executor wrapExecutor(Executor executor) {
        return wrapExecutor(executor, false);
    }

    public static Executor wrapExecutor(Executor executor, boolean autoThreadContextPropagation) {
        Objects.requireNonNull(executor, "executor");

        CommandWrapper wrapper = newCommandWrapper(autoThreadContextPropagation);
        return new TraceExecutor(executor, wrapper);
    }

    public static ExecutorService wrapExecutorService(ExecutorService executorService) {
        return wrapExecutorService(executorService, false);
    }

    public static ExecutorService wrapExecutorService(ExecutorService executorService, boolean autoThreadContextPropagation) {
        Objects.requireNonNull(executorService, "executorService");

        CommandWrapper wrapper = newCommandWrapper(autoThreadContextPropagation);
        return new TraceExecutorService(executorService, wrapper);
    }

    public static ScheduledExecutorService wrapScheduledExecutorService(ScheduledExecutorService executorService) {
        return wrapScheduledExecutorService(executorService, false);
    }

    public static ScheduledExecutorService wrapScheduledExecutorService(ScheduledExecutorService executorService, boolean autoThreadContextPropagation) {
        Objects.requireNonNull(executorService, "executorService");

        CommandWrapper wrapper = newCommandWrapper(autoThreadContextPropagation);
        return new TraceScheduledExecutorService(executorService, wrapper);
    }

    private static CommandWrapper newCommandWrapper(boolean autoThreadContextPropagation) {
        if (autoThreadContextPropagation) {
            return new DefaultCommandWrapper();
        }
        return new DisableCommandWrapper();
    }

}
