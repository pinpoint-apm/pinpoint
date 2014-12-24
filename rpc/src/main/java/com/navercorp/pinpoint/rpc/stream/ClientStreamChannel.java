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

import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;

/**
 * @author koo.taejin <kr14910>
 */
public class ClientStreamChannel extends StreamChannel {

	public ClientStreamChannel(Channel channel, int streamId, StreamChannelManager streamChannelManager) {
		super(channel, streamId, streamChannelManager);
	}

	public ChannelFuture sendCreate(byte[] payload) {
		assertState(StreamChannelStateCode.OPEN_AWAIT);

		StreamCreatePacket packet = new StreamCreatePacket(getStreamId(), payload);
		return this.getChannel().write(packet);
	}

	boolean changeStateOpen() {
		boolean result = getState().changeStateOpen();

		logger.info(makeStateChangeMessage(StreamChannelStateCode.OPEN, result));
		return result;
	}

	boolean changeStateOpenAwait() {
		boolean result = getState().changeStateOpenAwait();

		logger.info(makeStateChangeMessage(StreamChannelStateCode.OPEN_AWAIT, result));
		return result;
	}

}
