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

import com.navercorp.pinpoint.sdk.v1.concurrent.wrapper.CommandWrapper;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * {@link Executor} for TraceContext propagation.
 * <p>{@link TraceScheduledExecutorService} marks the entry point of the async action.
 */
public class TraceExecutor implements Executor {

    protected final Executor delegate;
    protected final CommandWrapper wrapper;

    public TraceExecutor(Executor delegate, CommandWrapper wrapper) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.wrapper = Objects.requireNonNull(wrapper, "wrapper");
    }


    @Override
    public void execute(Runnable command) {
        Objects.requireNonNull(command, "command");

        command = wrapper.wrap(command);
        delegate.execute(command);
    }

}
