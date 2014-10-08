package com.nhn.pinpoint.rpc.packet.stream;

/**
 * @author koo.taejin <kr14910>
 */
public abstract class BasicStreamPacket implements StreamPacket {

	private static final byte[] EMPTY_PAYLOAD = new byte[0];

	private final int streamChannelId;

	public BasicStreamPacket(int streamChannelId) {
		this.streamChannelId = streamChannelId;
	}

	public byte[] getPayload() {
		return EMPTY_PAYLOAD;
	}

	public int getStreamChannelId() {
		return streamChannelId;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append("{channelId=").append(streamChannelId);
		sb.append(", ");
		if (getPayload() == null || getPayload() == EMPTY_PAYLOAD) {
			sb.append("payload=null");
		} else {
			sb.append("payloadLength=").append(getPayload().length);
		}
		sb.append('}');
		return sb.toString();
	}

}
