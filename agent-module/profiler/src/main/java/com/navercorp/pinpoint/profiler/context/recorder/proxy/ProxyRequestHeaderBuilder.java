/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.recorder.proxy;

/**
 * @author jaehong.kim
 */
public class ProxyRequestHeaderBuilder {
    // received time of request.
    private long receivedTimeMillis;

    // optional
    private int durationTimeMicroseconds = -1;
    private byte idlePercent = -1;
    private byte busyPercent = -1;
    private String app;

    // state
    private boolean valid = false;
    private String cause = "required value not set";

    public ProxyRequestHeader build() {
        return new DefaultProxyRequestHeader(receivedTimeMillis, durationTimeMicroseconds, idlePercent, busyPercent, app, valid, cause);
    }

    public ProxyRequestHeaderBuilder setReceivedTimeMillis(final long receivedTimeMillis) {
        this.receivedTimeMillis = receivedTimeMillis;
        return this;
    }

    public ProxyRequestHeaderBuilder setDurationTimeMicroseconds(final int durationTimeMicroseconds) {
        this.durationTimeMicroseconds = durationTimeMicroseconds;
        return this;
    }

    public ProxyRequestHeaderBuilder setIdlePercent(final byte idlePercent) {
        this.idlePercent = idlePercent;
        return this;
    }

    public ProxyRequestHeaderBuilder setBusyPercent(final byte busyPercent) {
        this.busyPercent = busyPercent;
        return this;
    }

    public ProxyRequestHeaderBuilder setApp(final String app) {
        this.app = app;
        return this;
    }

    public ProxyRequestHeaderBuilder setValid(final boolean valid) {
        this.valid = valid;
        return this;
    }

    public ProxyRequestHeaderBuilder setCause(final String cause) {
        this.cause = cause;
        return this;
    }
}