package com.nhn.pinpoint.collector.cluster.route;


public interface RouteHandler {

	void addRequestFilter(RouteFilter<RequestEvent> filter);
	
	void addResponseFilter(RouteFilter<ResponseEvent> filter);
	
	RouteResult onRoute(RequestEvent event);

}
