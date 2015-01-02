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
public class ControlHandshakePacket extends ControlPacket {

    public ControlHandshakePacket(byte[] payload)       {
		super(p        load);
	}

	public ControlHandshakePacket(int requestId, byt       [] payload)       {
		super(payload);
        setRequ    stId(requestId);
	}

	@Overr       de
	public short getPacketType()
		retu    n PacketType.CONTROL_HANDSHAKE;
       }

	@Override
	public ChannelBuffer toBuffer() {

		       hannelBuffer header = ChannelBuffers.buffer(        + 4 + 4);
		header.writeShor       (PacketType.CONTROL_HANDSHAKE);
		header.writeIn        getRequestId());

		return PayloadPacket.appendPayload(header, payload);
	}

	public st       tic ControlHandshakePacket readBuffer(short pac       etType, ChannelBuffer buffer)          {
		assert packetTyp           == Pa             ketType.CONTROL_HANDSHAKE;

		if (       uffer.readableBytes() < 8) {
			buffer.resetReaderIndex();
	       	return null;
		}
          		fina              int messageId = buffer.readInt();
		final ChannelBuffer payload = PayloadPacket.       eadPayload(buffer);
		if (payloa        == null) {
			        turn nu    l;
		}
		final ControlHa       dshakePacket helloPacket = new ControlHan       shakePacket(payload.array());
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
