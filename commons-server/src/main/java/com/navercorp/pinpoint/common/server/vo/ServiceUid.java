package com.navercorp.pinpoint.common.server.vo;

public class ServiceUid {

    public static final int DEFAULT_SERVICE_UID_CODE = 0;

    // reserve 0 for default service uid
    public static final ServiceUid DEFAULT_SERVICE_UID = new ServiceUid(DEFAULT_SERVICE_UID_CODE);

    // reserve -1 ~ -5
    public static final ServiceUid ERROR_SERVICE_UID = new ServiceUid(-1);
    public static final int RESERVED_NEGATIVE_UID_COUNT = 5;

    private final int uid;

    public static ServiceUid of(int uid) {
        if (uid == DEFAULT_SERVICE_UID_CODE) {
            return DEFAULT_SERVICE_UID;
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
