package com.nhn.pinpoint.rpc.packet.stream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.nhn.pinpoint.rpc.packet.PacketType;

/**
 * @author koo.taejin <kr14910>
 */
public class StreamClosePacket extends BasicStreamPacket {

	private final static short PACKET_TYPE = PacketType.APPLICATION_STREAM_CLOSE;

	public static final short UNKNWON_ERROR = -1;

	public static final short SUCCESS = 0;
	
	public static final short ILLEGAL_STREAM_CHANNEL_ID = 101;
	public static final short DUPLICATE_STREAM_CHANNEL_ID = 102;
	
	public static final short ILLEGAL_CHANNEL_STATE = 111;
	
	private final short code;

	public StreamClosePacket(int streamChannelId, short code) {
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

	public static StreamClosePacket readBuffer(short packetType, ChannelBuffer buffer) {
		assert packetType == PACKET_TYPE;

		if (buffer.readableBytes() < 6) {
			buffer.resetReaderIndex();
			return null;
		}

		final int streamChannelId = buffer.readInt();
		final short code = buffer.readShort();

		final StreamClosePacket packet = new StreamClosePacket(streamChannelId, code);
		return packet;
	}

	public short getCode() {
		return code;
	}

}
