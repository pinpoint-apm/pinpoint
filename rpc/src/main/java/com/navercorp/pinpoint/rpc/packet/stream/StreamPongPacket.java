package com.nhn.pinpoint.rpc.packet.stream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.nhn.pinpoint.rpc.packet.PacketType;

/**
 * @author koo.taejin <kr14910>
 */
public class StreamPongPacket extends BasicStreamPacket {

	private final static short PACKET_TYPE = PacketType.APPLICATION_STREAM_PONG;

	private final int requestId;
	
	public StreamPongPacket(int streamChannelId, int requestId) {
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

	public static StreamPongPacket readBuffer(short packetType, ChannelBuffer buffer) {
		assert packetType == PACKET_TYPE;

		if (buffer.readableBytes() < 4) {
			buffer.resetReaderIndex();
			return null;
		}

		final int streamChannelId = buffer.readInt();
		final int requestId = buffer.readInt();

		final StreamPongPacket packet = new StreamPongPacket(streamChannelId, requestId);
		return packet;
	}

	public int getRequestId() {
		return requestId;
	}

}
