package com.nhn.pinpoint.collector.cluster.route;

public interface RouteFilter<T extends RouteEvent> {

	void doEvent(T event);
	
}
