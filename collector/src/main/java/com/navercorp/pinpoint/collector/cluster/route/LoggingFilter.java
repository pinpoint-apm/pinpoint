package com.nhn.pinpoint.collector.cluster.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingFilter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	class RequestFilter implements RouteFilter<RequestEvent> {

		@Override
		public void doEvent(RequestEvent event) {
			logger.warn("{} doEvent {}.", this.getClass().getSimpleName(), event);
		}

	}

	class ResponseFilter implements RouteFilter<ResponseEvent> {

		@Override
		public void doEvent(ResponseEvent event) {
			logger.warn("{} doEvent {}.", this.getClass().getSimpleName(), event);
		}

	}

	public RequestFilter getRequestFilter() {
		return new RequestFilter();
	}

	public ResponseFilter getResponseFilter() {
		return new ResponseFilter();
	}

}
