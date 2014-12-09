package com.navercorp.pinpoint.rpc.client;

import org.jboss.netty.channel.Channel;

import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;

/**
 * @author koo.taejin
 */
public interface MessageListener {

	void handleSend(SendPacket sendPacket, Channel channel);

	void handleRequest(RequestPacket requestPacket, Channel channel);

}
