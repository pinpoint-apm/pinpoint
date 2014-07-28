package com.nhn.pinpoint.rpc.server;

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

import com.nhn.pinpoint.rpc.control.ProtocolException;
import com.nhn.pinpoint.rpc.packet.ControlRegisterAgentConfirmPacket;
import com.nhn.pinpoint.rpc.packet.ControlRegisterAgentPacket;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.StreamPacket;
import com.nhn.pinpoint.rpc.util.ControlMessageEnDeconderUtils;
import com.nhn.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin
 */
public class ControlPacketServerTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// RegisterPacket 등록 실패한 경우에도 메시지 전달 가능 확인 (return code : 2 파라미터 부족)
	@Test
	public void registerAgentTest1() throws Exception {
		PinpointServerSocket pinpointServerSocket = new PinpointServerSocket();
		pinpointServerSocket.setMessageListener(new SimpleListener());
		pinpointServerSocket.bind("127.0.0.1", 22234);

		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 22234);
			
			sendAndReceiveSimplePacket(socket);
			
			int code= sendAndReceiveRegisterPacket(socket);
			Assert.assertEquals(2, code);

			sendAndReceiveSimplePacket(socket);
		} finally {
			if (socket != null) {
				socket.close();
			}

			if (pinpointServerSocket != null) {
				pinpointServerSocket.close();
			}
		}
	}

	// RegisterPacket 등록 성공시 메시지 전달 가능 확인 (return code : 0)
	@Test
	public void registerAgentTest2() throws Exception {
		PinpointServerSocket pinpointServerSocket = new PinpointServerSocket();
		pinpointServerSocket.setMessageListener(new SimpleListener());
		pinpointServerSocket.bind("127.0.0.1", 22234);

		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 22234);

			sendAndReceiveSimplePacket(socket);

			int code= sendAndReceiveRegisterPacket(socket, getParams());
			Assert.assertEquals(0, code);

			sendAndReceiveSimplePacket(socket);
		} finally {
			if (socket != null) {
				socket.close();
			}

			if (pinpointServerSocket != null) {
				pinpointServerSocket.close();
			}
		}
	}

	// RegisterPacket 등록 실패한 경우 다시 Register Packet을 보낼 경우 동일한 메시지 던지는지 확인 (return code : 2 파라미터 부족)
	@Test
	public void registerAgentTest3() throws Exception {
		PinpointServerSocket pinpointServerSocket = new PinpointServerSocket();
		pinpointServerSocket.setMessageListener(new SimpleListener());
		pinpointServerSocket.bind("127.0.0.1", 22234);

		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 22234);
			int code = sendAndReceiveRegisterPacket(socket);
			Assert.assertEquals(2, code);
			
			code = sendAndReceiveRegisterPacket(socket);
			Assert.assertEquals(2, code);
			
			sendAndReceiveSimplePacket(socket);
		} finally {
			if (socket != null) {
				socket.close();
			}

			if (pinpointServerSocket != null) {
				pinpointServerSocket.close();
			}
		}
	}

	// RegisterPacket 등록 성공 메시지를 여러번 보낼 경우 최초는 성공, 두번쨰는  이미 성공 code를 받는지 확인
	// 이후 메시지 전달 가능 확인 
	@Test
	public void registerAgentTest4() throws Exception {
		PinpointServerSocket pinpointServerSocket = new PinpointServerSocket();
		pinpointServerSocket.setMessageListener(new SimpleListener());
		pinpointServerSocket.bind("127.0.0.1", 22234);

		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 22234);
			sendAndReceiveSimplePacket(socket);

			int code = sendAndReceiveRegisterPacket(socket, getParams());
			Assert.assertEquals(0, code);

			sendAndReceiveSimplePacket(socket);

			code = sendAndReceiveRegisterPacket(socket, getParams());
			Assert.assertEquals(1, code);

			sendAndReceiveSimplePacket(socket);
		} finally {
			if (socket != null) {
				socket.close();
			}

			if (pinpointServerSocket != null) {
				pinpointServerSocket.close();
			}
		}
	}

	
	private int sendAndReceiveRegisterPacket(Socket socket) throws ProtocolException, IOException {
		return sendAndReceiveRegisterPacket(socket, Collections.EMPTY_MAP);
	}

	private int sendAndReceiveRegisterPacket(Socket socket, Map properties) throws ProtocolException, IOException {
		sendRegisterPacket(socket.getOutputStream(), properties);
		ControlRegisterAgentConfirmPacket packet = receiveRegisterConfirmPacket(socket.getInputStream());
		Map result = (Map) ControlMessageEnDeconderUtils.decode(packet.getPayload());
		
		return MapUtils.get(result, "code", Integer.class, -1);
	}

	private void sendAndReceiveSimplePacket(Socket socket) throws ProtocolException, IOException {
		sendSimpleRequestPacket(socket.getOutputStream());
		ResponsePacket responsePacket = readSimpleResponsePacket(socket.getInputStream());
		Assert.assertNotNull(responsePacket);
	}

	private void sendRegisterPacket(OutputStream outputStream, Map properties) throws ProtocolException, IOException {
		byte[] payload = ControlMessageEnDeconderUtils.encode(properties);
		ControlRegisterAgentPacket packet = new ControlRegisterAgentPacket(1, payload);

		ByteBuffer bb = packet.toBuffer().toByteBuffer(0, packet.toBuffer().writerIndex());
		sendData(outputStream, bb.array());
	}

	private void sendSimpleRequestPacket(OutputStream outputStream) throws ProtocolException, IOException {
		RequestPacket packet = new RequestPacket(new byte[0]);
		packet.setRequestId(10);

		ByteBuffer bb = packet.toBuffer().toByteBuffer(0, packet.toBuffer().writerIndex());
		sendData(outputStream, bb.array());
	}

	private void sendData(OutputStream outputStream, byte[] payload) throws IOException {
		outputStream.write(payload);
		outputStream.flush();
	}

	private ControlRegisterAgentConfirmPacket receiveRegisterConfirmPacket(InputStream inputStream) throws ProtocolException, IOException {

		byte[] payload = readData(inputStream);
		ChannelBuffer cb = ChannelBuffers.wrappedBuffer(payload);

		short packetType = cb.readShort();

		ControlRegisterAgentConfirmPacket packet = ControlRegisterAgentConfirmPacket.readBuffer(packetType, cb);
		return packet;
	}

	private ResponsePacket readSimpleResponsePacket(InputStream inputStream) throws ProtocolException, IOException {
		byte[] payload = readData(inputStream);
		ChannelBuffer cb = ChannelBuffers.wrappedBuffer(payload);

		short packetType = cb.readShort();

		ResponsePacket packet = ResponsePacket.readBuffer(packetType, cb);
		return packet;
	}

	private byte[] readData(InputStream inputStream) throws IOException {
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
			logger.info("handlerRequest {}", requestPacket, channel);
			channel.sendResponseMessage(requestPacket, requestPacket.getPayload());
		}

		@Override
		public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {

		}
	}
	
	private Map getParams() {
		Map properties = new HashMap();
		
		properties.put(AgentProperties.KEY_AGENTID, "agent");
		properties.put(AgentProperties.KEY_APPLICATION_NAME, "application");
		properties.put(AgentProperties.KEY_HOSTNAME, "hostname");
		properties.put(AgentProperties.KEY_IP, "ip");
		properties.put(AgentProperties.KEY_PID, 1111);
		properties.put(AgentProperties.KEY_PORTS, 65535);
		properties.put(AgentProperties.KEY_SERVICE_TYPE, 10);
		properties.put(AgentProperties.KEY_START_TIME_MILLIS, System.currentTimeMillis());
		properties.put(AgentProperties.KEY_VERSION, "1.0");
		
		
		return properties;
	}

}
