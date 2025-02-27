package com.navercorp.pinpoint.common.server.vo;

public class ApplicationUid {

    private final long uid;

    public ApplicationUid(long uid) {
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
        return (int) (uid ^ (uid >>> 32));
    }

    @Override
    public String toString() {
        return "ApplicationUid{" +
                "uid=" + uid +
                '}';
    }
}
