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
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jaehong.kim
 */
public class AsyncTraceCloser implements AsyncTraceCloseable {
    private Span span;
    private Storage storage;

    private boolean closed = false;
    private boolean setup = false;
    private boolean await = false;

    public AsyncTraceCloser(final Span span, final Storage storage) {
        if (span == null || storage == null) {
            throw new IllegalArgumentException("span or storage must not be null.");
        }
        this.span = span;
        this.storage = storage;
    }

    @Override
    public void close() {
        synchronized (this) {
            if (this.await && !this.closed) {
                if (span.isTimeRecording()) {
                    span.markAfterTime();
                }
                this.storage.store(this.span);

                // clear
                final Storage temp = this.storage;
                temp.close();
                this.storage = null;
                this.span = null;
            }
            this.closed = true;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("closed=").append(closed);
        sb.append(", setup=").append(setup);
        sb.append(", await=").append(await);
        sb.append('}');
        return sb.toString();
    }
}