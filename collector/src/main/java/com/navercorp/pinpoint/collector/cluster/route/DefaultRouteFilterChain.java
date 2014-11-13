package com.nhn.pinpoint.collector.cluster.route;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRouteFilterChain<T extends RouteEvent> implements RouteFilterChain<T> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final CopyOnWriteArrayList<RouteFilter<T>> filterList = new CopyOnWriteArrayList<RouteFilter<T>>();
	
	@Override
	public void addLast(RouteFilter<T> filter) {
		filterList.add(filter);
	}

	@Override
	public void doEvent(T event) {
		for (RouteFilter<T> filter : filterList) {
			try {
				filter.doEvent(event);
			} catch (Exception e) {
				if (logger.isWarnEnabled()) {
					logger.warn(filter.getClass().getSimpleName() + " filter occured exception. caused=" + e.getMessage() + ".", e);
				}
			}
		}
	}
	
}
