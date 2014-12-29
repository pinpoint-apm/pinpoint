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
					logger.warn(filter.getClass().getSimpleName() + " filter occured exception. Error:" + e.getMessage() + ".", e);
				}
			}
		}
	}
	
}
