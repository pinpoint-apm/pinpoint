/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.packet;

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

    public static final short APPLICATION_STREAM_PING = 17;
    public static final short APPLICATION_STREAM_PONG = 18;
    
    public static final short APPLICATION_STREAM_RESPONSE = 20;

    
    public static final short CONTROL_CLIENT_CLOSE = 100;
    public static final short CONTROL_SERVER_CLOSE = 110;

    // control packet
    public static final short CONTROL_HANDSHAKE = 150;
    public static final short CONTROL_HANDSHAKE_RESPONSE = 151;

    // keep stay because of performance in case of ping and pong. others removed.
    // CONTROL_PING will be deprecated. caused : Two payload types are used in one control packet.
    // since 1.7.0, use CONTROL_PING_SIMPLE, CONTROL_PING_PAYLOAD
    @Deprecated
    public static final short CONTROL_PING = 200;
    public static final short CONTROL_PONG = 201;

    public static final short CONTROL_PING_SIMPLE = 210;
    public static final short CONTROL_PING_PAYLOAD = 211;

    public static final short UNKNOWN = 500;

    public static final int PACKET_TYPE_SIZE = 2;
}
