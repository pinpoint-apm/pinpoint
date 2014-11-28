package com.nhn.pinpoint.rpc.packet.stream;

/**
 * @author koo.taejin <kr14910>
 */
public abstract class BasicStreamPacket implements StreamPacket {

	// Status Code
	public static final short SUCCESS = 0;
	
	public static final short CHANNEL_CLOSE = 100;

	public static final short ID_ERROR = 110;
	public static final short ID_ILLEGAL = 111;
	public static final short ID_DUPLICATED = 112;
	public static final short ID_NOT_FOUND = 113;
	
	public static final short STATE_ERROR = 120;
	public static final short STATE_NOT_RUN = 121;
	public static final short STATE_ILLEGAL = 129;

	public static final short TYPE_ERROR = 130;
	public static final short TYPE_SERVER_UNSUPPORT = 131;
	public static final short TYPE_CLIENT = 136;
	public static final short TYPE_UNKOWN = 139;
	
	public static final short PACKET_ERROR = 140;
	public static final short PACKET_UNKNOWN = 141;
	public static final short PACKET_UNSUPPORT = 142;
	
	public static final short UNKNWON_ERROR = 200;

	
	public static final short ROUTE_TYPE_ERROR = 330;
    public static final short ROUTE_TYPE_SERVER_UNSUPPORT = 331;
    public static final short ROUTE_TYPE_CLIENT = 336;
    public static final short ROUTE_TYPE_UNKOWN = 339;
    
    public static final short ROUTE_PACKET_ERROR = 340;
    public static final short ROUTE_PACKET_UNKNOWN = 341;
    public static final short ROUTE_PACKET_UNSUPPORT = 342;
	
    public static final short ROUTE_NOT_FOUND = 350;

    public static final short ROUTE_CONNECTION_ERROR = 360;

	
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
		sb.append("{streamChannelId=").append(streamChannelId);
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
