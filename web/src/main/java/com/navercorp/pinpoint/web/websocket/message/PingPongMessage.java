/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.websocket.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.server.util.json.JsonRuntimeException;

import java.util.Objects;


public class PingPongMessage {

    private final String ping;
    private final String pong;

    public static PingPongMessage newMessage(ObjectMapper mapper) {
        String ping = toJson(mapper, new PingMessage());
        String pong = toJson(mapper, new PongMessage());
        return new PingPongMessage(ping, pong);
    }

    public PingPongMessage(String ping, String pong) {
        this.ping = Objects.requireNonNull(ping, "ping");
        this.pong = Objects.requireNonNull(pong, "pong");
    }

    private static String toJson(ObjectMapper mapper, Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonRuntimeException(object + " message create fail.", e);
        }
    }

    public String ping() {
        return ping;
    }

    public String pong() {
        return pong;
    }
}
