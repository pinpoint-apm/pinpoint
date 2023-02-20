package com.navercorp.pinpoint.uristat.web.model;

public class UriStatHistogram {
    private final long timestamp;
    private final int[] histogram;
    private final int version;

    public UriStatHistogram(long timestamp, double hist0, double hist1, double hist2, double hist3,
                   double hist4, double hist5, double hist6, double hist7, int version) {
        //How can i distinguish total stat vs failed stat response?
        this.timestamp = timestamp;
        this.histogram = new int[]{(int) hist0, (int) hist1, (int) hist2, (int) hist3, (int) hist4, (int) hist5, (int) hist6, (int) hist7};
        this.version = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int[] getHistogram() {
        return histogram;
    }

    public int getVersion() {
        return version;
    }
}
