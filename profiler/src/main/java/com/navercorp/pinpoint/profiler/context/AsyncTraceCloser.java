/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceCloseable;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;

/**
 * @author jaehong.kim
 */
@InterfaceAudience.LimitedPrivate("vert.x")
public class AsyncTraceCloser implements AsyncTraceCloseable {

    private final CompletionCallback completionCallback;

    private boolean closed = false;
    private boolean setup = false;
    private boolean await = false;

    public AsyncTraceCloser(CompletionCallback completionCallback) {
        if (completionCallback == null) {
            throw new NullPointerException("completionCallback must not be null");
        }
        this.completionCallback = completionCallback;
    }

    @Override
    public void close() {
        boolean fireCallback = false;
        synchronized (this) {
            if (this.await && !this.closed) {
                fireCallback = true;
            }
            this.closed = true;
        }
        if (fireCallback) {
            completionCallback.onComplete();
        }
    }

    public void setup() {
        synchronized (this) {
            this.setup = true;
        }
    }

    public boolean await() {
        synchronized (this) {
            if (!this.setup || this.closed) {
                return false;
            }
            this.await = true;
            return true;
        }
    }

    @InterfaceAudience.LimitedPrivate("LocalTraceContext")
    public interface CompletionCallback {
        void onComplete();
    }

    @Override
    public String toString() {
        return "AsyncTraceCloser{" +
                "completionCallback=" + completionCallback +
                ", closed=" + closed +
                ", setup=" + setup +
                ", await=" + await +
                '}';
    }
}