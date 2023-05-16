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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Taejin Koo
 */
public class PinpointWebSocketMessageConverterTest {

    private final PinpointWebSocketMessageConverter messageConverter = new PinpointWebSocketMessageConverter();

    @Test
    public void requestMessageTest() throws Exception {
        String command = "command";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("test", "test");

        String textMessage = messageConverter.getRequestTextMessage(command, parameters);
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        assertNotNull(webSocketMessage);
        assertThat(webSocketMessage).isInstanceOf(RequestMessage.class);
        RequestMessage requestMessage = (RequestMessage) webSocketMessage;

        assertEquals(PinpointWebSocketMessageType.REQUEST, requestMessage.getType());
        assertEquals(command, requestMessage.getCommand());
        assertEquals(parameters, requestMessage.getParameters());
    }

    @Test
    public void responseMessageTest() throws Exception {
        String command = "command";
        Map<String, Object> result = new HashMap<>();
        result.put("test", "test");

        String textMessage = messageConverter.getResponseTextMessage(command, result);
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        assertNotNull(webSocketMessage);
        assertThat(webSocketMessage).isInstanceOf(ResponseMessage.class);
        ResponseMessage responseMessage = (ResponseMessage) webSocketMessage;

        assertEquals(PinpointWebSocketMessageType.RESPONSE, responseMessage.getType());
        assertEquals(command, responseMessage.getCommand());
        assertEquals(result, responseMessage.getResult());
    }

    @Test
    public void sendMessageTest() throws Exception {
        String command = "command";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("test", "test");

        String textMessage = messageConverter.getSendTextMessage(command, parameters);
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        assertNotNull(webSocketMessage);
        assertThat(webSocketMessage).isInstanceOf(SendMessage.class);
        SendMessage sendMessage = (SendMessage) webSocketMessage;

        assertEquals(PinpointWebSocketMessageType.SEND, sendMessage.getType());
        assertEquals(command, sendMessage.getCommand());
        assertEquals(parameters, sendMessage.getParameters());
    }

    @Test
    public void pingMessageTest() {
        String textMessage = messageConverter.getPingTextMessage();
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        assertNotNull(webSocketMessage);
        assertThat(webSocketMessage).isInstanceOf(PingMessage.class);
        assertEquals(PinpointWebSocketMessageType.PING, webSocketMessage.getType());
    }


    @Test
    public void pongMessageTest() {
        String textMessage = messageConverter.getPongTextMessage();
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        assertNotNull(webSocketMessage);
        assertThat(webSocketMessage).isInstanceOf(PongMessage.class);
        assertEquals(PinpointWebSocketMessageType.PONG, webSocketMessage.getType());
    }

    @Test
    public void UnknownMessageTest() {
        PinpointWebSocketMessage emptyString = messageConverter.getWebSocketMessage("");
        Assertions.assertSame(emptyString.getClass(), UnknownMessage.class);
        assertEquals(PinpointWebSocketMessageType.UNKNOWN, emptyString.getType());

        PinpointWebSocketMessage nullValue = messageConverter.getWebSocketMessage(null);
        Assertions.assertSame(nullValue.getClass(), UnknownMessage.class);
        assertEquals(PinpointWebSocketMessageType.UNKNOWN, nullValue.getType());

        PinpointWebSocketMessage emptyObject = messageConverter.getWebSocketMessage("{}");
        Assertions.assertSame(emptyObject.getClass(), UnknownMessage.class);
        assertEquals(PinpointWebSocketMessageType.UNKNOWN, emptyObject.getType());
    }


}
