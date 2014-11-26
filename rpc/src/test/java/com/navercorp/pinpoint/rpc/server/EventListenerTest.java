package com.nhn.pinpoint.rpc.server;

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

import com.nhn.pinpoint.rpc.control.ProtocolException;
import com.nhn.pinpoint.rpc.packet.ControlHandShakeResponsePacket;
import com.nhn.pinpoint.rpc.packet.ControlHandShakePacket;
import com.nhn.pinpoint.rpc.packet.HandShakeResponseCode;
import com.nhn.pinpoint.rpc.packet.HandShakeResponseType;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.ResponsePacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.util.ControlMessageEnDeconderUtils;
import com.nhn.pinpoint.rpc.util.MapUtils;

/**
 * @author koo.taejin
 */
public class EventListenerTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	// RegisterPacket 등록 실패한 경우에도 메시지 전달 가능 확인 (return code : 2 파라미터 부족)
	@Test
	public void registerAgentTest1() throws Exception {
		EventListener eventListner = new EventListener();
		
		PinpointServerSocket pinpointServerSocket = new PinpointServerSocket(eventListner);
		pinpointServerSocket.setMessageListener(new SimpleListener());
		pinpointServerSocket.bind("127.0.0.1", 22234);

		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 22234);
			sendAndReceiveSimplePacket(socket);
			Assert.assertEquals(eventListner.getCode(), PinpointServerSocketStateCode.RUN);
			
			int code = sendAndReceiveRegisterPacket(socket, getParams());
			Assert.assertEquals(eventListner.getCode(), PinpointServerSocketStateCode.RUN_DUPLEX_COMMUNICATION);

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

	private int sendAndReceiveRegisterPacket(Socket socket, Map<String, Object> properties) throws ProtocolException, IOException {
		sendRegisterPacket(socket.getOutputStream(), properties);
		ControlHandShakeResponsePacket packet = receiveRegisterConfirmPacket(socket.getInputStream());
		Map<Object, Object> result = (Map<Object, Object>) ControlMessageEnDeconderUtils.decode(packet.getPayload());
		
		return MapUtils.getInteger(result, "code", -1);
	}

	private void sendAndReceiveSimplePacket(Socket socket) throws ProtocolException, IOException {
		sendSimpleRequestPacket(socket.getOutputStream());
		ResponsePacket responsePacket = readSimpleResponsePacket(socket.getInputStream());
		Assert.assertNotNull(responsePacket);
	}

	private void sendRegisterPacket(OutputStream outputStream, Map<String, Object> properties) throws ProtocolException, IOException {
		byte[] payload = ControlMessageEnDeconderUtils.encode(properties);
		ControlHandShakePacket packet = new ControlHandShakePacket(1, payload);

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

	private ControlHandShakeResponsePacket receiveRegisterConfirmPacket(InputStream inputStream) throws ProtocolException, IOException {

		byte[] payload = readData(inputStream);
		ChannelBuffer cb = ChannelBuffers.wrappedBuffer(payload);

		short packetType = cb.readShort();

		ControlHandShakeResponsePacket packet = ControlHandShakeResponsePacket.readBuffer(packetType, cb);
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
	
	private Map<String, Object> getParams() {
		Map<String, Object> properties = new HashMap<String, Object>();
		
        properties.put(AgentHandShakePropertyType.AGENT_ID.getName(), "agent");
        properties.put(AgentHandShakePropertyType.APPLICATION_NAME.getName(), "application");
        properties.put(AgentHandShakePropertyType.HOSTNAME.getName(), "hostname");
        properties.put(AgentHandShakePropertyType.IP.getName(), "ip");
        properties.put(AgentHandShakePropertyType.PID.getName(), 1111);
        properties.put(AgentHandShakePropertyType.SERVICE_TYPE.getName(), 10);
        properties.put(AgentHandShakePropertyType.START_TIMESTAMP.getName(), System.currentTimeMillis());
        properties.put(AgentHandShakePropertyType.VERSION.getName(), "1.0");
		
		return properties;
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
		public HandShakeResponseCode handleHandShake(Map properties) {
			logger.info("handle HandShake {}", properties);
	        return HandShakeResponseType.Success.DUPLEX_COMMUNICATION;
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
