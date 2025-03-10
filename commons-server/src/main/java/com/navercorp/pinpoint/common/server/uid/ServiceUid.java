package com.navercorp.pinpoint.common.server.uid;

public class ServiceUid {

    // reserve -64 ~ 64
    // 0 for default serviceUid
    // -1 for error serviceUid
    public static final int RESERVED_POSITIVE_UID_COUNT = 64;
    public static final int RESERVED_NEGATIVE_UID_COUNT = 64;

    public static final int DEFAULT_SERVICE_UID_CODE = 0;

    public static final ServiceUid DEFAULT_SERVICE_UID = new ServiceUid(DEFAULT_SERVICE_UID_CODE);
    public static final ServiceUid ERROR_SERVICE_UID = new ServiceUid(-1);

    private final int uid;

    public static ServiceUid of(int uid) {
        if (uid == DEFAULT_SERVICE_UID_CODE) {
            return DEFAULT_SERVICE_UID;
        }
        if (uid == -1) {
            return ERROR_SERVICE_UID;
        }
        // bound check
        if (-RESERVED_NEGATIVE_UID_COUNT <= uid && uid <= RESERVED_POSITIVE_UID_COUNT) {
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
