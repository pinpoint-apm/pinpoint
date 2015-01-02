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
public class StreamCreateSuccessPacket extends BasicStreamPacket {

    private final static short PACKET_TYPE = PacketType.APPLICATION_STREAM_CREATE_SUCCESS

	public StreamCreateSuccessPacket(int streamChannel       d) {
		super(stream        annelId    ;
	}

	@Override
	public sho       t getPacketType         {
		re    urn PACKET_TYPE;
	}

	@Override       	public ChannelBuffer toBuffer() {
		ChannelBuff       r header = ChannelBuffers.buffe       (2 + 4);
		header.writeShort(getPac       etType());        	header.writeInt(getStreamChannelId());

		return header;
	}

	public static StreamCreateS       ccessPacket readBuffer(short p       cketType, ChannelBuffer buffe          ) {
		assert packetT          pe ==              ACKET_TYPE;

		if (buffer.readableBytes()       < 4) {
			buffer.resetReaderIndex();
			return null;
		}

		final int streamChannelI        = buffer.    eadInt();

		final StreamCreateSuccessPacket packet = new StreamCreateSuccessPacket(streamChannelId);
		return packet;
	}

}
