package com.nhn.pinpoint.rpc.packet;

/**
 * @author emeroad
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

    public static final short CONTROL_PING = 200;
    public static final short CONTROL_PONG = 201;


    public static final int PACKET_TYPE_SIZE = 2;
}
