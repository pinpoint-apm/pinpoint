package com.nhn.pinpoint.rpc.stream;

import com.nhn.pinpoint.rpc.util.AssertUtils;

/**
 * @author koo.taejin <kr14910>
 */
public class ClientStreamChannelContext extends StreamChannelContext {

	private final ClientStreamChannel clientStreamChannel;
	private final ClientStreamChannelMessageListener clientStreamChannelMessageListener;

	public ClientStreamChannelContext(ClientStreamChannel clientStreamChannel, ClientStreamChannelMessageListener clientStreamChannelMessageListener) {
		AssertUtils.assertNotNull(clientStreamChannel);
		AssertUtils.assertNotNull(clientStreamChannelMessageListener);

		this.clientStreamChannel = clientStreamChannel;
		this.clientStreamChannelMessageListener = clientStreamChannelMessageListener;
	}

	@Override
	public ClientStreamChannel getStreamChannel() {
		return clientStreamChannel;
	}

	public ClientStreamChannelMessageListener getClientStreamChannelMessageListener() {
		return clientStreamChannelMessageListener;
	}

}
