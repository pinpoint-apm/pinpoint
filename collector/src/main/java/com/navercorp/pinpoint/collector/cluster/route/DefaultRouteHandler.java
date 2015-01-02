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

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.collector.cluster.ClusterPointLocator;
import com.navercorp.pinpoint.collector.cluster.TargetClusterPoint;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.thrift.io.TCommandTypeVersion;

/**
 * @author koo.taejin
 */
public class DefaultRouteHandler extends AbstractRouteHandler<RequestEvent> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	private final RouteFilterChain<RequestEvent> requestFilterC    ain;
	private final RouteFilterChain<ResponseEvent> responseFilte    Chain;

	public DefaultRouteHandler(ClusterPointLocator<TargetClusterPoint> targetClusterPoin    Locator) {
	    super(targetClusterP       intLocator);

		this.requestFilterChain = new DefaultRouteFilterCh       in<RequestEvent>();
		this.responseFilterChain = new DefaultRouteFil        rChain<    esponseEvent>();
	}

	@Override
	public void addRequestFilter(       outeFilter<RequestEvent> filter) {
	        his.req    estFilterChain.addLast(filter);
	}

	@Override
	public void addR       sponseFilter(RouteFilter<ResponseEven         filter     {
		this.responseFilterChain.addLast(filter);       	}

	@Override
	public RouteRes       lt onRoute(RequestEvent event) {
		requ       stFilterChain.doEvent(event);

		RouteResult routeResult = onRoute0(event);

		respons       FilterChain.doE        nt(new ResponseEvent(event, event.getRequestId()        routeResult));

		return routeResult;
	}

       private RouteResult onRo          te0(RequestEvent event) {
		TBase requestO             ject = event.getRequestObject();
		if (requestObject == null) {
			return        ew RouteResult(RouteSta          us.BAD_REQUEST);
		}

		TargetClusterPoi             t clusterPoint = findClusterPoint(event.getDeliveryCommand());
		if (clusterPoint == null)       {
			return new RouteResult(RouteStatus.NOT_FOUND)
		}

		TCommandTypeVersion commandVersion =              CommandTypeVersion.getVersion(clusterPoint.gerVersion());
		if (!commandVersion.isSupportC       mmand(reque       tObject)) {
			return new RouteResult(RouteStatus.       OT_ACCEPTABLE);
		}

		Fut          re<ResponseMessage> future = clusterPoint.re             uest(event.getDeliveryCommand().getPayload());
		fu    ure.await();
		ResponseMessage responseMessage = future.getResult();

		if (responseMessage == null) {
			return new RouteResult(RouteStatus.AGENT_TIMEOUT);
		}

		return new RouteResult(RouteStatus.OK, responseMessage);
	}

}
