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

package com.navercorp.pinpoint.web.websocket.message;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public enum PinpointWebSocketMessageType {

    REQUEST,
    RESPONSE,
    SEND,
    PING,
    PONG,
    UNKNOWN;

    private static final Set<PinpointWebSocketMessageType> MESSAGE_TYPES = EnumSet.allOf(PinpointWebSocketMessageType.class);

    public static PinpointWebSocketMessageType getType(String name) {
        for (PinpointWebSocketMessageType type : MESSAGE_TYPES) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return UNKNOWN;
    }

}
