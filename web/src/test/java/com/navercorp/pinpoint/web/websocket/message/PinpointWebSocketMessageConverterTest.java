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

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class PinpointWebSocketMessageConverterTest {

    PinpointWebSocketMessageConverter messageConverter = new PinpointWebSocketMessageConverter();

    @Test
    public void requestMessageTest() throws Exception {
        String command = "command";
        Map parameters = new HashMap();
        parameters.put("test", "test");

        String textMessage = messageConverter.getRequestTextMessage(command, parameters);
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        Assert.assertNotNull(webSocketMessage);
        Assert.assertTrue(webSocketMessage instanceof RequestMessage);
        RequestMessage requestMessage = (RequestMessage) webSocketMessage;

        Assert.assertEquals(PinpointWebSocketMessageType.REQUEST, requestMessage.getType());
        Assert.assertEquals(command, requestMessage.getCommand());
        Assert.assertEquals(parameters, requestMessage.getParams());
    }

    @Test
    public void responseMessageTest() throws Exception {
        String command = "command";
        Map result = new HashMap();
        result.put("test", "test");

        String textMessage = messageConverter.getResponseTextMessage(command, result);
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        Assert.assertNotNull(webSocketMessage);
        Assert.assertTrue(webSocketMessage instanceof ResponseMessage);
        ResponseMessage responseMessage = (ResponseMessage) webSocketMessage;

        Assert.assertEquals(PinpointWebSocketMessageType.RESPONSE, responseMessage.getType());
        Assert.assertEquals(command, responseMessage.getCommand());
        Assert.assertEquals(result, responseMessage.getResult());
    }

    @Test
    public void sendMessageTest() throws Exception {
        String command = "command";
        Map parameters = new HashMap();
        parameters.put("test", "test");

        String textMessage = messageConverter.getSendTextMessage(command, parameters);
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        Assert.assertNotNull(webSocketMessage);
        Assert.assertTrue(webSocketMessage instanceof SendMessage);
        SendMessage sendMessage = (SendMessage) webSocketMessage;

        Assert.assertEquals(PinpointWebSocketMessageType.SEND, sendMessage.getType());
        Assert.assertEquals(command, sendMessage.getCommand());
        Assert.assertEquals(parameters, sendMessage.getParams());
    }

    @Test
    public void pingMessageTest() throws Exception {
        String textMessage = messageConverter.getPingTextMessage();
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        Assert.assertNotNull(webSocketMessage);
        Assert.assertTrue(webSocketMessage instanceof PingMessage);
        Assert.assertEquals(PinpointWebSocketMessageType.PING, webSocketMessage.getType());
    }


    @Test
    public void pongMessageTest() throws Exception {
        String textMessage = messageConverter.getPongTextMessage();
        PinpointWebSocketMessage webSocketMessage = messageConverter.getWebSocketMessage(textMessage);

        Assert.assertNotNull(webSocketMessage);
        Assert.assertTrue(webSocketMessage instanceof PongMessage);
        Assert.assertEquals(PinpointWebSocketMessageType.PONG, webSocketMessage.getType());
    }

}
