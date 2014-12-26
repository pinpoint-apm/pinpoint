/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.cluster.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author koo.taejin
 */
public class LoggingFilter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	class RequestFilter implements RouteFilter<RequestEvent> {

		@Override
		public void doEvent(RequestEvent event) {
			logger.warn("{} doEvent {}.", this.getClass().getSimpleName(), event);
		}

	}
	
	class StreamCreateFilter implements RouteFilter<StreamEvent> {

        @Override
        public void doEvent(StreamEvent event) {
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
	
	public StreamCreateFilter getStreamCreateFilter() {
	    return new StreamCreateFilter();
	}

	public ResponseFilter getResponseFilter() {
		return new ResponseFilter();
	}

}
