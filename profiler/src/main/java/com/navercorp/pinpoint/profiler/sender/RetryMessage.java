/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender;

/**
 * @author emeroad
 */
public class RetryMessage {
    private int retryCount;
    private byte[] bytes;

    public RetryMessage(int retryCount, byte[] bytes) {
        this.retryCount = retryCount;
        this.bytes = bytes;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int fail() {
        return ++retryCount;
    }
}
