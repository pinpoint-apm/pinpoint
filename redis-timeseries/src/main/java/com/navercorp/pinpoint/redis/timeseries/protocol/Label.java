package com.navercorp.pinpoint.redis.timeseries.protocol;

public record Label(String label, String value) {
    public static Label of(String label, String value) {
        return new Label(label, value);
    }
}
