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

package com.navercorp.pinpoint.tools;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.NetworkAvailabilityCheckPacket;
import com.navercorp.pinpoint.tools.network.NetworkChecker;
import com.navercorp.pinpoint.tools.network.TCPChecker;
import com.navercorp.pinpoint.tools.network.UDPChecker;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * 
 * @author netspider
 * 
 */
public class NetworkAvailabilityChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkAvailabilityChecker.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage : " + NetworkAvailabilityChecker.class.getSimpleName() + " AGENT_CONFIG_FILE");
            return;
        }

        String configPath = args[0];

        ProfilerConfig profilerConfig = null;
        try {
            profilerConfig = DefaultProfilerConfig.load(configPath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            checkUDPStat(profilerConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            checkUDPSpan(profilerConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            checkTCP(profilerConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkUDPStat(ProfilerConfig profilerConfig) throws Exception {
        String ip = profilerConfig.getCollectorStatServerIp();
        int port = profilerConfig.getCollectorStatServerPort();

        NetworkChecker checker = new UDPChecker("UDP-STAT", ip, port);
        checker.check(getNetworkCheckPayload(), getNetworkCheckResponsePayload());
    }


    private static void checkUDPSpan(ProfilerConfig profilerConfig) throws Exception {
        String ip = profilerConfig.getCollectorSpanServerIp();
        int port = profilerConfig.getCollectorSpanServerPort();

        NetworkChecker checker = new UDPChecker("UDP-SPAN", ip, port);
        checker.check(getNetworkCheckPayload(), getNetworkCheckResponsePayload());
    }

    private static void checkTCP(ProfilerConfig profilerConfig) throws Exception {
        String ip = profilerConfig.getCollectorTcpServerIp();
        int port = profilerConfig.getCollectorTcpServerPort();

        NetworkChecker checker = new TCPChecker("TCP", ip, port);
        checker.check();
    }

    private static byte[] getNetworkCheckPayload() throws TException {
        HeaderTBaseSerializer serializer = new HeaderTBaseSerializerFactory(false, 65535, false).createSerializer();
        byte[] payload = serializer.serialize(new NetworkAvailabilityCheckPacket());
        int size = serializer.getInterBufferSize();

        return Arrays.copyOf(payload, size);
    }

    private static byte[] getNetworkCheckResponsePayload() {
        return Arrays.copyOf(NetworkAvailabilityCheckPacket.DATA_OK, NetworkAvailabilityCheckPacket.DATA_OK.length);
    }

}
