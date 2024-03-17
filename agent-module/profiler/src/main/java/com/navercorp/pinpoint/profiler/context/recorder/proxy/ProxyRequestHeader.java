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
public interface ProxyRequestHeader {

    long getReceivedTimeMillis();

    /*
     * The time from when the request was received to the time the headers are sent on the wire.
     */
    int getDurationTimeMicroseconds();

    /*
     * The current idle percentage of httpd (0 to 100) based on available processes and threads.
     */
    byte getIdlePercent();

    /*
     * The current busy percentage of httpd (0 to 100) based on available processes and threads.
     */
    byte getBusyPercent();

    String getApp();

    boolean isValid();

    String getCause();
}