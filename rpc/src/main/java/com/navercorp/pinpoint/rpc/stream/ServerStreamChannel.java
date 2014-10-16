package com.nhn.pinpoint.rpc.stream;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import com.nhn.pinpoint.rpc.packet.stream.StreamCreateSuccessPacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamResponsePacket;

/**
 * @author koo.taejin <kr14910>
 */
public class ServerStreamChannel extends StreamChannel {

	public ServerStreamChannel(Channel channel, int streamId, StreamChannelManager streamChannelManager) {
		super(channel, streamId, streamChannelManager);
	}

	public ChannelFuture sendData(byte[] payload) {
		assertState(StreamChannelStateCode.RUN);

		StreamResponsePacket dataPacket = new StreamResponsePacket(getStreamId(), payload);
		return this.getChannel().write(dataPacket);
	}

	public ChannelFuture sendCreateSuccess() {
		assertState(StreamChannelStateCode.RUN);

		StreamCreateSuccessPacket packet = new StreamCreateSuccessPacket(getStreamId());
		return this.getChannel().write(packet);
	}

	boolean changeStateOpenArrived() {
		boolean result = getState().changeStateOpenArrived();

		logger.info(makeStateChangeMessage(StreamChannelStateCode.OPEN_ARRIVED, result));
		return result;
	}

}
