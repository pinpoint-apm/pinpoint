package com.navercorp.pinpoint.common.server.vo;

public class ServiceUid {

    // reserve 0 for default service uid
    public static final ServiceUid DEFAULT_SERVICE_UID = new ServiceUid(0);

    // reserve -1 ~ -5
    public static final ServiceUid ERROR_SERVICE_UID = new ServiceUid(-1);
    public static final int RESERVED_NEGATIVE_UID_COUNT = 5;

    private final int value;

    public ServiceUid(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceUid that = (ServiceUid) o;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
