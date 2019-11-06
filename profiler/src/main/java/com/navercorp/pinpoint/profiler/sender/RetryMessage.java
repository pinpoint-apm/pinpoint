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

    private int retryCount = 0;
    private final int maxRetryCount;

    private final byte[] bytes;
    private final Object message;
    private final String messageDescription;

    public RetryMessage(int maxRetryCount, byte[] bytes) {
        this(0, maxRetryCount, bytes, null,"");
    }

    public RetryMessage(int retryCount, int maxRetryCount, byte[] bytes) {
        this(retryCount, maxRetryCount, bytes, null,"");
    }

    public RetryMessage(int maxRetryCount, byte[] bytes, String messageDescription) {
        this(0, maxRetryCount, bytes, null, messageDescription);
    }

    public RetryMessage(int retryCount, int maxRetryCount, byte[] bytes, String messageDescription) {
        this(retryCount, maxRetryCount, bytes, null, messageDescription);
    }

    public RetryMessage(int maxRetryCount, Object message) {
        this(0, maxRetryCount, null, message, "");
    }

    public RetryMessage(int retryCount, int maxRetryCount, Object message) {
        this(retryCount, maxRetryCount, null, message, "");
    }

    public RetryMessage(int maxRetryCount, Object message, String messageDescription) {
        this(0, maxRetryCount, null, message, messageDescription);
    }

    public RetryMessage(int retryCount, int maxRetryCount, Object message, String messageDescription) {
        this(retryCount, maxRetryCount, null, message, messageDescription);
    }

    public RetryMessage(int retryCount, int maxRetryCount, byte[] bytes, Object message, String messageDescription) {
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount:" + retryCount + " must be positive number");
        }
        if (maxRetryCount < 0) {
            throw new IllegalArgumentException("maxRetryCount:" + maxRetryCount + " must be positive number");
        }
        if (retryCount > maxRetryCount) {
            throw new IllegalArgumentException("maxRetryCount(" + maxRetryCount + ") must be greater than retryCount(" + retryCount + ")");
        }

        this.retryCount = retryCount;
        this.maxRetryCount = maxRetryCount;
        this.bytes = bytes;
        this.message = message;
        this.messageDescription = messageDescription;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public boolean isRetryAvailable() {
        return retryCount < maxRetryCount;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int fail() {
        return ++retryCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RetryMessage{");
        sb.append("retryCount=").append(retryCount);
        sb.append(", maxRetryCount=").append(maxRetryCount);
        sb.append(", bytes=").append(getLength(bytes));
        sb.append(", messageDescription='").append(messageDescription).append('\'');
        sb.append('}');
        return sb.toString();
    }

    private int getLength(byte[] bytes) {
        if (bytes == null) {
            return -1;
        }
        return bytes.length;
    }
}