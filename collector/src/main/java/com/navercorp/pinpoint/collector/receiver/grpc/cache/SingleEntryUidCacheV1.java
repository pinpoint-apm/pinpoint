package com.navercorp.pinpoint.collector.receiver.grpc.cache;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.Objects;

public class SingleEntryUidCacheV1 implements UidCache {

    //    private volatile ServiceUid lastServiceUid;
    private volatile Entry lastEntry;

    @Override
    public ServiceUid getServiceUid(String serviceName) {
        return ServiceUid.DEFAULT;
    }

    @Override
    public void put(String serviceName, ServiceUid serviceUid) {
        // empty
    }

    @Override
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName) {
        final Entry lastEntry = this.lastEntry;
        if (lastEntry == null) {
            return null;
        }
        if (lastEntry.equals(serviceUid, applicationName)) {
            return lastEntry.getApplicationUid();
        }
        return null;
    }

    @Override
    public void put(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid) {
        this.lastEntry = new Entry(serviceUid, applicationName, applicationUid);
    }

    static class Entry {
        private final ServiceUid serviceUid;
        private final String applicationName;
        private final ApplicationUid applicationUid;

        public Entry(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid) {
            this.serviceUid = Objects.requireNonNull(serviceUid, "serviceUid");

            this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
            this.applicationUid = Objects.requireNonNull(applicationUid, "applicationUid");
        }

        public boolean equals(ServiceUid serviceUid, String applicationName) {
            if (this.serviceUid.equals(serviceUid) && this.applicationName.equals(applicationName)) {
                return true;
            }
            return false;
        }

        public ApplicationUid getApplicationUid() {
            return applicationUid;
        }
    }
}
