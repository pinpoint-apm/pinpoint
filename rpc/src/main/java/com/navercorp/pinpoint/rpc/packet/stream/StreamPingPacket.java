package com.nhn.pinpoint.rpc.packet.stream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.nhn.pinpoint.rpc.packet.PacketType;

/**
 * @author koo.taejin <kr14910>
 */
public class StreamPingPacket extends BasicStreamPacket {

	private final static short PACKET_TYPE = PacketType.APPLICATION_STREAM_PING;

	private final int requestId;
	
	public StreamPingPacket(int streamChannelId, int requestId) {
		super(streamChannelId);
		this.requestId = requestId;
	}

	@Override
	public short getPacketType() {
		return PACKET_TYPE;
	}

	@Override
	public ChannelBuffer toBuffer() {
		ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
		header.writeShort(getPacketType());
		header.writeInt(getStreamChannelId());
		header.writeInt(requestId);

		return header;
	}

	public static StreamPingPacket readBuffer(short packetType, ChannelBuffer buffer) {
		assert packetType == PACKET_TYPE;

		if (buffer.readableBytes() < 4) {
			buffer.resetReaderIndex();
			return null;
		}

		final int streamChannelId = buffer.readInt();
		final int requestId = buffer.readInt();

		final StreamPingPacket packet = new StreamPingPacket(streamChannelId, requestId);
		return packet;
	}

	public int getRequestId() {
		return requestId;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append("{streamChannelId=").append(getStreamChannelId());
		sb.append(", ");
		sb.append("streamChannelId=").append(getRequestId());
		sb.append('}');
		return sb.toString();
	}

}
