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
 * @author koo.taejin
 */
public class ClientStreamChannel extends StreamChannel {

    public ClientStreamChannel(Channel channel, int streamId, StreamChannelManager streamChannelManager)       {
		super(channel, streamId, streamChannelM        ager);
	}

	public ChannelFuture sendCreate(byt       [] payload) {
		assertState(StreamChannelSta       eCode.OPEN_AWAIT);

		StreamCreatePacket packet = new StreamCreatePacke       (getStreamId(), payload);
		return         is.getChannel().write(pac       et);
	}

	boolean changeStateOpen() {
		boo       ean result = getState().changeStateOpen();

		logger.info(makeStateCh       ngeMessage        treamChannelStateCode.OPEN, re       ult));
		return result;
	}

	boolean changeState       penAwait() {
		boolean result = getState().changeStateOpenAwait();

		logge       .info(make    tateChangeMessage(StreamChannelStateCode.OPEN_AWAIT, result));
		return result;
	}

}
