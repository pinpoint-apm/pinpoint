/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.websocket;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;

import java.util.*;

/**
 * @author Taejin Koo
 */
public enum ActiveThreadCountErrorType {

    UNSUPPORTED_VERSION((short) 111, "UNSUPPORTED VERSION", StreamCode.TYPE_UNSUPPORT.name()),
    CLUSTER_OPTION_NOT_SET((short) 121, "CLUSTER OPTION NOT SET", StreamCode.CONNECTION_UNSUPPORT.name()),

    TIMEOUT((short) 211, "TIMEOUT", TRouteResult.TIMEOUT.name()),

    NOT_FOUND((short) -1, "NOT FOUND", StreamCode.CONNECTION_NOT_FOUND.name()),
    CLUSTER_CHANNEL_CLOSED((short) -1, "CLUSTER CHANNEL CLOSED", StreamCode.STATE_CLOSED.name()),
    PINPOINT_INTERNAL_ERROR((short) -1, "PINPOINT INTERNAL ERROR");

    private final static Map<String, ActiveThreadCountErrorType> CODE_MAP = Collections.unmodifiableMap(initializeCodeMapping());

    private final short code;
    private final String message;
    private final List<String> errorMessageList;
    private static final String LINE_DELEMETER = "-";

    ActiveThreadCountErrorType(short code, String message, String... candidateErrorMessages) {
        this.code = code;
        this.message = message;

        this.errorMessageList = asList(candidateErrorMessages);
    }

    private List<String> asList(String[] candidateErrorMessages) {
        if (ArrayUtils.isEmpty(candidateErrorMessages)) {
            return Collections.emptyList();
        }

        return Arrays.asList(candidateErrorMessages);
    }

    public short getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getErrorMessageList() {
        return errorMessageList;
    }

    private static Map<String, ActiveThreadCountErrorType> initializeCodeMapping() {
        Map<String, ActiveThreadCountErrorType> codeMap = new HashMap<>();
        for (ActiveThreadCountErrorType errorType : ActiveThreadCountErrorType.values()) {

            List<String> errorMessageList = errorType.getErrorMessageList();
            for (String errorMessage : errorMessageList) {
                codeMap.put(errorMessage, errorType);
            }

        }
        return codeMap;
    }

    public static ActiveThreadCountErrorType getType(String errorMessage) {
        ActiveThreadCountErrorType errorType = CODE_MAP.get(errorMessage);
        if (errorType == null) {
            return PINPOINT_INTERNAL_ERROR;
        }
        return errorType;
    }

}
