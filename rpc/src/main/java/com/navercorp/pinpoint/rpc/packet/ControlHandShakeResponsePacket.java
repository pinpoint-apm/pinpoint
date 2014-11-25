package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author koo.taejin
 */
public class ControlHandShakeResponsePacket extends ControlPacket {

    public static final String CODE = "code";
    public static final String SUB_CODE = "subCode";
    
	public ControlHandShakeResponsePacket(byte[] payload) {
		super(payload);
	}

	public ControlHandShakeResponsePacket(int requestId, byte[] payload) {
		super(payload);
		setRequestId(requestId);
	}

	@Override
	public short getPacketType() {
		return PacketType.CONTROL_HANDSHAKE_RESPONSE;
	}

	@Override
	public ChannelBuffer toBuffer() {

		ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
		header.writeShort(PacketType.CONTROL_HANDSHAKE_RESPONSE);
		header.writeInt(getRequestId());

		return PayloadPacket.appendPayload(header, payload);
	}

	public static ControlHandShakeResponsePacket readBuffer(short packetType, ChannelBuffer buffer) {
		assert packetType == PacketType.CONTROL_HANDSHAKE_RESPONSE;

		if (buffer.readableBytes() < 8) {
			buffer.resetReaderIndex();
			return null;
		}

		final int messageId = buffer.readInt();
		final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
		if (payload == null) {
			return null;
		}
		final ControlHandShakeResponsePacket helloPacket = new ControlHandShakeResponsePacket(payload.array());
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
