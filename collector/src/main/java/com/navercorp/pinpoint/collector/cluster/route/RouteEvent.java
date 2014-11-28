package com.nhn.pinpoint.collector.cluster.route;

import org.jboss.netty.channel.Channel;

import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;

/**
 * @author koo.taejin <kr14910>
 */
public interface RouteEvent {

	TCommandTransfer getDeliveryCommand();
	
	Channel getSourceChannel();
	
}
