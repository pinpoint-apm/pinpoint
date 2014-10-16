package com.nhn.pinpoint.rpc.stream;

/**
 * @author koo.taejin <kr14910>
 */
public class ServerStreamChannelContext extends StreamChannelContext {

	private ServerStreamChannel streamChannel;
	
	public ServerStreamChannelContext(ServerStreamChannel streamChannel) {
		this.streamChannel = streamChannel;
	}

	@Override
	public ServerStreamChannel getStreamChannel() {
		return streamChannel;
	}

}
