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

package com.navercorp.pinpoint.profiler.tools;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.profiler.sender.UdpDataSender;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * 
 * @author netspider
 * 
 */
public class NetworkAvailabilityChecker implements PinpointTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkAvailabilityChecker.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage : " + NetworkAvailabilityChecker.class.getSimpleName() + " AGENT_CONFIG_FILE");
            return;
        }

        String configPath = args[0];

        DataSender udpStatSender = null;
        DataSender udpSpanSender = null;
        DataSender tcpSender = null;

        PinpointClientFactory clientFactory = null;
        PinpointClient client = null;
        try {
            ProfilerConfig profilerConfig = DefaultProfilerConfig.load(configPath);

            String collectorStatIp = profilerConfig.getCollectorStatServerIp();
            int collectorStatPort = profilerConfig.getCollectorStatServerPort();
            udpStatSender = new UdpDataSender(collectorStatIp, collectorStatPort, "UDP-STAT", 10);

            String collectorSpanIp = profilerConfig.getCollectorSpanServerIp();
            int collectorSpanPort = profilerConfig.getCollectorSpanServerPort();
            udpSpanSender = new UdpDataSender(collectorSpanIp, collectorSpanPort, "UDP-SPAN", 10);

            String collectorTcpIp = profilerConfig.getCollectorTcpServerIp();
            int collectorTcpPort = profilerConfig.getCollectorTcpServerPort();
            clientFactory = createPinpointClientFactory();
            client = ClientFactoryUtils.createPinpointClient(collectorTcpIp, collectorTcpPort, clientFactory);

            tcpSender = new TcpDataSender(client);

            boolean udpSenderResult = udpStatSender.isNetworkAvailable();
            boolean udpSpanSenderResult = udpSpanSender.isNetworkAvailable();
            boolean tcpSenderResult = tcpSender.isNetworkAvailable();

            StringBuilder buffer = new StringBuilder();
            buffer.append("\nTEST RESULT\n");
            write(buffer, "UDP-STAT://", collectorStatIp, collectorStatPort, udpSenderResult);
            write(buffer, "UDP-SPAN://", collectorSpanIp, collectorSpanPort, udpSpanSenderResult);
            write(buffer, "TCP://", collectorTcpIp, collectorTcpPort, tcpSenderResult);

            System.out.println(buffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDataSender(udpStatSender);
            closeDataSender(udpSpanSender);
            closeDataSender(tcpSender);
            System.out.println("END.");

            if (client != null) {
                client.close();
            }
            if (clientFactory != null) {
                clientFactory.release();
            }
        }
    }

    private static void write(StringBuilder buffer, String protocol, String collectorStatIp, int collectorStatPort, boolean udpSenderResult) {
        buffer.append(protocol);
        buffer.append(collectorStatIp);
        buffer.append(":");
        buffer.append(collectorStatPort);
        buffer.append("=");
        buffer.append((udpSenderResult) ? "OK" : "FAILED");
        buffer.append("\n");
    }

    private static void closeDataSender(DataSender dataSender) {
        if (dataSender != null) {
            dataSender.stop();
        }
    }
    
    private static PinpointClientFactory createPinpointClientFactory() {
        PinpointClientFactory pinpointClientFactory = new PinpointClientFactory();
        pinpointClientFactory.setTimeoutMillis(1000 * 5);
        pinpointClientFactory.setProperties(Collections.<String, Object>emptyMap());

        return pinpointClientFactory;
    }

}
