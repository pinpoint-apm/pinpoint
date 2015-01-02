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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;

/**
 * @author koo.taejin
 */
public class LoggingStreamChannelMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingStreamChannelMessageListener.class)

	public static final ServerStreamChannelMessageListener SERVER_LISTENER = new Serv    r();
	public static final ClientStreamChannelMessageListener CLIENT_LISTENER = new Cl    ent();

	static class Server implements ServerStreamChannelMessage       isten       r {

		@Override
		public short handleStreamCreate(ServerStreamChannelContext streamChannelContext, Strea          CreatePacket packet) {
			LOGGER.info("handleStreamCreate StreamChannel:{}, Packet:{}"           st             eamC       annelContext, packet);
			return 0;
		}

		@Override
		public void handleStreamClose(ServerStreamChann          lContext streamChannelContext, StreamClosePacket packet) {
			LOGGER.info("handleStre             mClose StreamChannel:{}, Packet:{}", streamChannelContext, packet)
		}
       	}

	static class Client implements ClientStreamChannelMessageListener {

		@Override
		public void hand          eStreamData(ClientStreamChannelContext streamChannelContext, StreamResponsePacket pa             ket)       {
			LOGGER.info("handleStreamData StreamChannel:{}, Packet:{}", streamChannelContext, packet);
		}

	          @Override
		public void handleStreamClose(ClientStreamChannelContext streamChannelCon          ext, StreamClosePacket packet) {
			LOGGER.info("handleStreamClose StreamChannel:{}, Packet:{}", streamChannelContext, packet);
		}

	}
}
