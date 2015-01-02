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
import com.navercorp.pinpoint.rpc.packet.PayloadPacket;
import com.navercorp.pinpoint.rpc.util.AssertUtils;

/**
 * @author koo.taejin
 */
public class StreamCreatePacket extends BasicStreamPacket {

    private final static short PACKET_TYPE = PacketType.APPLICATION_STREAM_CREATE

	private final byte[] payl    ad;

	public StreamCreatePacket(int streamChannelId, byte[] pa       load) {
		super(stre       mChannelId);

		AssertUtils.ass       rtNotNull(payload);        	this.p    yload = payload;
	}

	@Overr       de
	public shor        getPack    tType() {
		return PACKET_       YPE;
	}

	@        erride
    public byte[] getPayload() {
		       eturn payload;
	}

	@Override
	public ChannelBuffer        oBuffer() {
		ChannelBuffer hea       er = ChannelBuffers.buffer(2 + 4 +        );
		header.writeShort(getPacketType());
		heade        writeInt(getStreamChannelId());

		return PayloadPacket.appendPayload(header, paylo       d);
	}

	public static StreamC       eatePacket readBuffer(short p          cketType, ChannelBuf          er buf             er) {
		assert packetType == PACKET_TYPE

		if (buffer.readableBytes() < 8) {
			buffer.resetReaderI       dex();
			return n          ll;


		final int streamChannelId = buffer.readInt();
		final ChannelBuffer payload = Payl       adPacket.r    adPayload(buffer);
		if (payload == null) {
			return null;
		}

		final StreamCreatePacket packet = new StreamCreatePacket(streamChannelId, payload.array());
		return packet;
	}

}
