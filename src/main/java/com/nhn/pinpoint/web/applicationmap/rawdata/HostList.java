package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author emeroad
 */
public class HostList {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Host> hostMap = new HashMap<String, Host>();

    public void addHost(String hostName, short serviceTypeCode, short slot, long value) {
        if (hostName == null) {
            throw new NullPointerException("host must not be null");
        }
        final Host find = hostMap.get(hostName);
        if (find != null) {
            final ResponseHistogram histogram = find.getHistogram();
            histogram.addSample(slot, value);
        } else {
            final Host host = new Host(hostName, ServiceType.findServiceType(serviceTypeCode));
            host.getHistogram().addSample(slot, value);
            hostMap.put(hostName, host);
        }
    }

    public void addHost(Host host) {
        if (host == null) {
            throw new NullPointerException("host must not be null");
        }
        final String hostName = host.getHost();
        final Host find = this.hostMap.get(hostName);
        if (find != null) {
            find.add(host);
//            final ResponseHistogram findHistogram = find.getHistogram();
//            // TODO 고칠것.
//            findHistogram.mergeWith(host.getHistogram());
        } else {
            hostMap.put(hostName, host);
        }

    }

    public void addHostList(HostList addHostList) {
        if (addHostList == null) {
            throw new NullPointerException("host must not be null");
        }
        for (Host host : addHostList.hostMap.values()) {
            final String hostName = host.getHost();
            final Host find = this.hostMap.get(hostName);
            if (find != null) {
                find.add(host);
            } else {
                this.hostMap.put(hostName, host);
            }

        }

//        for (Entry<String, Host> entry : applicationStatistics.getToHostList().entrySet()) {
//            final String key = entry.getKey();
//            final Host host = this.toHostList.get(key);
//            if (host != null) {
//                host.mergeWith(entry.getValue());
//            } else {
//                this.toHostList.put(key, entry.getValue());
//            }
//        }
    }

    public List<Host> getHostList() {
        final Collection<Host> values = hostMap.values();
        return new ArrayList<Host>(values);
    }

    @Deprecated
    public void put(HostList addHostList) {
        if (addHostList == null) {
            throw new NullPointerException("host must not be null");
        }
        // 이 메소드를 문제가 있음 put정책이 정확하지 않음.
        for (Host host : addHostList.hostMap.values()) {
            final String hostName = host.getHost();
            final Host old = this.hostMap.put(hostName, host);
            if (old != null) {
                logger.warn("old key exist. key:{}, new:{} old:{}", hostName, host, old);
            }
        }
    }
}
