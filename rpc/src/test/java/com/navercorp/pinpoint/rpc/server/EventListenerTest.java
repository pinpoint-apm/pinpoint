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

package com.navercorp.pinpoint.rpc.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.ChannelContext;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocket;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocketStateCode;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.SocketChannel;
import com.navercorp.pinpoint.rpc.server.SocketChannelStateChangeEventListener;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin
 */
public class EventListenerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	// Test for being possible to send messages in case of failure of registering packet ( return code : 2, lack of param    ter
	@Test
	public void registerAgentTest1() throws        xception {
		EventListener eventListner = new             EventListener();
		
		PinpointServerSocket pinpointServerSocket = new Pinpoi       tServerSocket(eventListner);
		pinpointServerSocket.setMes       ageListener(new SimpleListener());
		pinpoi       tServerSocket.bin       (          127.0.0.1", 22234);

		Socket sock          t = null;
		try {
			socket =          new Socket("127.0.0.1", 22234);
			sendAndReceiveSimplePacket(socket);
		                   Assert.assertEquals(eventListner.getCode(), Pinpoi          tServerSocketStateCode.RUN);
			
			int code = sendAndReceiveRegisterPacket(socket, getParams()          ;
			Assert.assertEquals(even       Listner          getCode(), Pinp             intServ                   rSocketStateCode.RUN_DUPLE             _COMMUNICATION);

			                      endAndReceiveSimplePacket(socket);
		} finally {
			if (socket != null) {
				socket.close();
			}

			if (pinpointServe       Socket != null) {
				pinpointServerSocket.close();
	       	}
		}
	}

	private int sendAndReceiveRegisterPacket(Socket socket, Map<String, Object> pr       perties) throws ProtocolException, IOException {
		sendRegisterPacket(socket.getOutputStream(), propert             es);
		ControlHandshakeResponsePacket pa        et = receiveRegisterConfirmPacket(socket.getInputStream());
		Map<Object, Object> result = (       ap<Object, Object>) ControlMessageEncodingUtil       .decode(packet.getPayload());
		
		return MapUtils.getInteger(result, "code",        1);
	}

	private void sendAndRece        eSimplePacket(Socket socket) throws ProtocolException, IOException {
		sendSimpleRequestPacket(socket.getOutputStream());
		Resp       nsePacket responsePacket = readSimpleResponsePacket(socket.g       tInputStream());
		Assert.assertNotNull(responsePacket);
	}

	privat        void sendRegisterPacket(OutputStream outputStream, Map<String, Object> propert       es) throws ProtocolException, I        xception {
		byte[] payload = ControlMessageEncodingUtils.encode(properties);
		ControlHandshakePacke        packet = new ControlHandshakePacket(1, payload);
       		ByteBuffer bb = pac       et.toBuffer().toByteBuffer(0, packet.toBuffer().writerIndex());
		sendData(outp       tStream, bb.array());
	}

	priv        e void sendSimpleRequestPacket(OutputStream outputStream) throws ProtocolException,       IOException {
		RequestP       cket packet = new        equestPacket(new byte[0]);
		packet.setRequestId(10);

		ByteBuffer bb = packet.toBuffer().toByteBuffer(0, packet.toBuffer().writer       ndex());
		sendData(outputStream, b       .array());
	}

	private void sendData(OutputStream out       utStream, byte[] payload) throw        IOException {
		outputStream.write(payload);
		outputStream.flush();
	}

	private ControlHand       hakeRespon        Packet receiveRegisterConfirmPacket(InputStream inputStream) throws ProtocolException, IOException {

		byte[]       payload = readData(inputStream);
		       hannelBuffer cb = ChannelBuffers.wrappedBuffer(payload       ;

		short packetType = cb.read       hort();

		ControlHandshakeResponsePacket packet = ControlHand       hakeRespon        Packet.readBuffer(packetType, cb);
		return packet;
	}

	private Re       ponsePacket readSim       leResponsePacket(InputStr          am inputStream) throws ProtocolExce          tion, IOException
		by          e[] payload = readData(input             tream);
		ChannelBuffer cb             = ChannelBuf                      ers.wrappedBuffer(payload);

		s       ort packetType = cb.rea       Short();

	          ResponsePacket packet = ResponsePacket       readBuffer(packetType, cb);
		return packet;
	}

	private b       te[] readData(InputStream inputStream) throws IOException {
		int availableSize = 0;

		for (int i = 0; i < 3; i++) {
			availableSize = inputStream.available();

			if (availableSize > 0) {
				break;
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		byte[] payload = new byte[availableSize];
		inputStream.read(payload);

		return payload;
	}
	
	private Map<String, Object> getParams() {
		Map<String, Object> properties = new HashMap<String, Object>();
		
        properties.put(AgentHandshakePropertyType.AGENT_ID.getName(), "agent");
        properties             put(AgentHa        shakePropertyType.APPLICATION_NAME.getName(), "applic       tion"       ;
        properties.put(AgentHandshakePropertyType.HOSTNAME.getNam             (),        hostname");
        properties.put(AgentHandshakePropertyType.IP.getName(),          "ip");
        properties.put(AgentHandshakePropert          Type.PID.getName(), 1111);
        properties.put(AgentHandshakeP                          pertyType.SERVICE_TYPE.getName(), 10);
        properties.          ut(AgentHandshakePropertyType.START_TIMES    AMP.getName(), System.currentTimeMillis());
        properties.p             t(AgentHandshakePropertyType.VERSION.getName(), "1.0");
		
		return p       operties;
	}

	class SimpleListener imp             em       nts ServerMessageListener {
		@Override
		public void handleSend(SendPacket sendPacket, SocketCh          nnel channel) {
             		}

		@Override
		public void handleReques          (Reque                   tPacket requestPacket, SocketChannel channel) {
			logger.info("handlerRequest {}", requestPacket, channel);
			channel.sendResponseMessage(requestPacket, requestPacket.getPayload());
		}
		
		@Override
		public HandshakeResponseCode handleHandshake(Map properties) {
			logger.info("handle Handshake {}", properties);
	        return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
		}
	}


	class EventListener implements SocketChannelStateChangeEventListener {

		private PinpointServerSocketStateCode code;
		
		@Override
		public void eventPerformed(ChannelContext channelContext, PinpointServerSocketStateCode stateCode) {
			this.code = stateCode;
		}

		public PinpointServerSocketStateCode getCode() {
			return code;
		}
		
	}
	
}
