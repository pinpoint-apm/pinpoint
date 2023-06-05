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

import com.navercorp.pinpoint.tools.network.NetworkChecker;
import com.navercorp.pinpoint.tools.network.TCPChecker;
import com.navercorp.pinpoint.tools.network.grpc.GrpcTransportConfig;
import com.navercorp.pinpoint.tools.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author netspider
 */
public class NetworkAvailabilityChecker {

    public static final String ACTIVE_PROFILE_KEY = "pinpoint.profiler.profiles.active";
    public static final String CONFIG_FILE_NAME = "pinpoint-root.config";
    public static final String PROFILE_CONFIG_FILE_NAME = "pinpoint.config";

    private static final String DEFAULT_PROFILE = "release";

    private static final Logger logger = new Logger();

    public static void main(String[] args) {
        if (args.length != 1) {
            logger.info("usage : " + NetworkAvailabilityChecker.class.getSimpleName() + " AGENT_CONFIG_FILE");
            return;
        }

        String configPath = args[0];
        Path filePath = Paths.get(configPath);

        final Properties defaultProperties = new Properties();
        loadFileProperties(defaultProperties, filePath);

        Path path = filePath.toAbsolutePath().getParent();

        if (configPath.contains(CONFIG_FILE_NAME)) {
            // 2. load profile
            final String activeProfile = getActiveProfile(defaultProperties);
            logger.info("Active profile : " + activeProfile);

            if (activeProfile == null) {
                logger.info("Could not find activeProfile : null");
                return;
            }

            final Path activeProfileConfigFile = Paths.get(path.toString(), "profiles", activeProfile, PROFILE_CONFIG_FILE_NAME);

            loadFileProperties(defaultProperties, activeProfileConfigFile.toAbsolutePath());
        }

        logger.info("Transport Module set to GRPC");

        GrpcTransportConfig grpcTransportConfig = new GrpcTransportConfig(defaultProperties);

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

    }

    private static String getActiveProfile(Properties defaultProperties) {
        return defaultProperties.getProperty(ACTIVE_PROFILE_KEY, DEFAULT_PROFILE);
    }

    private static void loadFileProperties(Properties properties, Path filePath) {
        try {
            InputStream inputStream = Files.newInputStream(filePath);
            properties.load(inputStream);
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

}
