/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocket;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.SocketChannel;
import com.navercorp.pinpoint.thrift.dto.TApiMetaData;

/**
 * @author emeroad
 */
public class TcpDataSenderReconnectTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int PORT = 10050;
    public static final String HOST = "127.0.0.1";

    private int send;

    public PinpointServerSocket serverStart() {
        PinpointServerSocket server = new PinpointServerSocket();
        server.setMessageListener(new ServerMessageListener() {

            @Override
            public void handleSend(SendPacket sendPacket, SocketChannel channel) {
                logger.info("handleSend:{}", sendPacket);
                send++;
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
                logger.info("handleRequest:{}", requestPacket);
            }

          		@          verride
			public HandshakeResponseCode handleHandshake(             ap properties) {
				return HandshakeResponseType.          uccess.DUPLEX_COMMUNICATION;
			}
        });
        server.bind(HOST, PORT);
        return server;
    }


    @Test
    public void connectAndSend() throws InterruptedException {
        PinpointServerSocket old = serverStart();

        PinpointSocketFactory socketFactory = createPinpointSocketFactory();
        PinpointSocket socket = createPinpointSocket(HOST, PORT, socketFactory);

        TcpDataSender sender = new TcpDataSender(socket);
        Thread.sleep(500);
        old.close();

        Thread.sleep(500);
        logger.info("Server start------------------");
        PinpointServerSocket pinpointServerSocket = serverStart();

        Thread.sleep(5000);
        logger.info("sendMessage------------------");
        sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));

        Thread.sleep(500);
        logger.info("sender stop------------------");
        sender.stop();

        pinpointServerSocket.close();
        socket.close();
        socketFactory.release();
    }
    
    private PinpointSocketFacto    y createPinpointSocketFactory() {
    	PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setTimeoutMillis(1000 * 5);
        pinpointSocketFactory.setProperties(Collections.EMPTY_MAP    ;

        return pinpointSocketFactory;
	}

    
    private PinpointSocket createPinpointSocket(String host, in     port, PinpointSocketFactory fa    tory) {
    	PinpointSocket socket = null;
    	for (int i = 0; i < 3; i++) {
            try {
                socket = factory.connect(host, port);
                logger.info("tcp connect success:{}/{}", host, port);
                return socket;
               } catch (PinpointSocketException e) {
            	logger.warn("tcp connect fail:{}/{} try reconnect, retryC    unt:{}", host, port, i);
            }
        }
    	logger.warn("change background tcp connect mode  {}/{} ", host, port);
           socket = factory.scheduledConnect(host, port);
    	
        return socket;
    }
}
