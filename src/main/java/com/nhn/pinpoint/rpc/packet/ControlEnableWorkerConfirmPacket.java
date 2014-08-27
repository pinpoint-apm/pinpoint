package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author koo.taejin
 */
public class ControlEnableWorkerConfirmPacket extends ControlPacket {

	public static final int SUCCESS = 0;
	public static final int ALREADY_REGISTER = 1;
	public static final int INVALID_PROPERTIES = 2;
	public static final int ILLEGAL_PROTOCOL = 3;
	public static final int UNKNOWN_ERROR = 4;
	
	public ControlEnableWorkerConfirmPacket(byte[] payload) {
		super(payload);
	}

	public ControlEnableWorkerConfirmPacket(int requestId, byte[] payload) {
		super(payload);
		setRequestId(requestId);
	}

	@Override
	public short getPacketType() {
		return PacketType.CONTROL_ENABLE_WORKER_CONFIRM;
	}

	@Override
	public ChannelBuffer toBuffer() {

		ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
		header.writeShort(PacketType.CONTROL_ENABLE_WORKER_CONFIRM);
		header.writeInt(getRequestId());

		return PayloadPacket.appendPayload(header, payload);
	}

	public static ControlEnableWorkerConfirmPacket readBuffer(short packetType, ChannelBuffer buffer) {
		assert packetType == PacketType.CONTROL_ENABLE_WORKER_CONFIRM;

		if (buffer.readableBytes() < 8) {
			buffer.resetReaderIndex();
			return null;
		}

		final int messageId = buffer.readInt();
		final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
		if (payload == null) {
			return null;
		}
		final ControlEnableWorkerConfirmPacket helloPacket = new ControlEnableWorkerConfirmPacket(payload.array());
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
