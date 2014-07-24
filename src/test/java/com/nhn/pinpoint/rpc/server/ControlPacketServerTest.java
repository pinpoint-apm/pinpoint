package com.nhn.pinpoint.rpc.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Assert;
import org.junit.Test;

import com.nhn.pinpoint.rpc.packet.ControlRegisterAgentConfirmPacket;
import com.nhn.pinpoint.rpc.packet.ControlRegisterAgentPacket;
import com.nhn.pinpoint.rpc.util.ControlMessageEnDeconderUtils;

/**
 * @author koo.taejin
 */
public class ControlPacketServerTest {

	@Test
	public void registerAgentTest() throws Exception {
		PinpointServerSocket pinpointServerSocket = new PinpointServerSocket();
		pinpointServerSocket.bind("127.0.0.1", 22234);

		Socket socket = new Socket("127.0.0.1", 22234);

		byte[] payload = ControlMessageEnDeconderUtils.encode(Collections.EMPTY_MAP);
		ControlRegisterAgentPacket packet = new ControlRegisterAgentPacket(1, payload);

		ByteBuffer bb = packet.toBuffer().toByteBuffer(0, packet.toBuffer().writerIndex());

		sendData(socket.getOutputStream(), bb.array());

		byte[] a = new byte[24];
		socket.getInputStream().read(a);

		ChannelBuffer cb = ChannelBuffers.wrappedBuffer(a);
		short packetType = cb.readShort();

		ControlRegisterAgentConfirmPacket p = ControlRegisterAgentConfirmPacket.readBuffer(packetType, cb);
		Map result = (Map) ControlMessageEnDeconderUtils.decode(p.getPayload());

		Assert.assertEquals(0, result.get("code"));

		socket.close();
		pinpointServerSocket.close();
	}

	private void sendData(OutputStream os, byte[] payload) throws IOException {
		os.write(payload);
		os.flush();
	}

}
