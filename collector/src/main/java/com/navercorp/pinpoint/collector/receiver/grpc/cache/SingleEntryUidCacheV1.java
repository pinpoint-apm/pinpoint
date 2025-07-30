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
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        final Entry lastEntry = this.lastEntry;
        if (lastEntry == null) {
            return null;
        }
        if (lastEntry.equals(serviceUid, applicationName, serviceTypeCode)) {
            return lastEntry.getApplicationUid();
        }
        return null;
    }

    @Override
    public void put(ServiceUid serviceUid, String applicationName, int serviceTypeCode, ApplicationUid applicationUid) {
        this.lastEntry = new Entry(serviceUid, applicationName, serviceTypeCode, applicationUid);
    }

    static class Entry {
        private final ServiceUid serviceUid;
        private final String applicationName;
        private final int serviceTypeCode;
        private final ApplicationUid applicationUid;

        public Entry(ServiceUid serviceUid, String applicationName, int serviceTypeCode, ApplicationUid applicationUid) {
            this.serviceUid = Objects.requireNonNull(serviceUid, "serviceUid");

            this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
            this.serviceTypeCode = serviceTypeCode;
            this.applicationUid = Objects.requireNonNull(applicationUid, "applicationUid");
        }

        public boolean equals(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
            if (this.serviceUid.equals(serviceUid) &&
                    this.applicationName.equals(applicationName) &&
                    this.serviceTypeCode == serviceTypeCode) {
                return true;
            }
            return false;
        }

        public ApplicationUid getApplicationUid() {
            return applicationUid;
        }
    }
}
