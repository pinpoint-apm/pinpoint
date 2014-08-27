package com.nhn.pinpoint.rpc.server;

public interface SocketChannelStateChangeEventListener {
	
	void eventPerformed(ChannelContext channelContext, PinpointServerSocketStateCode stateCode);

}
