/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.metric.rpc;

import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceTypeCategory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class DefaultAcceptHistogram implements AcceptHistogram {

    private final ConcurrentMap<ResponseKey, Histogram> map;

    public DefaultAcceptHistogram() {
        this.map = new ConcurrentHashMap<ResponseKey, Histogram>();
    }

    @Override
    public boolean addResponseTime(String parentApplicationName, short serviceTypeCode, int millis, boolean error) {
        if (parentApplicationName == null) {
            throw new NullPointerException("parentApplicationName");
        }
        // Cannot compare by ServiceType value because it could be incompatible if new service type is added.  
        if (!ServiceTypeCategory.SERVER.contains(serviceTypeCode)) {
            return false;
        }
        
        // TODO As already explained, ServiceType.UNDEFINED is returned if serviceTypeCode is of new service type which is added to newer version.
        // How to handle this situation?
        // We can infer if we know the type of histogramSchema. Server can determine the server type with code + schemaType. 
        final ResponseKey responseKey = new ResponseKey(parentApplicationName, serviceTypeCode);
        final Histogram histogram = getHistogram(responseKey);
        histogram.addResponseTime(millis, error);
        return true;
    }

    private Histogram getHistogram(ResponseKey responseKey) {
        final Histogram hit = map.get(responseKey);
        if (hit != null) {
            return hit;
        }
        final Histogram histogram = new LongAdderHistogram(responseKey.getServiceType(), BaseHistogramSchema.NORMAL_SCHEMA);
        final Histogram old = map.putIfAbsent(responseKey, histogram);
        if (old != null) {
            return old;
        }
        return histogram;
    }


    private static final class ResponseKey {
        private final short serviceType;
        private final String parentApplicationName;

        private ResponseKey(String parentApplicationName, short serviceType) {
            if (parentApplicationName == null) {
                throw new NullPointerException("parentApplicationName");
            }

            this.parentApplicationName = parentApplicationName;
            this.serviceType = serviceType;
        }

        public String getParentApplicationName() {
            return parentApplicationName;
        }

        public short getServiceType() {
            return serviceType;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ResponseKey that = (ResponseKey) o;

            if (!parentApplicationName.equals(that.parentApplicationName)) return false;
            if (serviceType != that.serviceType) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) serviceType;
            result = 31 * result + parentApplicationName.hashCode();
            return result;
        }
    }
}
