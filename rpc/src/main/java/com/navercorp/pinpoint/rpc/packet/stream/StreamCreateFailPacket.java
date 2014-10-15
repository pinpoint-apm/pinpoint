package com.nhn.pinpoint.rpc.packet.stream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.nhn.pinpoint.rpc.packet.PacketType;

/**
 * @author koo.taejin <kr14910>
 */
public class StreamCreateFailPacket extends BasicStreamPacket {

	private final static short PACKET_TYPE = PacketType.APPLICATION_STREAM_CREATE_FAIL;

	private final short code;

	public StreamCreateFailPacket(int streamChannelId, short code) {
		super(streamChannelId);

		this.code = code;
	}

	@Override
	public short getPacketType() {
		return PACKET_TYPE;
	}

	@Override
	public ChannelBuffer toBuffer() {
		ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 2);
		header.writeShort(getPacketType());
		header.writeInt(getStreamChannelId());
		header.writeShort(code);

		return header;
	}

	public static StreamCreateFailPacket readBuffer(short packetType, ChannelBuffer buffer) {
		assert packetType == PACKET_TYPE;

		if (buffer.readableBytes() < 6) {
			buffer.resetReaderIndex();
			return null;
		}

		final int streamChannelId = buffer.readInt();
		final short code = buffer.readShort();

		final StreamCreateFailPacket packet = new StreamCreateFailPacket(streamChannelId, code);
		return packet;
	}

	public short getCode() {
		return code;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append("{streamChannelId=").append(getStreamChannelId());
		sb.append(", ");
		sb.append("code=").append(getCode());
		sb.append('}');
		return sb.toString();
	}

}
