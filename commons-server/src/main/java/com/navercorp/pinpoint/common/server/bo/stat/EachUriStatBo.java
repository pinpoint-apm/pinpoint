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

package com.navercorp.pinpoint.common.server.bo.stat;


import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class EachUriStatBo {

    private final long timestamp;
    private final String uri;

    private final UriStatHistogram totalHistogram ;
    private final UriStatHistogram failedHistogram;

    public EachUriStatBo(long timestamp, String uri,
                         UriStatHistogram totalHistogram, UriStatHistogram failedHistogram) {
        this.timestamp = timestamp;
        this.uri = Objects.requireNonNull(uri, "uri");
        this.totalHistogram = totalHistogram;
        this.failedHistogram = failedHistogram;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUri() {
        return uri;
    }

    public UriStatHistogram getTotalHistogram() {
        return totalHistogram;
    }

    public UriStatHistogram getFailedHistogram() {
        return failedHistogram;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        EachUriStatBo that = (EachUriStatBo) o;
        return timestamp == that.timestamp && uri.equals(that.uri) && Objects.equals(totalHistogram, that.totalHistogram) && Objects.equals(failedHistogram, that.failedHistogram);
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + Objects.hashCode(totalHistogram);
        result = 31 * result + Objects.hashCode(failedHistogram);
        result = 31 * result + Long.hashCode(timestamp);
        return result;
    }

    @Override
    public String toString() {
        return "EachUriStatBo{" +
                "uri='" + uri + '\'' +
                ", totalHistogram=" + totalHistogram +
                ", failedHistogram=" + failedHistogram +
                ", timestamp=" + timestamp +
                '}';
    }
}
