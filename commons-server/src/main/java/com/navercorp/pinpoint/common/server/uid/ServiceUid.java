package com.navercorp.pinpoint.common.server.uid;

public class ServiceUid {

    // reserve -64 ~ 64
    // 0 for default serviceUid
    // -1 for error serviceUid
    public static final int RESERVED_POSITIVE_UID_COUNT = 64;
    public static final int RESERVED_NEGATIVE_UID_COUNT = 64;

    public static final int DEFAULT_SERVICE_UID_CODE = 0;
    public static final String DEFAULT_SERVICE_UID_NAME = "DEFAULT";
    public static final String UNKNOWN_SERVICE_UID_NAME = "UNKNOWN";

    // serviceUid
    public static final ServiceUid DEFAULT = new ServiceUid(DEFAULT_SERVICE_UID_CODE);
    public static final ServiceUid TEST = new ServiceUid(5);

    public static final ServiceUid ERROR = new ServiceUid(-1);
    public static final ServiceUid UNKNOWN = new ServiceUid(-2);
    public static final ServiceUid NULL = new ServiceUid(-3); // use only for representing missing value in the in-memory cache

    private final int uid;

    public static boolean isReservedUid(int uid) {
        return (-RESERVED_NEGATIVE_UID_COUNT <= uid && uid <= RESERVED_POSITIVE_UID_COUNT);
    }

    public static ServiceUid of(int uid) {
        if (uid == DEFAULT.getUid()) {
            return DEFAULT;
        }
        if (uid == ERROR.getUid()) {
            return ERROR;
        }
        if (isReservedUid(uid)) {
            throw new IllegalArgumentException("Range check failed: " + uid + " is invalid");
        }

        return new ServiceUid(uid);
    }

    ServiceUid(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceUid that = (ServiceUid) o;

        return uid == that.uid;
    }

    @Override
    public int hashCode() {
        return uid;
    }

    @Override
    public String toString() {
        return "ServiceUid{"
                + uid +
                '}';
    }
}
