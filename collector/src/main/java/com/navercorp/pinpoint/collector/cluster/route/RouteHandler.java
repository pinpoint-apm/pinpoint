package com.nhn.pinpoint.collector.cluster.route;


/**
 * @author koo.taejin <kr14910>
 */
public interface RouteHandler<T extends RouteEvent> {

	void addRequestFilter(RouteFilter<T> filter);
	
	void addResponseFilter(RouteFilter<ResponseEvent> filter);
	
	RouteResult onRoute(T event);

}
