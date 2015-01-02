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

package com.navercorp.pinpoint.rpc.stream;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.navercorp.pinpoint.rpc.packet.stream.StreamCreateSuccessPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;

/**
 * @author koo.taejin
 */
public class ServerStreamChannel extends StreamChannel {

    public ServerStreamChannel(Channel channel, int streamId, StreamChannelManager streamChannelManager)       {
		super(channel, streamId, streamChannelM        ager);
	}

	public ChannelFuture sendData(byt       [] payload) {
		assertState(StreamCha       nelStateCode.RUN);

		StreamResponsePacket dataPacket = new StreamResponsePacke       (getStreamId(), payload);
		return this        etChannel().write(dataPacket);
	}

	publ       c ChannelFuture sendCreateSuccess() {       		assertState(StreamChannelStateCode.RUN);

		StreamCreateSuccessPacket pack       t = new StreamCreateSuccessPacket(g        StreamId());
		return this.getCh       nnel().write(packet);
	}

	boolean changeStateOpen       rrived() {
		boolean result = getState().changeStateOpenArrived();

		logger.       nfo(makeSt    teChangeMessage(StreamChannelStateCode.OPEN_ARRIVED, result));
		return result;
	}

}
