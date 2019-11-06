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

package com.navercorp.pinpoint.web.vo.scatter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * To be used with @ResponseBody.
 * 
 * @author emeroad
 */
public class ScatterScanResult {
    private long resultFrom;
    private long resultTo;

    private final ScatterIndex scatterIndex = ScatterIndex.MATA_DATA;

    private List<Dot> scatter = Collections.emptyList();

    public ScatterScanResult(long resultFrom, long resultTo, List<Dot> scatterList) {
        this.resultFrom = resultFrom;
        this.resultTo = resultTo;
        this.scatter = Objects.requireNonNull(scatterList, "scatterList");
    }

    public ScatterScanResult() {
    }

    public void setResultFrom(long resultFrom) {
        this.resultFrom = resultFrom;
    }

    public void setResultTo(long resultTo) {
        this.resultTo = resultTo;
    }

    public void setScatter(List<Dot> scatter) {
        this.scatter = scatter;
    }

    @JsonProperty("resultFrom")
    public long getResultFrom() {
        return resultFrom;
    }

    @JsonProperty("resultTo")
    public long getResultTo() {
        return resultTo;
    }

    @JsonProperty("scatterIndex")
    public ScatterIndex getScatterIndex() {
        return scatterIndex;
    }

    @JsonProperty("scatter")
    public Map<String, List<Dot>> getScatter() {
        final Map<String, List<Dot>> scatterAgentData = new HashMap<>();
        for (Dot dot : scatter) {
            List<Dot> list = scatterAgentData.computeIfAbsent(dot.getAgentId(), k -> new ArrayList<>());
            list.add(dot);
        }

        return scatterAgentData;
    }
}
