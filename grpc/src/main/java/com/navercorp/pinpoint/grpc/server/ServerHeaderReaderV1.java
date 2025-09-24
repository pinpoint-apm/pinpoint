/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import com.navercorp.pinpoint.grpc.HeaderV1;
import io.grpc.Metadata;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class ServerHeaderReaderV1 implements HeaderReader<Header> {

    // for debug
    protected final String name;
    private final int applicationNameMaxLength;

    private final Function<Metadata, Map<String, Object>> metadataConverter;

    private final HeaderExtractor headerExtractor = new HeaderExtractor();

    public static HeaderReader<Header> v3(String name, Function<Metadata, Map<String, Object>> metadataConverter) {
        return new ServerHeaderReaderV1(name, metadataConverter, PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
    }

    public ServerHeaderReaderV1(String name, Function<Metadata, Map<String, Object>> metadataConverter) {
        this(name, metadataConverter, PinpointConstants.AGENT_ID_MAX_LEN);
    }

    private ServerHeaderReaderV1(String name, Function<Metadata, Map<String, Object>> metadataConverter, int applicationNameMaxLength) {
        this.name = Objects.requireNonNull(name, "name");
        this.metadataConverter = Objects.requireNonNull(metadataConverter, "metadataConverter");
        this.applicationNameMaxLength = applicationNameMaxLength;
    }

    @Override
    public Header extract(Metadata headers) {
        final String agentId = headerExtractor.getId(headers, Header.AGENT_ID_KEY);
        String agentName = headerExtractor.getName(headers, Header.AGENT_NAME_KEY);
        if (StringUtils.isEmpty(agentName)) {
            agentName = agentId;
        }

        final String applicationName = headerExtractor.getName(headers, Header.APPLICATION_NAME_KEY, applicationNameMaxLength);
        final long startTime = headerExtractor.getTime(headers, Header.AGENT_START_TIME_KEY);
        final int serviceType = headerExtractor.getServiceType(headers);
        final long socketId = headerExtractor.getSocketId(headers);
        final List<Integer> supportCommandCodeList = headerExtractor.getSupportCommandCodeList(headers);
        final boolean grpcBuiltInRetry = headerExtractor.getGrpcBuiltInRetry(headers);
        final Map<String, Object> properties = metadataConverter.apply(headers);
        return new HeaderV1(name, agentId, agentName, applicationName, serviceType, startTime, socketId, supportCommandCodeList, grpcBuiltInRetry, properties);
    }

    @Override
    public String toString() {
        return "AgentHeaderReaderV1{" +
                "name='" + name + '\'' +
                '}';
    }
}
