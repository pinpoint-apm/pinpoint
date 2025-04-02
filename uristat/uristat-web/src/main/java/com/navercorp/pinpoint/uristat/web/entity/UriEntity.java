package com.navercorp.pinpoint.uristat.web.entity;

/**
 * @author emeroad
 */
public class UriEntity {
   // common
    protected long timestamp;
    protected String version;

    public UriEntity() {
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
