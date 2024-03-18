/*
 * Copyright 2018 NAVER Corp.
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
public class DefaultProxyRequestHeader implements ProxyRequestHeader {
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

    public DefaultProxyRequestHeader() {
    }

    public DefaultProxyRequestHeader(long receivedTimeMillis, int durationTimeMicroseconds, byte idlePercent, byte busyPercent, String app, boolean valid, String cause) {
        this.receivedTimeMillis = receivedTimeMillis;
        this.durationTimeMicroseconds = durationTimeMicroseconds;
        this.idlePercent = idlePercent;
        this.busyPercent = busyPercent;
        this.app = app;
        this.valid = valid;
        this.cause = cause;
    }

    public long getReceivedTimeMillis() {
        return receivedTimeMillis;
    }

    public void setReceivedTimeMillis(long receivedTimeMillis) {
        this.receivedTimeMillis = receivedTimeMillis;
    }

    /*
     * The time from when the request was received to the time the headers are sent on the wire.
     */
    public int getDurationTimeMicroseconds() {
        return durationTimeMicroseconds;
    }

    public void setDurationTimeMicroseconds(int durationTimeMicroseconds) {
        this.durationTimeMicroseconds = durationTimeMicroseconds;
    }

    /*
     * The current idle percentage of httpd (0 to 100) based on available processes and threads.
     */
    public byte getIdlePercent() {
        return idlePercent;
    }

    public void setIdlePercent(byte idlePercent) {
        this.idlePercent = idlePercent;
    }

    /*
     * The current busy percentage of httpd (0 to 100) based on available processes and threads.
     */
    public byte getBusyPercent() {
        return busyPercent;
    }

    public void setBusyPercent(byte busyPercent) {
        this.busyPercent = busyPercent;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultProxyRequestHeader{");
        sb.append("receivedTimeMillis=").append(receivedTimeMillis);
        sb.append(", durationTimeMicroseconds=").append(durationTimeMicroseconds);
        sb.append(", idlePercent=").append(idlePercent);
        sb.append(", busyPercent=").append(busyPercent);
        sb.append(", app='").append(app).append('\'');
        sb.append(", valid=").append(valid);
        sb.append(", cause='").append(cause).append('\'');
        sb.append('}');
        return sb.toString();
    }
}