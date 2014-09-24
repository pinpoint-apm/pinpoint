package com.nhn.pinpoint.rpc.client;

import org.jboss.netty.channel.Channel;

import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;

/**
 * @author koo.taejin
 */
public interface MessageListener {

	void handleSend(SendPacket sendPacket, Channel channel);

	void handleRequest(RequestPacket requestPacket, Channel channel);

}
