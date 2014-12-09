package com.navercorp.pinpoint.collector.cluster.route;

import org.jboss.netty.channel.Channel;

import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;

/**
 * @author koo.taejin <kr14910>
 */
public interface RouteEvent {

	TCommandTransfer getDeliveryCommand();
	
	Channel getSourceChannel();
	
}
