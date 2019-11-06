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

package com.navercorp.pinpoint.rpc.packet.stream;

import com.navercorp.pinpoint.common.util.apache.IntHashMap;


/**
 * @author Taejin Koo
 */
public enum StreamCode {

    // Status Code
    OK((short) 0),

    UNKNWON_ERROR((short) 100),

    ID_ERROR((short) 110),
    ID_ILLEGAL((short) 111),
    ID_DUPLICATED((short) 112),
    ID_NOT_FOUND((short) 113),

    STATE_ERROR((short) 120),
    STATE_NOT_CONNECTED((short) 121),
    STATE_CLOSED((short) 122),

    TYPE_ERROR((short) 130),
    TYPE_UNKNOWN((short) 131),
    TYPE_UNSUPPORT((short) 132),

    PACKET_ERROR((short) 140),
    PACKET_UNKNOWN((short) 141),
    PACKET_UNSUPPORT((short) 142),

    CONNECTION_ERRROR((short) 150),
    CONNECTION_NOT_FOUND((short) 151),
    CONNECTION_TIMEOUT((short) 152),
    CONNECTION_UNSUPPORT((short) 153),

    ROUTE_ERROR((short)160);

    private final short value;
    private final static IntHashMap<StreamCode> CODE_MAP = initializeCodeMapping();

    StreamCode(short value) {
        this.value = value;
    }

    public static boolean isConnectionError(StreamCode streamCode) {
        if (CONNECTION_ERRROR == streamCode || CONNECTION_NOT_FOUND == streamCode || CONNECTION_TIMEOUT == streamCode || CONNECTION_UNSUPPORT == streamCode) {
            return true;
        }
        return false;
    }

    public static StreamCode getCode(short value) {
        StreamCode streamCode = CODE_MAP.get(value);
        if (streamCode != null) {
            return streamCode;
        }

        short codeGroup = (short) (value - (value % 10));
        streamCode = CODE_MAP.get(codeGroup);
        if (streamCode != null) {
            return streamCode;
        }

        return UNKNWON_ERROR;
    }

    private static IntHashMap<StreamCode> initializeCodeMapping() {
        IntHashMap<StreamCode> codeMap = new IntHashMap<StreamCode>();
        for (StreamCode streamCode : StreamCode.values()) {
            codeMap.put(streamCode.value, streamCode);
        }
        return codeMap;
    }

    public short value() {
        return value;
    }

}
