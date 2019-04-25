/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.monitor.aggregate;

import com.google.common.util.concurrent.AtomicLongMap;
import com.navercorp.pinpoint.collector.util.AtomicLongMapUtils;

import java.util.Map;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class IpPortPacketCountAggregator {

    private final AtomicLongMap<IpPort> countMap = AtomicLongMap.create();

    public void increment(String ip, int port) {
        countMap.incrementAndGet(new IpPort(ip, port));
    }

    public Map<IpPort, Long> getAndReset() {
        return AtomicLongMapUtils.remove(countMap);
    }

    public static class IpPort {
        private final String ip;
        private final int port;
        private final int hashCode;

        private IpPort(String ip, int port) {
            this.ip = ip;
            this.port = port;
            this.hashCode = hashCode0();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IpPort ipPort = (IpPort) o;
            return port == ipPort.port &&
                    Objects.equals(ip, ipPort.ip);
        }

        private int hashCode0() {
            return Objects.hash(ip, port);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return ip + ":" + port;
        }
    }
}
