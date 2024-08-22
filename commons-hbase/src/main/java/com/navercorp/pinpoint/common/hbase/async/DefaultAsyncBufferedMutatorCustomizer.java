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

package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.client.AsyncBufferedMutatorBuilder;

import java.util.concurrent.TimeUnit;

public class DefaultAsyncBufferedMutatorCustomizer implements AsyncBufferedMutatorCustomizer {

    private long writeBufferSize = 100;
    private long writeBufferPeriodicFlush = 100;

    public DefaultAsyncBufferedMutatorCustomizer() {
    }

    public void setWriteBufferSize(long writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    public void setWriteBufferPeriodicFlush(long writeBufferPeriodicFlush) {
        this.writeBufferPeriodicFlush = writeBufferPeriodicFlush;
    }

    @Override
    public void customize(AsyncBufferedMutatorBuilder builder) {
        if (writeBufferSize > 0) {
            builder.setWriteBufferSize(writeBufferSize);
        }
        if (writeBufferPeriodicFlush > 0) {
            builder.setWriteBufferPeriodicFlush(writeBufferPeriodicFlush, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public String toString() {
        return "DefaultAsyncBufferedMutatorCustomizer{" +
                "writeBufferSize=" + writeBufferSize +
                ", writeBufferPeriodicFlush=" + writeBufferPeriodicFlush +
                '}';
    }
}
