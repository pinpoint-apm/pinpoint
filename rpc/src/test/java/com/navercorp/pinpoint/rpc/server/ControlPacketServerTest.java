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
import java.util.Collections;
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
import com.navercorp.pinpoint.rpc.server.PinpointServerSocket;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.SocketChannel;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;
import com.navercorp.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin
 */
public class ControlPacketServerTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	// Test for being possible to send messages in case of failure of registering packet ( return code : 2, lack of param    ter
	@Test
	public void registerAgentTest1() throws        xception {
		PinpointServerSocket pinpointServerSocket = new Pinpoi       tServerSocket();
		pinpointServerSocket.setMessageListener       new SimpleListener());
		pinpointServerSock       t.bind("127.0.0.1       ,          22234);

		Socket socket = null;
	                   try {
			socket = new So                   ket("127.0.0.1", 22234);
			
			send          ndReceiveSimplePacket(so          ket);
			
			int code= sendAn       Receive          egisterPacket(s             cket);
                   		Assert.assertEquals(2, c             de);

			sendAndRecei                      eSimplePacket(socket);
		} finally {
			if (socket != null) {
				socket.close();
			}

			if (p    npo    ntServerSocket != null) {
				pinpointServerSocke       .close();
			}
		}
	}

	// Test for being possible to send messages       in case of success of registering packet ( return code : 0
	@Test
	public void registerAgentTest2() t       rows Exception {
       	          inpointServerSocket pinpointServerS          cket = new PinpointServerSocke          ();
		pinpointServerSocket.setMessageListener(new Simp          eListener());
		pinpoint          erverSocket.bind("127.0.0.1",       22234);
		Socket socke              = null
		try {
			socket = new S             cket("127.0.0.1", 222                      4);

			sendAndReceiveSimplePacket(socket);

			int code= sendAndReceiveRegisterPacket(socket,     etP    rams());
			Assert.assertEquals(0, code);

			sen       AndReceiveSimplePacket(socket);
		} finally {
			if (socket != null        {
				socket.close();
			}

			if (pinpointServerSocket !        null) {
				pinpointServerSocket.close();
       		}
		}
	}

	// w       e           failure of registering and retryi          g to register, confirm to return same code          ( return code : 2
	@Tes
	public void registerAgentTest3(           throws Exception {
		P                   npointServerSocket pinpo       ntServe          Socket = new Pi             pointSe                   verSocket();
		pinpointSer             erSocket.setMessageLi                      tener(new SimpleListener());
		pinpointServerSocket.bind("127.0.0.1    , 22234);

		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 2223    );
    		int code = sendAndReceiveRegisterPacket(socket)
			Assert.assertEquals(2, code);
			
			code = sendAndReceiveRegis       erPacket(socket);
			Assert.assertEquals(2, code);
			
			       endAndReceiveSimplePacket(socket);
		} fina       ly {
			if (socke                  = null) {
				socket.close();
			}
			if (pinpointServerSocket !           null) {
				pinpointServerSocket.close();
			}
		}
	}
          	// after success of reg          stering, when success message           re sent repeatedly.
	// test 1) confirm to return s          ccess code, 2) confirm t           return already success code.       	@Test
          public void reg             sterAge                   tTest4() throws Exception
		PinpointServerSock                         t pinpointServerSocket = new PinpointServerSocket();
		pinpointServerSocket.setMessageL       stener(new SimpleListener());
		pinpointServerSocket.bind("127.        0.1", 22234);

		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 22234);
			sendAndReceiveS       mplePacket(socket);

			int code = sendAndReceiveRegi       terPacket(socket, getParams());
			Assert.assertEquals(0, code);

			sendAndReceiveSimpleP       cket(socket);

			code = sendAndReceiveRegisterPacket(socket, getParams());
			Assert.assertEquals(1, c             de);

			sendAndReceiveSimplePacket(sock        );
		} finally {
			if (socket != null) {
				socket.close();
			}

			if (pinpointServerSoc       et != null) {
				pinpointServerSocket.close()
			}
		}
	}

	
	private int sendAndReceiveRegisterPacket(Socket socket) throw        ProtocolException, IOException {        	return sendAndReceiveRegisterPacket(socket, Collections.EMPTY_MAP);
	}

	private int sendAndReceiveRegisterPack       t(Socket socket, Map properties) throws ProtocolException, I       Exception {
		sendRegisterPacket(socket.getOutputStream(), propertie       );
		ControlHandshakeResponsePacket packet = receiveRegisterConfirmPacket(socke       .getInputStream());
		Map<Objec         Object> result = (Map<Object, Object>) ControlMessageEncodingUtils.decode(packet.getPayload());
		
	       return MapUtils.getInteger(result, "code", -1);
	}
	private void sendAn       ReceiveSimplePacket(Socket socket) throws ProtocolException, IOException {
		se       dSimpleRequestPacket(socket.get        tputStream());
		ResponsePacket responsePacket = readSimpleResponsePacket(socket.ge       InputStream());
		Assert       assertNotNull(res        nsePacket);
	}

	private void sendRegisterPacket(OutputStream outputStream, Map properties) throws ProtocolException, IOException {       		byte[] payload = ControlMessageEn       odingUtils.encode(properties);
		ControlHandshakePacke        packet = new ControlHandshakeP       cket(1, payload);

		ByteBuffer bb = packet.toBuffer().toByteBuffer(0, packet.toBuffer().write       Index());
        sendData(outputStream, bb.array());
	}

	private void sendSimpleRequestPacket(OutputStream outputStream) throw        ProtocolException, IOException {
	       RequestPacket packet = new RequestPacket(new byte[0]);       		packet.setRequestId(10);

		B       teBuffer bb = packet.toBuffer().toByteBuffer(0, packet.toBuffe       ().writerI        ex());
		sendData(outputStream, bb.array());
	}

	private void send       ata(OutputStream ou       putStream, byte[] payload           throws IOException {
		outputStrea          .write(payload);
	                                           outpu          Stream.flush();
	}

	private             ControlHandshakeResponsePa             ket receiveR                      gisterConfirmPacket(InputStream        nputStream) throws Prot       colExceptio         IOException {

		byte[] payload = readData(inputStre       m);
	       ChannelBuffer cb = ChannelBuffers.wrappedBuffer(payload);

		short              acke       Type = cb.readShort();

		ControlHandshakeResponsePacket packet = ControlHa          dshakeResponsePacket.readBuffer(packetType, cb);
		ret          rn packet;
	}

	private ResponsePacket readSimpleResponsePacket(I                          utStream inputStream) throws ProtocolException, IOExceptio           {
		byte[] payload          = readData(inputStream);
		ChannelBuffer cb = ChannelBuf                            ers.wrappedBuffer(payload);

		short packetType = cb.rea          Short();

		             esponsePacket packet = ResponsePacket.readBuffer(p                   cketType, cb);
		return packet;
	}

	private byte                ] readData(InputStre       m inputStream) throws IOExc       ption {
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

	class SimpleListener implements ServerMessageListener {
		@Override
		public void handleSend(SendPacket sendPacket, SocketChannel channel) {

		}

		@Override
		public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
			logger.info("handl             rRequest {}    {}", requestPacket, channel);
			channel.sendResponseMessage(requestPacket, requestPacket.getPayload());
		}
		
		@Override
		public HandshakeResponseCode handleHandshake(Map properties) {
			if (properties == null) {
			    return HandshakeResponseType.ProtocolError.PROTOCOL_ERROR;
			}
			
			boolean hasAllType = AgentHandshakePropertyType.hasAllType(properties);
			if (!hasAllType) {
				return HandshakeResponseType.PropertyError.PROPERTY_ERROR;
			}

			return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
		}
	}
	
	private Map getParams() {
		Map properties = new HashMap();
		
        properties.put(AgentHandshakePropertyType.AGENT_ID.getName(), "agent");
        properties.put(AgentHandshakePropertyType.APPLICATION_NAME.getName(), "application");
        properties.put(AgentHandshakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(AgentHandshakePropertyType.IP.getName(), "ip");
        properties.put(AgentHandshakePropertyType.PID.getName(), 1111);
        properties.put(AgentHandshakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(AgentHandshakePropertyType.START_TIMESTAMP.getName(), System.currentTimeMillis());
        properties.put(AgentHandshakePropertyType.VERSION.getName(), "1.0");
		
		return properties;
	}

}
