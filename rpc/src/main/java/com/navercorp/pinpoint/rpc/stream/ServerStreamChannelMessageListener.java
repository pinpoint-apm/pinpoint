package com.nhn.pinpoint.rpc.stream;

import com.nhn.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreatePacket;

/**
 * @author koo.taejin <kr14910>
 */
public interface ServerStreamChannelMessageListener {

	short handleStreamCreate(ServerStreamChannelContext streamChannelContext, StreamCreatePacket packet);

	void handleStreamClose(StreamChannelContext streamChannelContext, StreamClosePacket packet);

}
