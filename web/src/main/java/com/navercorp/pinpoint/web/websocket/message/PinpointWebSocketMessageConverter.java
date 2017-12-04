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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
        this(JSON_SERIALIZER);
    }

    private PinpointWebSocketMessageConverter(ObjectMapper objectMapper) {
        pingMessage = createPingMessage(objectMapper);
        pongMessage = createPongMessage(objectMapper);
    }

    public PinpointWebSocketMessage getWebSocketMessage(String message) throws IOException {
        if (StringUtils.isEmpty(message)) {
            return new UnknownMessage();
        }
        try {
            return parseMessage(message);
        } catch (Exception e) {
            LOGGER.warn("getWebSocketMessage failed. message:{}", e.getMessage(), e);
        }

        return new UnknownMessage();
    }

    private PinpointWebSocketMessage parseMessage(String message) throws IOException {
        // TODO JacksonPolymorphicDeserialization
        // https://github.com/FasterXML/jackson-docs/wiki/JacksonPolymorphicDeserialization
        // JacksonPolymorphicDeserialization has security vulnerability
        // (CVE-2017-7525 jackson-databind: Deserialization vulnerability via readValue method of ObjectMapper)
        // will not work until the issue is completely resolved.
        final JsonNode root = JSON_SERIALIZER.readTree(message);
        if (!root.isObject()) {
            return new UnknownMessage();
        }

        JsonNode type = root.path(TYPE);
        PinpointWebSocketMessageType messageType = PinpointWebSocketMessageType.getType(type.asText());
        switch (messageType) {
            case PING:
                return new PingMessage();
            case PONG:
                return new PongMessage();
            case REQUEST:
                return readRequest(root);
            case RESPONSE:
                return readResponse(root);
            case SEND:
                return readSend(root);
        }
        return new UnknownMessage();
    }

    private PinpointWebSocketMessage readSend(JsonNode root) throws JsonProcessingException {
        String command = root.path(COMMAND).asText();
        JsonNode resultNode = root.path(PARAMETERS);
        Map parameterMap = readMap(resultNode);
        return new SendMessage(command, parameterMap);
    }

    private PinpointWebSocketMessage readResponse(JsonNode root) throws JsonProcessingException {
        String command = root.path(COMMAND).asText();

        JsonNode resultNode = root.path(RESULT);
        Map resultMap = readMap(resultNode);
        return new ResponseMessage(command, resultMap);
    }

    private PinpointWebSocketMessage readRequest(JsonNode root) throws JsonProcessingException {
        String command = root.path(COMMAND).asText();
        JsonNode parameterNode = root.path(PARAMETERS);
        Map parameterMap = readMap(parameterNode);

        return new RequestMessage(command, parameterMap);
    }

    private Map readMap(JsonNode parameterNode) throws JsonProcessingException {
        return JSON_SERIALIZER.treeToValue(parameterNode, Map.class);
    }

    public String getRequestTextMessage(String command, Map<String, Object> params) throws JsonProcessingException {
        RequestMessage request = new RequestMessage(command, params);

        return JSON_SERIALIZER.writeValueAsString(request);
    }

    public String getResponseTextMessage(String command, Map<String, Object> result) throws JsonProcessingException {
        ResponseMessage response = new ResponseMessage(command, result);

        return JSON_SERIALIZER.writeValueAsString(response);
    }

    public String getSendTextMessage(String command, Map<String, Object> params) throws JsonProcessingException {

        SendMessage message = new SendMessage(command, params);

        return JSON_SERIALIZER.writeValueAsString(message);
    }

    public String getPingTextMessage() {
        return pingMessage;
    }

    public String getPongTextMessage() {
        return pongMessage;
    }

    private String createPingMessage(ObjectMapper objectMapper) {
        PingMessage ping = new PingMessage();
        try {
            return objectMapper.writeValueAsString(ping);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("ping message create fail.", e);
        }
    }


    private String createPongMessage(ObjectMapper objectMapper) {
        PongMessage pong = new PongMessage();
        try {
            return objectMapper.writeValueAsString(pong);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("pong message create fail.", e);
        }

    }


}
