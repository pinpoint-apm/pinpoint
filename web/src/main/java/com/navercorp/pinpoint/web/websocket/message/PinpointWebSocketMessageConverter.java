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
import com.navercorp.pinpoint.common.server.util.json.TypeRef;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class PinpointWebSocketMessageConverter {

    private static final String TYPE = "type";

    private static final String COMMAND = "command";
    private static final String PARAMETERS = "parameters";
    private static final String RESULT = "result";

    private static final Logger LOGGER = LogManager.getLogger(PinpointWebSocketMessageConverter.class);

    private final ObjectMapper mapper;

    private final PingPongMessage pingPongMessage;

    public PinpointWebSocketMessageConverter(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.pingPongMessage = PingPongMessage.newMessage(mapper);
    }

    public PinpointWebSocketMessage getWebSocketMessage(String message) {
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
        final JsonNode root = mapper.readTree(message);
        if (!root.isObject()) {
            return new UnknownMessage();
        }

        JsonNode type = root.path(TYPE);
        PinpointWebSocketMessageType messageType = PinpointWebSocketMessageType.getType(type.asText());
        return switch (messageType) {
            case PING -> new PingMessage();
            case PONG -> new PongMessage();
            case REQUEST -> readRequest(root);
            case RESPONSE -> readResponse(root);
            case SEND -> readSend(root);
            default -> new UnknownMessage();
        };
    }

    private PinpointWebSocketMessage readSend(JsonNode root) {
        String command = root.path(COMMAND).asText();
        JsonNode resultNode = root.path(PARAMETERS);
        Map<String, Object> parameterMap = readMap(resultNode);
        return new SendMessage(command, parameterMap);
    }

    private PinpointWebSocketMessage readResponse(JsonNode root) {
        String command = root.path(COMMAND).asText();

        JsonNode resultNode = root.path(RESULT);
        Map<String, Object> resultMap = readMap(resultNode);
        return new ResponseMessage(command, resultMap);
    }

    private PinpointWebSocketMessage readRequest(JsonNode root) {
        String command = root.path(COMMAND).asText();
        JsonNode parameterNode = root.path(PARAMETERS);
        Map<String, Object> parameterMap = readMap(parameterNode);

        return new RequestMessage(command, parameterMap);
    }

    private Map<String, Object> readMap(JsonNode parameterNode) {
        return mapper.convertValue(parameterNode, TypeRef.map());
    }

    public String getRequestTextMessage(String command, Map<String, Object> params) throws JsonProcessingException {
        RequestMessage request = new RequestMessage(command, params);

        return mapper.writeValueAsString(request);
    }

    public String getResponseTextMessage(String command, Map<String, Object> result) throws JsonProcessingException {
        ResponseMessage response = new ResponseMessage(command, result);

        return mapper.writeValueAsString(response);
    }

    public String getSendTextMessage(String command, Map<String, Object> params) throws JsonProcessingException {

        SendMessage message = new SendMessage(command, params);

        return mapper.writeValueAsString(message);
    }

    public String getPingTextMessage() {
        return pingPongMessage.ping();
    }

    public String getPongTextMessage() {
        return pingPongMessage.pong();
    }

}
