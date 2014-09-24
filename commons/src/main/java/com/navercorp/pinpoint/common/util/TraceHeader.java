package com.nhn.pinpoint.common.util;

/**
 * @author emeroad
 */
public class TraceHeader {
    private String id;
    private String spanId;
    private String parentSpanId;
    private String sampling;
    private String flag;

    public TraceHeader() {
    }

    public TraceHeader(String id, String spanId, String parentSpanId, String sampling, String flag) {
        this.id = id;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
        this.sampling = sampling;
        this.flag = flag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getSampling() {
        return sampling;
    }

    public void setSampling(String sampling) {
        this.sampling = sampling;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "TraceHeader{" +
                "id='" + id + '\'' +
                ", spanId='" + spanId + '\'' +
                ", parentSpanId='" + parentSpanId + '\'' +
                ", sampling='" + sampling + '\'' +
                ", flag='" + flag + '\'' +
                '}';
    }
}
