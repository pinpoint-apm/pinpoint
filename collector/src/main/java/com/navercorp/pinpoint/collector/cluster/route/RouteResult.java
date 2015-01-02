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

import com.navercorp.pinpoint.rpc.ResponseMessage;

/**
 * @author koo.taejin
 */
public class RouteResult {

    private final RouteStatus statu    ;
	private final ResponseMessage responseMess    ge;

	public RouteResult(RouteStatus s       atus) {
		this(        atus, null);
	}

	public RouteResult(RouteStatus status, ResponseMessag        responseMessage)       {
		this.status = status;
		this.re        onseMessage = responseMessage;       	}

	publi        RouteStatus getStatus() {
		return status;
       }

	public Response           ssage     etResponseMessage() {
		       eturn responseMessage
	}

	
	@Override
	public String toString() {
		return status.toString();
	}
	
}
