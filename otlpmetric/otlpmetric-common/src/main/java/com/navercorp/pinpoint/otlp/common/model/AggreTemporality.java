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
        return switch (value) {
            case 0 -> UNSPECIFIED;
            case 1 -> DELTA;
            case 2 -> CUMULATIVE;
            default -> UNRECOGNIZED;
        };
    }

    public static AggreTemporality forName(String name) {
        return switch (name.toUpperCase()) {
            case "UNSPECIFIED" -> UNSPECIFIED;
            case "DELTA" -> DELTA;
            case "CUMULATIVE" -> CUMULATIVE;
            default -> UNRECOGNIZED;
        };
    }

    private AggreTemporality(int value) {
        this.value = value;
    }
}
