package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;

/**
 * @author koo.taejin <kr14910>
 */
public interface ClientStreamChannelMessageListener {

	void handleStreamData(ClientStreamChannelContext streamChannelContext, StreamResponsePacket packet);

	void handleStreamClose(ClientStreamChannelContext streamChannelContext, StreamClosePacket packet);

}
