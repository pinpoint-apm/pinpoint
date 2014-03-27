package com.nhn.pinpoint.web.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nhn.pinpoint.web.vo.scatter.Dot;

import java.util.Collections;
import java.util.List;

/**
 * 나중에 @ResponseBody로 변경시 사용.
 * @author emeroad
 */
public class ScatterScanResult {
    private long resultFrom;
    private long resultTo;

    private final ScatterIndex scatterIndex = ScatterIndex.MATA_DATA;

    private List<Dot> scatter = Collections.emptyList();

    public ScatterScanResult(long resultFrom, long resultTo, List<Dot> scatterList) {
        if (scatterList == null) {
            throw new NullPointerException("resultFrom must not be null");
        }
        this.resultFrom = resultFrom;
        this.resultTo = resultTo;
        this.scatter = scatterList;
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
    public List<Dot> getScatter() {
        return scatter;
    }
}
