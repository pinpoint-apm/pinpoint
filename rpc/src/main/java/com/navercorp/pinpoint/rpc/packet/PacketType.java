package com.nhn.pinpoint.rpc.packet;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class PacketType {
    public static final short APPLICATION_SEND = 1;
    public static final short APPLICATION_TRACE_SEND = 2;
    public static final short APPLICATION_TRACE_SEND_ACK = 3;

    public static final short APPLICATION_REQUEST = 5;
    public static final short APPLICATION_RESPONSE = 6;


    public static final short APPLICATION_STREAM_CREATE = 10;
    public static final short APPLICATION_STREAM_CREATE_SUCCESS = 12;
    public static final short APPLICATION_STREAM_CREATE_FAIL = 14;

    public static final short APPLICATION_STREAM_CLOSE = 15;

    public static final short APPLICATION_STREAM_RESPONSE = 20;


    public static final short CONTROL_CLIENT_CLOSE = 100;
    public static final short CONTROL_SERVER_CLOSE = 110;

    // 컨트롤 패킷
    public static final short CONTROL_ENABLE_WORKER = 150;
    public static final short CONTROL_ENABLE_WORKER_CONFIRM = 151;

    // ping, pong의 경우 성능상 두고 다른 CONTROL은 이걸로 뺌
    public static final short CONTROL_PING = 200;
    public static final short CONTROL_PONG = 201;

    public static final short UNKNOWN = 500;

    public static final int PACKET_TYPE_SIZE = 2;
}
