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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class PinpointWebSocketMessageConverter {

    private static final String TYPE = "type";

    private static final String COMMAND = "command";
    private static final String PARAMETERS = "parameters";
    private static final String RESULT = "result";

    private static final Logger LOGGER = LoggerFactory.getLogger(PinpointWebSocketMessageConverter.class);

    private static final ObjectMapper JSON_SERIALIZER = new ObjectMapper();

    private final String pingMessage;
    private final String pongMessage;

    public PinpointWebSocketMessageConverter() {
        pingMessage = createPingMessage();
        pongMessage = createPongMessage();
    }

    public PinpointWebSocketMessage getWebSocketMessage(String message) throws IOException {
        try {
            Map responseMessage = JSON_SERIALIZER.readValue(message, Map.class);
            if (responseMessage == null) {
                return new UnknownMessage();
            }

            PinpointWebSocketMessageType messageType = PinpointWebSocketMessageType.getType((String) responseMessage.get(TYPE));
            switch (messageType) {
                case PING:
                    return new PingMessage();
                case PONG:
                    return new PongMessage();
                case REQUEST:
                    String command = (String) responseMessage.get(COMMAND);
                    Map parameters = (Map) responseMessage.get(PARAMETERS);
                    return new RequestMessage(command, parameters);
                case RESPONSE:
                    command = (String) responseMessage.get(COMMAND);
                    Map result = (Map) responseMessage.get(RESULT);
                    return new ResponseMessage(command, result);
                case SEND:
                    command = (String) responseMessage.get(COMMAND);
                    parameters = (Map) responseMessage.get(PARAMETERS);
                    return new SendMessage(command, parameters);
            }
        } catch (Exception e) {
            LOGGER.warn("getWebSocketMessage failed. message:{}", e.getMessage(), e);
        }

        return new UnknownMessage();
    }

    public String getRequestTextMessage(String command, Map<String, Object> params) throws JsonProcessingException {
        Map<String, Object> request = new HashMap<>(3);
        request.put(TYPE, PinpointWebSocketMessageType.REQUEST.name());
        request.put(COMMAND, command);
        request.put(PARAMETERS, params);

        return JSON_SERIALIZER.writeValueAsString(request);
    }

    public String getResponseTextMessage(String command, Map<String, Object> result) throws JsonProcessingException {
        Map<String, Object> request = new HashMap<>(3);
        request.put(TYPE, PinpointWebSocketMessageType.RESPONSE.name());
        request.put(COMMAND, command);
        request.put(RESULT, result);

        return JSON_SERIALIZER.writeValueAsString(request);
    }

    public String getSendTextMessage(String command, Map<String, Object> params) throws JsonProcessingException {
        Map<String, Object> request = new HashMap<>(3);
        request.put(TYPE, PinpointWebSocketMessageType.SEND.name());
        request.put(COMMAND, command);
        request.put(PARAMETERS, params);

        return JSON_SERIALIZER.writeValueAsString(request);
    }

    public String getPingTextMessage() {
        return pingMessage;
    }

    public String getPongTextMessage() {
        return pongMessage;
    }

    private String createPingMessage() {
        Map<String, Object> ping = new HashMap<>(1);
        ping.put(TYPE, PinpointWebSocketMessageType.PING.name());

        String pingTextMessage;

        try {
            pingTextMessage = JSON_SERIALIZER.writeValueAsString(ping);
        } catch (JsonProcessingException e) {
            pingTextMessage = createRawPingMessage();
        }

        return pingTextMessage;
    }

    private String createRawPingMessage() {
        String ping = PinpointWebSocketMessageType.PING.name();
        String emptyJsonMessage = createMessage(ping);

        return emptyJsonMessage;
    }

    private String createPongMessage() {
        Map<String, Object> ping = new HashMap<>(1);
        ping.put(TYPE, PinpointWebSocketMessageType.PONG.name());

        String pongTextMessage;

        try {
            pongTextMessage = JSON_SERIALIZER.writeValueAsString(ping);
        } catch (JsonProcessingException e) {
            pongTextMessage = createRawPongMessage();
        }

        return pongTextMessage;
    }

    private String createRawPongMessage() {
        String pong = PinpointWebSocketMessageType.PONG.name();
        String emptyJsonMessage = createMessage(pong);

        return emptyJsonMessage;
    }

    private String createMessage(String pingOrPong) {
        StringBuilder emptyJsonMessage = new StringBuilder();
        emptyJsonMessage.append('{');
        emptyJsonMessage.append('\"').append(TYPE).append('\"').append(':').append('\"').append(pingOrPong).append('\"');
        emptyJsonMessage.append('}');
        return emptyJsonMessage.toString();
    }


}
