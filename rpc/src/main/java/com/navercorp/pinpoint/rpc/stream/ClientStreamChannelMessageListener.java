package com.nhn.pinpoint.rpc.stream;

import com.nhn.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamResponsePacket;

/**
 * @author koo.taejin <kr14910>
 */
public interface ClientStreamChannelMessageListener {

	void handleStreamData(ClientStreamChannelContext streamChannelContext, StreamResponsePacket packet);

	void handleStreamClose(ClientStreamChannelContext streamChannelContext, StreamClosePacket packet);

}
