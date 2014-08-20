package com.nhn.pinpoint.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DoNothingChannelStateEventListener implements SocketChannelStateChangeEventListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static final SocketChannelStateChangeEventListener INSTANCE = new DoNothingChannelStateEventListener();

	@Override
	public void eventPerformed(ChannelContext channelContext, PinpointServerSocketStateCode stateCode) {
		logger.warn("eventPerformed {}:", channelContext);
	}

}
