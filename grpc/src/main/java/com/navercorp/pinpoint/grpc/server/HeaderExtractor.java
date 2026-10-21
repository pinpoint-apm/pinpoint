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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.grpc.Header;
import io.grpc.Metadata;

import java.util.ArrayList;
import java.util.List;

public class HeaderExtractor {
    private static final int UNSPECIFIED_LENGTH = -1;
    private static final int MIN_NAME_LENGTH = 1;

    private final int idMaxLength;
    private final int nameMaxLength;

    public HeaderExtractor() {
        this(PinpointConstants.AGENT_ID_MAX_LEN, PinpointConstants.AGENT_NAME_MAX_LEN);
    }

    public HeaderExtractor(int idMaxLength, int nameMaxLength) {
        this.idMaxLength = idMaxLength;
        this.nameMaxLength = nameMaxLength;
    }

    public long getTime(Metadata headers, Metadata.Key<String> timeKey) {
        final String timeStr = headers.get(timeKey);
        if (timeStr == null) {
            throw new InvalidGrpcHeaderException(timeKey, timeKey.name() + " header is missing");
        }
        try {
            // check number format
            return Long.parseLong(timeStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("unsupported format");
        }
    }

    public String getId(Metadata headers, Metadata.Key<String> idKey) {
        final String id = headers.get(idKey);
        if (id == null) {
            throw new InvalidGrpcHeaderException(idKey, idKey.name() + " header is missing");
        }
        return validateId(id, idKey);
    }

    public String getName(Metadata headers, Metadata.Key<String> idKey) {
        return getName(headers, idKey, this.nameMaxLength);
    }

    public String getName(Metadata headers, Metadata.Key<String> idKey, int maxNameLength) {
        return getName(headers, idKey, MIN_NAME_LENGTH, maxNameLength);
    }

    /**
     * A missing or empty name passes as is. Otherwise the length is bounded by
     * {@code [minNameLength, maxNameLength]} and the id character set is required.
     */
    public String getName(Metadata headers, Metadata.Key<String> idKey, int minNameLength, int maxNameLength) {
        final String name = headers.get(idKey);
        if (StringUtils.isEmpty(name)) {
            return name;
        }

        final IdValidateUtils.CheckResult result = IdValidateUtils.checkId(name, minNameLength, defaultLength(maxNameLength, this.nameMaxLength));
        if (result != IdValidateUtils.CheckResult.SUCCESS) {
            throw new InvalidGrpcHeaderException(idKey, "Invalid " + idKey.name() + " " + result);
        }
        return name;
    }

    /**
     * The service name header is required. An empty value passes, the collector maps it to the DEFAULT service.
     * Otherwise the length is bounded by {@link PinpointConstants#SERVICE_NAME_MIN_LEN} and
     * {@link PinpointConstants#SERVICE_NAME_MAX_LEN}, the same rule as the agent.
     */
    public String getServiceName(Metadata headers) {
        final Metadata.Key<String> key = Header.SERVICE_NAME_KEY;
        if (headers.get(key) == null) {
            throw new InvalidGrpcHeaderException(key, key.name() + " header is missing");
        }
        return getName(headers, key, PinpointConstants.SERVICE_NAME_MIN_LEN, PinpointConstants.SERVICE_NAME_MAX_LEN);
    }

    private int defaultLength(int length, int defaultValue) {
        if (length != UNSPECIFIED_LENGTH) {
            return length;
        }
        return defaultValue;
    }

    public long getSocketId(Metadata headers) {
        final String socketIdStr = headers.get(Header.SOCKET_ID);
        if (socketIdStr == null) {
            return Header.SOCKET_ID_NOT_EXIST;
        }
        try {
            return Long.parseLong(socketIdStr);
        } catch (NumberFormatException e) {
            return Header.SOCKET_ID_NOT_EXIST;
        }
    }

    public List<Integer> getSupportCommandCodeList(Metadata headers) {
        List<Integer> supportCommandCodeList = new ArrayList<>();

        final String value = headers.get(Header.SUPPORT_COMMAND_CODE);
        if (value == null) {
            return Header.SUPPORT_COMMAND_CODE_LIST_NOT_EXIST;
        }

        final List<String> codeValueList = StringUtils.tokenizeToStringList(value, Header.SUPPORT_COMMAND_CODE_DELIMITER);
        try {
            for (String codeValue : codeValueList) {
                if (StringUtils.isEmpty(codeValue)) {
                    continue;
                }

                final String trimmedCodeValue = codeValue.trim();
                final int code = Integer.parseInt(trimmedCodeValue);
                supportCommandCodeList.add(code);
            }
            return supportCommandCodeList;
        } catch (NumberFormatException e) {
            return Header.SUPPORT_COMMAND_CODE_LIST_PARSE_ERROR;
        }
    }

    public boolean getGrpcBuiltInRetry(Metadata headers) {
        final String value = headers.get(Header.GRPC_BUILT_IN_RETRY);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return Header.DEFAULT_GRPC_BUILT_IN_RETRY;
    }

    String validateId(String id, Metadata.Key<?> key) {
        if (!IdValidateUtils.validateId(id, idMaxLength)) {
            throw new InvalidGrpcHeaderException(key, "Invalid " + key.name());
        }
        return id;
    }

    public int getServiceType(Metadata headers) {
        final String serviceTypeStr = headers.get(Header.SERVICE_TYPE_KEY);
        if (serviceTypeStr == null) {
            return ServiceType.UNDEFINED.getCode();
        }
        try {
            return Integer.parseInt(serviceTypeStr);
        } catch (NumberFormatException ignored) {
            return ServiceType.UNDEFINED.getCode();
        }
    }
}
