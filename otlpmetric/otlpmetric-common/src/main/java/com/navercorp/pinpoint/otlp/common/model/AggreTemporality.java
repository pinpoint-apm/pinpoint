package com.navercorp.pinpoint.otlp.common.model;

public enum AggreTemporality {

    UNSPECIFIED(0),
    DELTA(1),
    CUMULATIVE(2),
    UNRECOGNIZED(-1);

    private final int value;
    public final int getNumber() {
        return this.value;
    }

    public static AggreTemporality forNumber(int value) {
        switch (value) {
            case 0:
                return UNSPECIFIED;
            case 1:
                return DELTA;
            case 2:
                return CUMULATIVE;
            default:
                return UNRECOGNIZED;
        }
    }

    public static AggreTemporality forName(String name) {
        switch(name.toUpperCase()) {
            case "UNSPECIFIED":
                return UNSPECIFIED;
            case "DELTA":
                return DELTA;
            case "CUMULATIVE":
                return CUMULATIVE;
            default:
                return UNRECOGNIZED;
        }
    }

    private AggreTemporality(int value) {
        this.value = value;
    }
}
