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

/**
 * @author Taejin Koo
 */
public class EachUriStatData {

    private final String uri;
    private final UriStatHistogram totalHistogram = new UriStatHistogram();
    private final UriStatHistogram failedHistogram = new UriStatHistogram();

    public EachUriStatData(String uri) {
        this.uri = uri;
    }

    public void add(UriStatInfo uriStatInfo) {
        boolean status = uriStatInfo.isStatus();
        totalHistogram.add(uriStatInfo.getElapsed());

        if (!status) {
            failedHistogram.add(uriStatInfo.getElapsed());
        }
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
    public String toString() {
        final StringBuilder sb = new StringBuilder("EachUriStatData{");
        sb.append("uri='").append(uri).append('\'');
        sb.append(", totalHistogram=").append(totalHistogram);
        sb.append(", failedHistogram=").append(failedHistogram);
        sb.append('}');
        return sb.toString();
    }
}