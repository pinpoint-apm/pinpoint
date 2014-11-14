package com.nhn.pinpoint.collector.cluster.route;

import org.jboss.netty.channel.Channel;

import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;

public interface RouteEvent {

	TCommandTransfer getDeliveryCommand();
	
	int getRequestId();
	Channel getSourceChannel();
	
}
