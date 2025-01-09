package com.navercorp.pinpoint.common.server.threaddump;

import jakarta.annotation.Nullable;

public enum TThreadState {
    NEW(0),
    RUNNABLE(1),
    BLOCKED(2),
    WAITING(3),
    TIMED_WAITING(4),
    TERMINATED(5),
    UNKNOWN(6);

    private final int value;

    private TThreadState(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    @Nullable
    public static TThreadState findByValue(int value) {
        switch (value) {
            case 0:
                return NEW;
            case 1:
                return RUNNABLE;
            case 2:
                return BLOCKED;
            case 3:
                return WAITING;
            case 4:
                return TIMED_WAITING;
            case 5:
                return TERMINATED;
            case 6:
                return UNKNOWN;
            default:
                return null;
        }
    }
}
