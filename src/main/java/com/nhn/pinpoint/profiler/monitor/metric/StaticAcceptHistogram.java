package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.common.IntHashMap;
import com.nhn.pinpoint.common.ServiceType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class StaticAcceptHistogram implements AcceptHistogram {
    // 동적으로 데이터 등록이 가능하지 않음. thread safe하지 않으므로 절대 put을 하지 말것.
    private final IntHashMap<ConcurrentMap<String, Histogram>> map;

    public StaticAcceptHistogram() {
        this.map = new IntHashMap<ConcurrentMap<String, Histogram>>();
        bindMap(map);
    }

    private void bindMap(IntHashMap<ConcurrentMap<String, Histogram>> map) {
        ServiceType[] serviceTypeList = ServiceType.values();
        for (ServiceType serviceType : serviceTypeList) {
            if (serviceType.isWas()) {
                ConcurrentMap<String, Histogram> caller = new ConcurrentHashMap<String, Histogram>();
                map.put(serviceType.getCode(), caller);
            }
        }
    }


    @Override
    public boolean addResponseTime(String parentApplicationName, short serviceType, int millis) {
        if (parentApplicationName == null) {
            throw new NullPointerException("parentApplicationName must not be null");
        }

        final ConcurrentMap<String, Histogram> histogramMap = this.map.get(serviceType);
        if (histogramMap == null) {
            return false;
        }
        Histogram histogram = getHistogram(histogramMap, parentApplicationName, serviceType);
        histogram.addResponseTime(millis);
        return true;
    }

    private Histogram getHistogram(ConcurrentMap<String, Histogram> histogramMap, String parentApplicationName, short serviceType) {
        final Histogram hit = histogramMap.get(parentApplicationName);
        if (hit != null) {
            return hit;
        }
        final Histogram histogram = new LongAdderHistogram(ServiceType.findServiceType(serviceType));
        final Histogram old = histogramMap.putIfAbsent(parentApplicationName, histogram);
        if (old != null) {
            return old;
        }
        return histogram;
    }
}
