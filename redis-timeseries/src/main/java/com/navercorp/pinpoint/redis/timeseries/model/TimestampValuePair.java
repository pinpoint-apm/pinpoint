package com.navercorp.pinpoint.redis.timeseries.model;

public record TimestampValuePair(long timestamp, double value) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private long timestamp;
        private double value;

        private Builder() {
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder value(double value) {
            this.value = value;
            return this;
        }

        public Builder clear() {
            this.timestamp = 0;
            this.value = 0;
            return this;
        }

        public TimestampValuePair build() {
            return new TimestampValuePair(timestamp, value);
        }

        public TimestampValuePair buildAndClear() {
            TimestampValuePair pair = build();
            clear();
            return pair;
        }
    }


    @Override
    public String toString() {
        return "TimestampValue{" +
                timestamp +
                '=' + value +
                '}';
    }
}
