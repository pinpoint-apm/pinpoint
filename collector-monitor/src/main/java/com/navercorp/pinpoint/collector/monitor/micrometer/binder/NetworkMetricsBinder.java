/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.monitor.micrometer.binder;

import io.micrometer.common.lang.NonNull;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ref https://github.com/jrask/micrometer-oshi-binder
 * @author intr3p1d
 */
public class NetworkMetricsBinder implements MeterBinder {

    public static final int MAX_REFRESH_TIME = 5_000;
    private final OperatingSystem os;

    public enum NetworkMetric {

        BYTES_RECEIVED,
        BYTES_SENT,
        PACKETS_RECEIVED,
        PACKETS_SENT
    }

    private final Iterable<Tag> tags;
    private final CachedNetworkStats networkStats =
            new CachedNetworkStats(new SystemInfo().getHardware().getNetworkIFs());

    public NetworkMetricsBinder() {
        this(Collections.emptyList());
    }

    public NetworkMetricsBinder(Iterable<Tag> tags) {
        SystemInfo systemInfo = new SystemInfo();
        this.os = systemInfo.getOperatingSystem();
        this.tags = tags;
    }

    @Override
    public void bindTo(@NonNull MeterRegistry meterRegistry) {

        FunctionCounter.builder("system.network.bytes.received",
                        NetworkMetric.BYTES_RECEIVED, this::getNetworkMetricAsLong)
                .tags(tags)
                .register(meterRegistry);

        FunctionCounter.builder("system.network.bytes.sent",
                        NetworkMetric.BYTES_SENT, this::getNetworkMetricAsLong)
                .tags(tags)
                .register(meterRegistry);

        FunctionCounter.builder("system.network.packets.received",
                        NetworkMetric.PACKETS_RECEIVED, this::getNetworkMetricAsLong)
                .tags(tags)
                .register(meterRegistry);

        FunctionCounter.builder("system.network.packets.sent",
                        NetworkMetric.PACKETS_SENT, this::getNetworkMetricAsLong)
                .tags(tags)
                .register(meterRegistry);

        Gauge.builder("process.open.sockets", this, NetworkMetricsBinder::getOpenSocketCount)
                .tags(tags)
                .register(meterRegistry);
    }


    private long getOpenSocketCount() {
        OSProcess process = os.getProcess(os.getProcessId());
        return process.getOpenFiles();
    }

    private long getNetworkMetricAsLong(NetworkMetric type) {
        networkStats.refresh();

        return switch (type) {
            case BYTES_SENT -> networkStats.bytesSent;
            case PACKETS_SENT -> networkStats.packetsSent;
            case BYTES_RECEIVED -> networkStats.bytesReceived;
            case PACKETS_RECEIVED -> networkStats.packetsReceived;
            default -> throw new IllegalStateException("Unknown NetworkMetrics: " + type.name());
        };
    }

    private static class CachedNetworkStats {

        private final Lock lock = new ReentrantLock();

        private final List<NetworkIF> networks;
        private long lastRefresh = 0;

        public volatile long bytesReceived;
        public volatile long bytesSent;
        public volatile long packetsReceived;
        public volatile long packetsSent;

        public CachedNetworkStats(List<NetworkIF> networkIFs) {
            this.networks = networkIFs;
        }

        public void refresh() {
            if (lock.tryLock()) {
                try {
                    doRefresh();
                } finally {
                    lock.unlock();
                }
            }
        }

        private void doRefresh() {

            if (System.currentTimeMillis() - lastRefresh < MAX_REFRESH_TIME) {
                return;
            }

            long bytesReceived = 0;
            long bytesSent = 0;
            long packetsReceived = 0;
            long packetsSent = 0;

            for (NetworkIF nif : networks) {
                nif.updateAttributes();
                bytesReceived += nif.getBytesRecv();
                bytesSent += nif.getBytesSent();
                packetsReceived += nif.getPacketsRecv();
                packetsSent += nif.getPacketsSent();

            }
            this.bytesReceived = bytesReceived;
            this.bytesSent = bytesSent;
            this.packetsReceived = packetsReceived;
            this.packetsSent = packetsSent;

            this.lastRefresh = System.currentTimeMillis();
        }
    }
}
