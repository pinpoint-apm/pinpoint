/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.storage;

import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Taejin Koo
 */
public class RequestUrlStatInfo {

    // 4 + 8 + 8
    public static int DEFAULT_DATA_SIZE_WITHOUT_URL = 20;

    // url + 4 + 8
    private final String url;
    private final int status;
    private final long startTime;
    private final long elapsedTime;

    public RequestUrlStatInfo(String url, int status, long startTime, long elapsedTime) {
        this.url = Assert.requireNonNull(url, "url");
        this.status = status;
        this.startTime = startTime;
        this.elapsedTime = elapsedTime;
    }

    public String getUrl() {
        return url;
    }

    public int getStatus() {
        return status;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequestsStatInfo{");
        sb.append("url='").append(url).append('\'');
        sb.append(", status=").append(status);
        sb.append(", startTime=").append(startTime);
        sb.append(", elapsedTime=").append(elapsedTime);
        sb.append('}');
        return sb.toString();
    }

}
