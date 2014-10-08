package com.nhn.pinpoint.rpc.packet.stream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.nhn.pinpoint.rpc.packet.PacketType;

/**
 * @author koo.taejin <kr14910>
 */
public class StreamCreateSuccessPacket extends BasicStreamPacket {

	private final static short PACKET_TYPE = PacketType.APPLICATION_STREAM_CREATE_SUCCESS;

	public StreamCreateSuccessPacket(int streamChannelId) {
		super(streamChannelId);
	}

	@Override
	public short getPacketType() {
		return PACKET_TYPE;
	}

	@Override
	public ChannelBuffer toBuffer() {
		ChannelBuffer header = ChannelBuffers.buffer(2 + 4);
		header.writeShort(getPacketType());
		header.writeInt(getStreamChannelId());

		return header;
	}

	public static StreamCreateSuccessPacket readBuffer(short packetType, ChannelBuffer buffer) {
		assert packetType == PACKET_TYPE;

		if (buffer.readableBytes() < 4) {
			buffer.resetReaderIndex();
			return null;
		}

		final int streamChannelId = buffer.readInt();

		final StreamCreateSuccessPacket packet = new StreamCreateSuccessPacket(streamChannelId);
		return packet;
	}

}
