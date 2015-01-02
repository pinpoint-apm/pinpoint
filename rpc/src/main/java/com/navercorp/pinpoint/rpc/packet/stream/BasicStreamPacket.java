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

package com.navercorp.pinpoint.rpc.packet.stream;

/**
 * @author koo.taejin
 */
public abstract class BasicStreamPacket implements StreamPacket {

    // Status Co    e
	public static final short SUCCESS       = 0;
	
	public static final short CHANNEL_CL    SE = 100;

	public static final short I    _ERROR = 110;
	public static final short     D_ILLEGAL = 111;
	public static final short     D_DUPLICATED = 112;
	public static final sh       rt ID_NOT_FOUND = 113;
	
	public static f    nal short STATE_ERROR = 120;
	public static     inal short STATE_NOT_RUN = 121;
	public stati     final short STATE_ILLEGAL = 129;

	publi     static final short TYPE_ERROR = 130;
	public static    final short TYPE_SERVER_UNSUPPORT = 131;
	    ublic static final short TYPE_CLIENT = 136
	public static final short TYPE_UNKOWN =     39;
	
	public static final short PACKET_ERROR    = 140;
	public static final short PACKET_UNKNOW        = 141;
	public static final short PACKET_UN       UPPORT = 142;
	
	public static final short UNKNWON_ERROR = 200;

	
	public static final short ROUTE_TYPE_ERROR = 330;
    public static final short ROUTE_TYPE_SERVER_UNSUPPORT = 331;
    public static final short ROUTE_TYPE_CLIENT = 336;
    public static final short ROUTE_TYPE_UNKOWN = 339;
    
    public static final short ROUTE_PACKET_ERROR = 340;
    public static final short ROUTE_PACKET_UNK    OWN = 341;
    public static final short ROUTE_PACKET_UNSUPPORT = 342;
	
    public static final short ROUTE_NOT_       OUND = 350;

    public static final short ROUTE_CONNE    TION_ERROR = 360;

	
	private sta    ic final byte[] EMPTY_PAYLOAD = new byte[0];
       	private final int streamChannelId;        	public BasicStreamPacket(       nt streamChannelI         {
		this.streamChannelId = str       amChannelId;
	}

	p        lic byt    [] getPayload() {
		retu       n EMPTY_PAYLOAD;
	}

	public int getStrea       ChannelId() {
		return streamChannelId;       	}

	@Override
	public String toString() {
		final        tringBuilder       sb = new StringBuilder();
		sb.append(this.getClass().ge          SimpleName());
		sb.       ppen          ("{streamChannelId=").append(streamChannelId);
		s             .append("        ");
		if (getPay    oad() == null || getPayload() == EMPTY_PAYLOAD) {
			sb.append("payload=null");
		} else {
			sb.append("payloadLength=").append(getPayload().length);
		}
		sb.append('}');
		return sb.toString();
	}

}
