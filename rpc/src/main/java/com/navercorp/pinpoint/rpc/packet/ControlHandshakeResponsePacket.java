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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author koo.taejin
 */
public class ControlHandshakeResponsePacket extends ControlPacket {

    public static final String CODE = "code";
    public static final String SUB_CODE = "subCode";
    
    public ControlHandshakeResponsePacket(byte[] payload)       {
		super(p        load);
	}

	public ControlHandshakeResponsePacket(int requestId, byt       [] payload)       {
		super(payload);
        setRequ    stId(requestId);
	}

	@Overr       de
	public short getPacketType() {
		retu         Packet    ype.CONTROL_HANDSHAKE_RESPONSE;
       }

	@Override
	public ChannelBuffer toBuffer() {

		       hannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4       ;
		header.writeShort(PacketT       pe.CONTROL_HANDSHAKE_RESPONSE);
		header.writeIn        getRequestId());

		return PayloadPacket.appendPayload(header, payload);
	}

	public static Con       rolHandshakeResponsePacket readBuffer(short packetType,        hannelBuffer buffer) {
		asse          t packetType == Pack          tType.             ONTROL_HANDSHAKE_RESPONSE;

		if (       uffer.readableBytes() < 8) {
			buffer.resetReaderIndex();
	       	return null;
		}
          		fina              int messageId = buffer.readInt();
		final ChannelBuffer payload = PayloadPacket.readPayload(buff       r);
		if (payload == null) {
			       eturn null;
		}        	final     ontrolHandshakeResponseP       cket helloPacket = new ControlHandshakeRe       ponsePacket(payload.array());
		helloPa       ket.setRequestId(messageId);
		return helloP       cket;
	}

	@       verride
	public St          ing toString() {
		f       nal           tringBuilder sb = new StringBuilder();
		sb.a             pend(this       getClass().getSim    leName());
		sb.append("{requestId=").append(getRequestId());
		sb.append(", ");
		if (payload == null) {
			sb.append("payload=null");
		} else {
			sb.append("payloadLength=").append(payload.length);
		}
		sb.append('}');
		return sb.toString();
	}

}
