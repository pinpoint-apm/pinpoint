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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Map;

/**
 * @author Taejin Koo
 */
@JsonPropertyOrder({"type", "command", "result"})
public class ResponseMessage implements PinpointWebSocketMessage {

    private final String command;
    private final Map<String, Object> result;

    public ResponseMessage(String command, Map<String, Object> result) {
        this.command = command;
        this.result = result;
    }

    public String getCommand() {
        return command;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    @Override
    public PinpointWebSocketMessageType getType() {
        return PinpointWebSocketMessageType.RESPONSE;
    }

}

