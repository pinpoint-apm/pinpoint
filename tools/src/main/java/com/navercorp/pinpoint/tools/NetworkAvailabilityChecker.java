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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import com.navercorp.pinpoint.bootstrap.config.Profiles;
import com.navercorp.pinpoint.common.util.PropertyUtils;

import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.NetworkAvailabilityCheckPacket;
import com.navercorp.pinpoint.tools.network.NetworkChecker;
import com.navercorp.pinpoint.tools.network.TCPChecker;
import com.navercorp.pinpoint.tools.network.UDPChecker;
import com.navercorp.pinpoint.tools.network.grpc.GrpcTransportConfig;
import com.navercorp.pinpoint.tools.network.thrift.ThriftTransportConfig;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author netspider
 */
public class NetworkAvailabilityChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkAvailabilityChecker.class);

    private static final String SEPARATOR = File.separator;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage : " + NetworkAvailabilityChecker.class.getSimpleName() + " AGENT_CONFIG_FILE");
            return;
        }

        String configPath = args[0];

        final Properties defaultProperties = new Properties();
        loadFileProperties(defaultProperties, configPath);

        File file = new File(configPath);
        String path = file.getAbsoluteFile().getParent();

        if (configPath.contains(Profiles.CONFIG_FILE_NAME)) {
            // 2. load profile
            final String activeProfile = getActiveProfile(defaultProperties);
            System.out.println("Active profile : " + activeProfile);

            if (activeProfile == null) {
                System.out.println("Could not find activeProfile : " + activeProfile);
                return;
            }

            final File activeProfileConfigFile = new File(path, "profiles" + SEPARATOR + activeProfile + SEPARATOR + Profiles.PROFILE_CONFIG_FILE_NAME);
            loadFileProperties(defaultProperties, activeProfileConfigFile.getAbsolutePath());
        }

        final ProfilerConfig profilerConfig = ProfilerConfigLoader.load(defaultProperties);
        if (profilerConfig.getTransportModule().toString().equals("GRPC")) {

            System.out.println("Transport Module set to GRPC");

            GrpcTransportConfig grpcTransportConfig = new GrpcTransportConfig();
            grpcTransportConfig.read(defaultProperties);

            try {
                checkGRPCBase(grpcTransportConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                checkGRPCMeta(grpcTransportConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                checkGRPCStat(grpcTransportConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                checkGRPCSpan(grpcTransportConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            System.out.println("Transport Module set to THRIFT");
            ThriftTransportConfig thriftTransportConfig = new ThriftTransportConfig();
            thriftTransportConfig.read(defaultProperties);
            try {
                checkUDPStat(thriftTransportConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                checkUDPSpan(thriftTransportConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                checkTCP(thriftTransportConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getActiveProfile(Properties defaultProperties) {
        String profile = defaultProperties.getProperty(Profiles.ACTIVE_PROFILE_KEY, Profiles.DEFAULT_ACTIVE_PROFILE);
        return profile;
    }

    private static void loadFileProperties(Properties properties, String filePath) {
        try {
            PropertyUtils.loadProperty(properties, Paths.get(filePath));
        } catch (IOException e) {
            throw new IllegalStateException(String.format("%s load fail Caused by:%s", filePath, e.getMessage()), e);
        }
    }

    private static void checkGRPCBase(GrpcTransportConfig grpcTransportConfig) throws Exception {
        String ip = grpcTransportConfig.getAgentCollectorIp();
        int port = grpcTransportConfig.getAgentCollectorPort();

        NetworkChecker checker = new TCPChecker("TCP Base", ip, port);
        checker.check();
    }

    private static void checkGRPCMeta(GrpcTransportConfig grpcTransportConfig) throws Exception {
        String ip = grpcTransportConfig.getMetadataCollectorIp();
        int port = grpcTransportConfig.getMetadataCollectorPort();

        NetworkChecker checker = new TCPChecker("TCP Meta", ip, port);
        checker.check();
    }

    private static void checkGRPCStat(GrpcTransportConfig grpcTransportConfig) throws Exception {
        String ip = grpcTransportConfig.getStatCollectorIp();
        int port = grpcTransportConfig.getStatCollectorPort();

        NetworkChecker checker = new TCPChecker("TCP Stat", ip, port);
        checker.check();
    }

    private static void checkGRPCSpan(GrpcTransportConfig grpcTransportConfig) throws Exception {
        String ip = grpcTransportConfig.getSpanCollectorIp();
        int port = grpcTransportConfig.getSpanCollectorPort();

        NetworkChecker checker = new TCPChecker("TCP Span", ip, port);
        checker.check();
    }

    private static void checkUDPStat(ThriftTransportConfig profilerConfig) throws Exception {
        String ip = profilerConfig.getCollectorStatServerIp();
        int port = profilerConfig.getCollectorStatServerPort();

        NetworkChecker checker = new UDPChecker("UDP-STAT", ip, port);
        checker.check(getNetworkCheckPayload(), getNetworkCheckResponsePayload());
    }


    private static void checkUDPSpan(ThriftTransportConfig profilerConfig) throws Exception {
        String ip = profilerConfig.getCollectorSpanServerIp();
        int port = profilerConfig.getCollectorSpanServerPort();

        NetworkChecker checker = new UDPChecker("UDP-SPAN", ip, port);
        checker.check(getNetworkCheckPayload(), getNetworkCheckResponsePayload());
    }

    private static void checkTCP(ThriftTransportConfig profilerConfig) throws Exception {
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
