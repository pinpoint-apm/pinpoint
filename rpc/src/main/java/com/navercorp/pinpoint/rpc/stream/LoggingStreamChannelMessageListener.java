package com.nhn.pinpoint.rpc.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamResponsePacket;

/**
 * @author koo.taejin <kr14910>
 */
public class LoggingStreamChannelMessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingStreamChannelMessageListener.class);

	public static final ServerStreamChannelMessageListener SERVER_LISTENER = new Server();
	public static final ClientStreamChannelMessageListener CLIENT_LISTENER = new Client();

	static class Server implements ServerStreamChannelMessageListener {

		@Override
		public short handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet) {
			LOGGER.info("handleStreamCreate StreamChannel:{}, Packet:{}", streamChannelContext, packet);
			return 0;
		}

		@Override
		public void handleStreamClose(StreamChannelContext streamChannelContext, StreamClosePacket packet) {
			LOGGER.info("handleStreamClose StreamChannel:{}, Packet:{}", streamChannelContext, packet);
		}

	}

	static class Client implements ClientStreamChannelMessageListener {

		@Override
		public void handleStreamData(ClientStreamChannelContext streamChannelContext, StreamResponsePacket packet) {
			LOGGER.info("handleStreamData StreamChannel:{}, Packet:{}", streamChannelContext, packet);
		}

		@Override
		public void handleStreamClose(StreamChannelContext streamChannelContext, StreamClosePacket packet) {
			LOGGER.info("handleStreamClose StreamChannel:{}, Packet:{}", streamChannelContext, packet);
		}

	}
}
