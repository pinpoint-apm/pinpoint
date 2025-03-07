package com.navercorp.pinpoint.common.server.uid;

public class ApplicationIdentifier {

    private final int serviceUid;
    private final long applicationUid;

    public ApplicationIdentifier(int serviceUid, long applicationUid) {
        this.serviceUid = serviceUid;
        this.applicationUid = applicationUid;
    }

    public int getServiceUid() {
        return serviceUid;
    }

    public long getApplicationUid() {
        return applicationUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationIdentifier that = (ApplicationIdentifier) o;

        if (serviceUid != that.serviceUid) return false;
        return applicationUid == that.applicationUid;
    }

    @Override
    public int hashCode() {
        int result = serviceUid;
        result = 31 * result + Long.hashCode(applicationUid);
        return result;
    }
}
