/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.metric;

import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;

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
        final Histogram histogram = getHistogram(histogramMap, parentApplicationName, serviceType);
        histogram.addResponseTime(millis);
        return true;
    }

    private Histogram getHistogram(ConcurrentMap<String, Histogram> histogramMap, String parentApplicationName, short serviceTypeCode) {
        final Histogram hit = histogramMap.get(parentApplicationName);
        if (hit != null) {
            return hit;
        }
        final ServiceType serviceType = ServiceType.findServiceType(serviceTypeCode);
        final Histogram histogram = new LongAdderHistogram(serviceType);
        final Histogram old = histogramMap.putIfAbsent(parentApplicationName, histogram);
        if (old != null) {
            return old;
        }
        return histogram;
    }
}
