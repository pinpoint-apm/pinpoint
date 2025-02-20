package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.grpc.Header;
import io.grpc.Metadata;
import io.grpc.Status;

import java.util.ArrayList;
import java.util.List;

public class HeaderExtractor {

    private final int nameMaxLength = PinpointConstants.AGENT_NAME_MAX_LEN;

    public long getTime(Metadata headers, Metadata.Key<String> timeKey) {
        final String timeStr = headers.get(timeKey);
        if (timeStr == null) {
            throw Status.INVALID_ARGUMENT.withDescription(timeKey.name() + " header is missing").asRuntimeException();
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
            throw Status.INVALID_ARGUMENT.withDescription(idKey.name() + " header is missing").asRuntimeException();
        }
        return validateId(id, idKey);
    }

    public String getName(Metadata headers, Metadata.Key<String> idKey) {
        final String name = headers.get(idKey);
        if (!StringUtils.isEmpty(name)) {
            final IdValidateUtils.CheckResult result = IdValidateUtils.checkId(name, nameMaxLength);
            if (result == IdValidateUtils.CheckResult.FAIL_PATTERN) {
                throw Status.INVALID_ARGUMENT.withDescription("invalid " + idKey.name()).asRuntimeException();
            }
            if (result == IdValidateUtils.CheckResult.FAIL_LENGTH) {
                throw Status.INVALID_ARGUMENT.withDescription("invalid " + idKey.name() + ".length").asRuntimeException();
            }
        }
        return name;
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
        if (!IdValidateUtils.validateId(id)) {
            throw Status.INVALID_ARGUMENT.withDescription("invalid " + key.name()).asRuntimeException();
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
