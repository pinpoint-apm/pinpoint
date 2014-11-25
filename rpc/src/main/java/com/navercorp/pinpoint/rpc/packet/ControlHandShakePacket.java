package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author koo.taejin
 */
public class ControlHandShakePacket extends ControlPacket {

	public ControlHandShakePacket(byte[] payload) {
		super(payload);
	}

	public ControlHandShakePacket(int requestId, byte[] payload) {
		super(payload);
		setRequestId(requestId);
	}

	@Override
	public short getPacketType() {
		return PacketType.CONTROL_HANDSHAKE;
	}

	@Override
	public ChannelBuffer toBuffer() {

		ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
		header.writeShort(PacketType.CONTROL_HANDSHAKE);
		header.writeInt(getRequestId());

		return PayloadPacket.appendPayload(header, payload);
	}

	public static ControlHandShakePacket readBuffer(short packetType, ChannelBuffer buffer) {
		assert packetType == PacketType.CONTROL_HANDSHAKE;

		if (buffer.readableBytes() < 8) {
			buffer.resetReaderIndex();
			return null;
		}

		final int messageId = buffer.readInt();
		final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
		if (payload == null) {
			return null;
		}
		final ControlHandShakePacket helloPacket = new ControlHandShakePacket(payload.array());
		helloPacket.setRequestId(messageId);
		return helloPacket;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append("{requestId=").append(getRequestId());
		sb.append(", ");
		if (payload == null) {
			sb.append("payload=null");
		} else {
			sb.append("payloadLength=").append(payload.length);
		}
		sb.append('}');
		return sb.toString();
	}

}
