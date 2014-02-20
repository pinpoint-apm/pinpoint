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

    public HostList() {
    }

    public HostList(HostList copyHostList) {
        if (copyHostList == null) {
            throw new NullPointerException("copyHostList must not be null");
        }

        for (Map.Entry<String, Host> copyEntry : copyHostList.hostMap.entrySet()) {
            String copyKey = copyEntry.getKey();
            Host copyValue = new Host(copyEntry.getValue());
            this.hostMap.put(copyKey, copyValue);
        }
    }


    public void addHost(String hostName, short serviceTypeCode, Histogram histogram) {
        if (hostName == null) {
            throw new NullPointerException("host must not be null");
        }
        Host host = getHost(hostName, serviceTypeCode);

        final Histogram hostHistogram = host.getHistogram();
        hostHistogram.add(histogram);
    }

    public void addHostUncheck(String hostName, short serviceTypeCode, Histogram histogram) {
        if (hostName == null) {
            throw new NullPointerException("host must not be null");
        }
        Host host = getHost(hostName, serviceTypeCode);

        final Histogram hostHistogram = host.getHistogram();
        hostHistogram.addUncheckType(histogram);
    }

    private Host getHost(String hostName, short serviceTypeCode) {
        Host host = hostMap.get(hostName);
        if (host == null) {
            host = new Host(hostName, ServiceType.findServiceType(serviceTypeCode));
            hostMap.put(hostName, host);
        }
        return host;
    }


    public void addHost(Host host) {
        if (host == null) {
            throw new NullPointerException("host must not be null");
        }
        final String hostName = host.getHost();
        final Host find = this.hostMap.get(hostName);
        if (find != null) {
            final Histogram histogram = find.getHistogram();
            histogram.add(host.getHistogram());
        } else {
            // WARN 이것도 copy해야 함.
            Host copy = new Host(host);
            hostMap.put(hostName, copy);
        }

    }

    public void addHostList(HostList addHostList) {
        if (addHostList == null) {
            throw new NullPointerException("host must not be null");
        }
        for (Host host : addHostList.hostMap.values()) {
            addHost(host);
        }
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

    @Override
    public String toString() {
        return "HostList{"
                    + hostMap +
                '}';
    }
}
