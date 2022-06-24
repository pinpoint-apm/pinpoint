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

        Assertions.assertNotNull(webSocketMessage);
        Assertions.assertTrue(webSocketMessage instanceof RequestMessage);
        RequestMessage requestMessage = (RequestMessage) webSocketMessage;

        Assertions.assertEquals(PinpointWebSocketMessageType.REQUEST, requestMessage.getType());
        Assertions.assertEquals(command, requestMessage.getCommand());
        Assertions.assertEquals(parameters, requestMessage.getParameters());
    }

    @Test
    public void responseMessageTest() throws Exception {
        String command = "command";
        Map<String, Object> result = new HashMap<>();
        result.put("test", "test");

        String textMessage = messageConverter.getResponseTextMessage(command, result);
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        Assertions.assertNotNull(webSocketMessage);
        Assertions.assertTrue(webSocketMessage instanceof ResponseMessage);
        ResponseMessage responseMessage = (ResponseMessage) webSocketMessage;

        Assertions.assertEquals(PinpointWebSocketMessageType.RESPONSE, responseMessage.getType());
        Assertions.assertEquals(command, responseMessage.getCommand());
        Assertions.assertEquals(result, responseMessage.getResult());
    }

    @Test
    public void sendMessageTest() throws Exception {
        String command = "command";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("test", "test");

        String textMessage = messageConverter.getSendTextMessage(command, parameters);
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        Assertions.assertNotNull(webSocketMessage);
        Assertions.assertTrue(webSocketMessage instanceof SendMessage);
        SendMessage sendMessage = (SendMessage) webSocketMessage;

        Assertions.assertEquals(PinpointWebSocketMessageType.SEND, sendMessage.getType());
        Assertions.assertEquals(command, sendMessage.getCommand());
        Assertions.assertEquals(parameters, sendMessage.getParameters());
    }

    @Test
    public void pingMessageTest() throws Exception {
        String textMessage = messageConverter.getPingTextMessage();
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        Assertions.assertNotNull(webSocketMessage);
        Assertions.assertTrue(webSocketMessage instanceof PingMessage);
        Assertions.assertEquals(PinpointWebSocketMessageType.PING, webSocketMessage.getType());
    }


    @Test
    public void pongMessageTest() throws Exception {
        String textMessage = messageConverter.getPongTextMessage();
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        Assertions.assertNotNull(webSocketMessage);
        Assertions.assertTrue(webSocketMessage instanceof PongMessage);
        Assertions.assertEquals(PinpointWebSocketMessageType.PONG, webSocketMessage.getType());
    }

    @Test
    public void UnknownMessageTest() throws Exception {
        PinpointWebSocketMessage emptyString = messageConverter.getWebSocketMessage("");
        Assertions.assertSame(emptyString.getClass(), UnknownMessage.class);
        Assertions.assertEquals(PinpointWebSocketMessageType.UNKNOWN, emptyString.getType());

        PinpointWebSocketMessage nullValue = messageConverter.getWebSocketMessage(null);
        Assertions.assertSame(nullValue.getClass(), UnknownMessage.class);
        Assertions.assertEquals(PinpointWebSocketMessageType.UNKNOWN, nullValue.getType());

        PinpointWebSocketMessage emptyObject = messageConverter.getWebSocketMessage("{}");
        Assertions.assertSame(emptyObject.getClass(), UnknownMessage.class);
        Assertions.assertEquals(PinpointWebSocketMessageType.UNKNOWN, emptyObject.getType());
    }


}
