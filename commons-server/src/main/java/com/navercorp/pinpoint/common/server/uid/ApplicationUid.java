package com.navercorp.pinpoint.common.server.uid;

public class ApplicationUid {

    // reserve some UIDs
    // Long.MIN_VALUE for error ApplicationUid
    // (Long.MIN_VALUE, Long.MIN_VALUE + RESERVED_NEGATIVE_UID_COUNT), (Long.MAX_VALUE - RESERVED_POSITIVE_UID_COUNT, Long.MAX_VALUE] for future use
    public static final int RESERVED_POSITIVE_UID_COUNT = 64;
    public static final int RESERVED_NEGATIVE_UID_COUNT = 64;

    public static final ApplicationUid ERROR_APPLICATION_UID = new ApplicationUid(-1);

    private final long uid;

    public static ApplicationUid of(long uid) {
        if (uid == -1) {
            return ERROR_APPLICATION_UID;
        }
        // bound check
        if (uid < Long.MIN_VALUE + RESERVED_NEGATIVE_UID_COUNT || Long.MAX_VALUE - RESERVED_POSITIVE_UID_COUNT < uid) {
            throw new IllegalArgumentException("Range check failed: " + uid + " is invalid");
        }
        return new ApplicationUid(uid);
    }

    ApplicationUid(long uid) {
        this.uid = uid;
    }

    public long getUid() {
        return uid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationUid that = (ApplicationUid) o;

        return uid == that.uid;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(uid);
    }

    @Override
    public String toString() {
        return "ApplicationUid{" +
                "uid=" + uid +
                '}';
    }
}
