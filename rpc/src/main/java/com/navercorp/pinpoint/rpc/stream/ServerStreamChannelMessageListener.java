package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;

/**
 * @author koo.taejin <kr14910>
 */
public interface ServerStreamChannelMessageListener {

	short handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet);

	void handleStreamClose(ServerStreamChannelContext streamChannelContext, StreamClosePacket packet);

}
