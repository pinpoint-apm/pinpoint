/*
 * Copyright 2020 NAVER Corp.
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
 */

package com.navercorp.pinpoint.profiler.monitor.metric.uri;

import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Taejin Koo
 */
public class UriStatInfo {

    private final String uri;
    private final boolean status;
    private final long elapsed;

    public UriStatInfo(String uri, boolean status, long elapsed) {
        this.uri = Assert.requireNonNull(uri, "uri");
        this.status = status;
        this.elapsed = elapsed;
    }

    public String getUri() {
        return uri;
    }

    public boolean isStatus() {
        return status;
    }

    public long getElapsed() {
        return elapsed;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UriStatInfo{");
        sb.append("uri='").append(uri).append('\'');
        sb.append(", status=").append(status);
        sb.append(", elapsed=").append(elapsed);
        sb.append('}');
        return sb.toString();
    }
}
