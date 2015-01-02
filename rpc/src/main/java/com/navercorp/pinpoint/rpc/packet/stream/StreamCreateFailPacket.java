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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.navercorp.pinpoint.rpc.packet.PacketType;

/**
 * @author koo.taejin
 */
public class StreamCreateFailPacket extends BasicStreamPacket {

    private final static short PACKET_TYPE = PacketType.APPLICATION_STREAM_CREATE_FAIL

	private final short c    de;

	public StreamCreateFailPacket(int streamChannelId, short       code) {
		super(stre       mChannelId);
        	this.c    de = code;
	}

	@Override
	p       blic short getP        ketType    ) {
		return PACKET_TYPE;
	}

	       Override
	public ChannelBuffer toBuffer() {
		Channe       Buffer header = ChannelBuffers.       uffer(2 + 4 + 2);
		header.writeSh       rt(getPacketType());
       	header.wr        eInt(getStreamChannelId());
		header.writeShort(code);

		return header;
	}

	public st       tic StreamCreateFailPacket rea       Buffer(short packetType, Chan          elBuffer buffer) {
	          assert             packetType == PACKET_TYPE;

		if (buffer       readableBytes() < 6) {
			buffer.re       etReaderIndex();
			return null;
		}

		final int streamChannelId = buffer.readInt()
		final s        rt code = buffer.readS       ort();

        final S    reamCreateFailPacket pac       et = new StreamCreateFailPacket(streamCha       nelId, code);
		return packet;
	}

	pub       ic short getCode() {
		return code;
	}

	@Override
	publ       c String toS       ring() {
		final StringBuilder sb       = new Strin       Builder();
		sb.a    pend(this.getClass().getSimpleName());
		sb.append("{streamChannelId=").append(getStreamChannelId());
		sb.append(", ");
		sb.append("code=").append(getCode());
		sb.append('}');
		return sb.toString();
	}

}
