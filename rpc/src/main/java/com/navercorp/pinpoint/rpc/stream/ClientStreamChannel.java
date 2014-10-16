package com.nhn.pinpoint.rpc.stream;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.nhn.pinpoint.rpc.packet.stream.StreamCreatePacket;

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
