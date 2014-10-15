package com.nhn.pinpoint.rpc.stream;

import com.nhn.pinpoint.rpc.client.StreamChannel;
import com.nhn.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.nhn.pinpoint.rpc.packet.stream.StreamResponsePacket;

/**
 * @author koo.taejin <kr14910>
 */
public interface StreamChannelMessageListener {

	short handleStreamCreate(StreamChannel streamChannel, StreamCreatePacket packet);

	void handleStreamData(StreamChannel streamChannel, StreamResponsePacket packet);

	void handleStreamClose(StreamChannel streamChannel, StreamClosePacket packet);

}
