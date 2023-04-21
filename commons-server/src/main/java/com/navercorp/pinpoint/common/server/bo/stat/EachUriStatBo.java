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

/**
 * @author Taejin Koo
 */
public class EachUriStatBo {

    private String uri;
    private UriStatHistogram totalHistogram = new UriStatHistogram();
    private UriStatHistogram failedHistogram = null;
    private long timestamp;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public UriStatHistogram getTotalHistogram() {
        return totalHistogram;
    }

    public void setTotalHistogram(UriStatHistogram totalHistogram) {
        this.totalHistogram = totalHistogram;
    }

    public UriStatHistogram getFailedHistogram() {
        return failedHistogram;
    }

    public void setFailedHistogram(UriStatHistogram failedHistogram) {
        this.failedHistogram = failedHistogram;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EachUriStatBo that = (EachUriStatBo) o;

        if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
        if (totalHistogram != null ? !totalHistogram.equals(that.totalHistogram) : that.totalHistogram != null) return false;
        if (failedHistogram != null ? !failedHistogram.equals(that.failedHistogram) : that.failedHistogram != null) return false;
        return timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (totalHistogram != null ? totalHistogram.hashCode() : 0);
        result = 31 * result + (failedHistogram != null ? failedHistogram.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
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
