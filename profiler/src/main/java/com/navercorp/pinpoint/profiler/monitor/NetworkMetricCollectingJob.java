/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.monitor;

import com.navercorp.pinpoint.common.profiler.clock.Clock;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.profiler.monitor.metric.MetricType;
import com.navercorp.pinpoint.profiler.monitor.metric.profilermetric.NetworkMetric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.PlatformEnum;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;
import oshi.software.os.InternetProtocolStats;

import java.util.*;

public class NetworkMetricCollectingJob implements Runnable {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private boolean networkInterfaceSupported = true;
    private boolean udpStatSupported;
    private boolean tcpStatSupported;
    private final Clock clock = Clock.tick(1000);
    private final long collectInterval;
    private final DataSender<MetricType> dataSender;

    private String hostName;
    private InternetProtocolStats protocolStats;
    private NetworkInterfaceInfo currNetworkIFs = null;
    private NetworkInterfaceInfo prevNetworkIFs = null;
    private InternetProtocolStats.TcpStats prevTcpV4Stats = null;
    private InternetProtocolStats.TcpStats prevTcpV6Stats = null;
    private InternetProtocolStats.UdpStats prevUdpV4Stats = null;
    private InternetProtocolStats.UdpStats prevUdpV6Stats = null;

    private class NetworkInterfaceInfo {
        private Map<String, NetworkIF> parsed;

        public NetworkInterfaceInfo(HardwareAbstractionLayer hal) {
            List<NetworkIF> networkIFs = hal.getNetworkIFs();
            this.parsed = new HashMap<>();

            for (NetworkIF networkIF : networkIFs) {
                this.parsed.put(networkIF.getName(), networkIF);
            }
        }

        public NetworkIF getNetworkIF(String name) {
            return parsed.get(name);
        }

        public Collection<NetworkIF> getCollection() {
            return parsed.values();
        }
    }

    public NetworkMetricCollectingJob(DataSender<MetricType> dataSender,
                                      boolean enableUdpStat, boolean enableTcpStat, long collectInterval) {
        this.dataSender = Objects.requireNonNull(dataSender, "dataSender");
        this.udpStatSupported = enableUdpStat;
        this.tcpStatSupported = enableTcpStat;
        this.collectInterval = collectInterval;

        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        this.hostName = os.getNetworkParams().getHostName();

        initializeNetworkInterfaceInfo(si);

        if (udpStatSupported || tcpStatSupported) {
            initializeProtocolStats(os);
        }
    }

    private void initializeProtocolStats(OperatingSystem os) {
        this.protocolStats = os.getInternetProtocolStats();
        try {
            protocolStats.getTCPv4Stats();
            protocolStats.getTCPv6Stats();
        } catch (Exception e) {
            udpStatSupported = false;
            tcpStatSupported = false;
            if (logger.isWarnEnabled()) {
                logger.warn("OSHI Protocol Statistics not supported. Not collecting protocol stat metrics.");
            }
        }
    }

    private void initializeNetworkInterfaceInfo(SystemInfo si) {
        HardwareAbstractionLayer hal = si.getHardware();
        try {
            currNetworkIFs = new NetworkInterfaceInfo(hal);
            prevNetworkIFs = new NetworkInterfaceInfo(hal);
        } catch (Exception e) {
            networkInterfaceSupported = false;
            if (logger.isWarnEnabled()) {
                logger.warn("OSHI Network Interface not supported. Not collecting network interface metrics.");
            }
        }

        if (currNetworkIFs == null || prevNetworkIFs == null) {
            networkInterfaceSupported = false;
        }
    }

    public static boolean isSupported() {
        return !SystemInfo.getCurrentPlatform().equals(PlatformEnum.UNKNOWN);
    }

    @Override
    public void run() {
        if (networkInterfaceSupported) {
            checkNetworkIfs();
        }

        if (tcpStatSupported) {
            addTCPProtocolStats();
        }

        if (udpStatSupported) {
            addUDPProtocolStats();
        }
    }

    private void checkNetworkIfs() {
        long timestamp = clock.millis() / 1000L;

        for (NetworkIF networkIF : currNetworkIFs.getCollection()) {
            if (networkIF.updateAttributes()) {
                NetworkIF prev = prevNetworkIFs.getNetworkIF(networkIF.getName());

                NetworkMetric metric = getMetricData(networkIF, prev, timestamp);
                if (metric != null) {
                    dataSender.send(metric);
                }
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to update current values for network interfaces");
                }
            }
        }

        NetworkInterfaceInfo temp = currNetworkIFs;
        currNetworkIFs = prevNetworkIFs;
        prevNetworkIFs = temp;
    }

    private NetworkMetric getMetricData(NetworkIF curr, NetworkIF prev, long timestamp) {
        if (prev == null) {
            return null;
        }

        NetworkMetric metric = new NetworkMetric("network_interface", timestamp, collectInterval);
        metric.addTag("name", curr.getName());
        metric.addTag("mac_addr", curr.getMacaddr());
        metric.addTag("host", this.hostName);
        metric.addField("rx_packets", (curr.getPacketsRecv() - prev.getPacketsRecv()));
        metric.addField("rx_bytes", (curr.getBytesRecv() - prev.getBytesRecv()));
        metric.addField("rx_errors", (curr.getInErrors() - prev.getInErrors()));
        metric.addField("rx_drops", (curr.getInDrops() - prev.getInDrops()));
        metric.addField("tx_packets", (curr.getPacketsSent() - prev.getPacketsSent()));
        metric.addField("tx_bytes", (curr.getBytesSent() - prev.getBytesSent()));
        metric.addField("tx_errors", (curr.getOutErrors() - prev.getOutErrors()));
        metric.addField("tx_collisions", (curr.getCollisions() - prev.getCollisions()));
        return metric;
    }

    private void addTCPProtocolStats() {
        long timestamp = clock.millis() / 1000L;
        InternetProtocolStats.TcpStats tcpv4 = protocolStats.getTCPv4Stats();
        InternetProtocolStats.TcpStats tcpv6 = protocolStats.getTCPv6Stats();

        NetworkMetric metric = getMetricData("v4", tcpv4, prevTcpV4Stats, timestamp);
        if (metric != null) {
            dataSender.send(metric);
        }

        metric = getMetricData("v6", tcpv6, prevTcpV6Stats, timestamp);
        if (metric != null) {
            dataSender.send(metric);
        }

        prevTcpV4Stats = tcpv4;
        prevTcpV6Stats = tcpv6;
    }

    private void addUDPProtocolStats() {
        long timestamp = clock.millis() / 1000L;
        InternetProtocolStats.UdpStats udpv4 = protocolStats.getUDPv4Stats();
        InternetProtocolStats.UdpStats udpv6 = protocolStats.getUDPv6Stats();

        NetworkMetric metric = getMetricData("v4", udpv4, prevUdpV4Stats, timestamp);
        if (metric != null) {
            dataSender.send(metric);
        }

        metric = getMetricData("v6", udpv6, prevUdpV6Stats, timestamp);
        if (metric != null) {
            dataSender.send(metric);
        }

        prevUdpV4Stats = protocolStats.getUDPv4Stats();
        prevUdpV6Stats = protocolStats.getUDPv6Stats();
    }

    private NetworkMetric getMetricData(String version, InternetProtocolStats.TcpStats tcpStats, InternetProtocolStats.TcpStats prev, long timestamp) {
        if (prev == null) {
            return null;
        }

        NetworkMetric metric = new NetworkMetric("tcp_stats", timestamp, collectInterval);
        metric.addTag("version", version);
        metric.addTag("host", this.hostName);
        metric.addField("conn_established", tcpStats.getConnectionsEstablished());
        metric.addField("conn_active", tcpStats.getConnectionsActive());
        metric.addField("conn_passive", tcpStats.getConnectionsPassive());
        metric.addField("conn_failure", tcpStats.getConnectionFailures());
        metric.addField("conn_reset", tcpStats.getConnectionsReset());
        metric.addField("seg_sent", (tcpStats.getSegmentsSent() - prev.getSegmentsSent()));
        metric.addField("seg_received", (tcpStats.getSegmentsReceived() - prev.getSegmentsReceived()));
        metric.addField("seg_retransmitted", (tcpStats.getSegmentsRetransmitted() - prev.getSegmentsRetransmitted()));
        metric.addField("in_errors", (tcpStats.getInErrors() - prev.getInErrors()));
        metric.addField("out_resets", (tcpStats.getOutResets() - prev.getOutResets()));
        return metric;
    }

    private NetworkMetric getMetricData(String version, InternetProtocolStats.UdpStats udpStats, InternetProtocolStats.UdpStats prev, long timestamp) {
        if (prev == null) {
            return null;
        }

        NetworkMetric metric = new NetworkMetric("udp_stats", timestamp, collectInterval);
        metric.addTag("version", version);
        metric.addTag("host", this.hostName);
        metric.addField("tx", (udpStats.getDatagramsSent() - prev.getDatagramsSent()));
        metric.addField("rx", (udpStats.getDatagramsReceived() - prev.getDatagramsReceived()));
        metric.addField("noport", (udpStats.getDatagramsNoPort() - prev.getDatagramsNoPort()));
        metric.addField("rx_error", (udpStats.getDatagramsReceivedErrors() - prev.getDatagramsReceivedErrors()));
        return metric;
    }

}
