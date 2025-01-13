/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.uristat.web.entity;

/**
 * @author intr3p1d
 */
public class UriStatChartEntity {

    // total
    private Double tot0;
    private Double tot1;
    private Double tot2;
    private Double tot3;
    private Double tot4;
    private Double tot5;
    private Double tot6;
    private Double tot7;

    // fail
    private Double fail0;
    private Double fail1;
    private Double fail2;
    private Double fail3;
    private Double fail4;
    private Double fail5;
    private Double fail6;
    private Double fail7;

    // latency
    private Double totalTimeMs;
    private Double maxLatencyMs;
    private Double count;

    // apdex
    private Double apdexRaw;

    // common
    private long timestamp;
    private String version;

    public UriStatChartEntity() {
    }

    public Double getTot0() {
        return tot0;
    }

    public void setTot0(Double tot0) {
        this.tot0 = tot0;
    }

    public Double getTot1() {
        return tot1;
    }

    public void setTot1(Double tot1) {
        this.tot1 = tot1;
    }

    public Double getTot2() {
        return tot2;
    }

    public void setTot2(Double tot2) {
        this.tot2 = tot2;
    }

    public Double getTot3() {
        return tot3;
    }

    public void setTot3(Double tot3) {
        this.tot3 = tot3;
    }

    public Double getTot4() {
        return tot4;
    }

    public void setTot4(Double tot4) {
        this.tot4 = tot4;
    }

    public Double getTot5() {
        return tot5;
    }

    public void setTot5(Double tot5) {
        this.tot5 = tot5;
    }

    public Double getTot6() {
        return tot6;
    }

    public void setTot6(Double tot6) {
        this.tot6 = tot6;
    }

    public Double getTot7() {
        return tot7;
    }

    public void setTot7(Double tot7) {
        this.tot7 = tot7;
    }

    public Double getFail0() {
        return fail0;
    }

    public void setFail0(Double fail0) {
        this.fail0 = fail0;
    }

    public Double getFail1() {
        return fail1;
    }

    public void setFail1(Double fail1) {
        this.fail1 = fail1;
    }

    public Double getFail2() {
        return fail2;
    }

    public void setFail2(Double fail2) {
        this.fail2 = fail2;
    }

    public Double getFail3() {
        return fail3;
    }

    public void setFail3(Double fail3) {
        this.fail3 = fail3;
    }

    public Double getFail4() {
        return fail4;
    }

    public void setFail4(Double fail4) {
        this.fail4 = fail4;
    }

    public Double getFail5() {
        return fail5;
    }

    public void setFail5(Double fail5) {
        this.fail5 = fail5;
    }

    public Double getFail6() {
        return fail6;
    }

    public void setFail6(Double fail6) {
        this.fail6 = fail6;
    }

    public Double getFail7() {
        return fail7;
    }

    public void setFail7(Double fail7) {
        this.fail7 = fail7;
    }

    public Double getTotalTimeMs() {
        return totalTimeMs;
    }

    public void setTotalTimeMs(Double totalTimeMs) {
        this.totalTimeMs = totalTimeMs;
    }

    public Double getMaxLatencyMs() {
        return maxLatencyMs;
    }

    public void setMaxLatencyMs(Double maxLatencyMs) {
        this.maxLatencyMs = maxLatencyMs;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Double getApdexRaw() {
        return apdexRaw;
    }

    public void setApdexRaw(Double apdexRaw) {
        this.apdexRaw = apdexRaw;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
