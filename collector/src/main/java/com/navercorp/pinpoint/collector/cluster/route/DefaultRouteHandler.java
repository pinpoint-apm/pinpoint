package com.nhn.pinpoint.collector.cluster.route;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.collector.cluster.ClusterPointLocator;
import com.nhn.pinpoint.collector.cluster.TargetClusterPoint;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.thrift.dto.command.TCommandTransfer;
import com.nhn.pinpoint.thrift.io.TCommandTypeVersion;

public class DefaultRouteHandler implements RouteHandler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final RouteFilterChain<RequestEvent> requestFilterChain;
	private final RouteFilterChain<ResponseEvent> responseFilterChain;

	private final ClusterPointLocator<TargetClusterPoint> targetClusterPointLocator;

	public DefaultRouteHandler(ClusterPointLocator<TargetClusterPoint> targetClusterPointLocator) {
		this.targetClusterPointLocator = targetClusterPointLocator;

		this.requestFilterChain = new DefaultRouteFilterChain<RequestEvent>();
		this.responseFilterChain = new DefaultRouteFilterChain<ResponseEvent>();
	}

	@Override
	public void addRequestFilter(RouteFilter<RequestEvent> filter) {
		this.requestFilterChain.addLast(filter);
	}

	@Override
	public void addResponseFilter(RouteFilter<ResponseEvent> filter) {
		this.responseFilterChain.addLast(filter);
	}

	@Override
	public RouteResult onRoute(RequestEvent event) {
		requestFilterChain.doEvent(event);

		RouteResult routeResult = onRoute0(event);

		responseFilterChain.doEvent(new ResponseEvent(event, routeResult));

		return routeResult;

	}

	private RouteResult onRoute0(RequestEvent event) {
		TBase requestObject = event.getRequestObject();
		if (requestObject == null) {
			return new RouteResult(RouteStatus.BAD_REQUEST);
		}

		TargetClusterPoint clusterPoint = findClusterPoint(event);
		if (clusterPoint == null) {
			return new RouteResult(RouteStatus.NOT_FOUND);
		}

		TCommandTypeVersion commandVersion = TCommandTypeVersion.getVersion(clusterPoint.gerVersion());
		if (!commandVersion.isSupportCommand(requestObject)) {
			return new RouteResult(RouteStatus.NOT_ACCEPTABLE);
		}

		Future<ResponseMessage> future = clusterPoint.request(event.getDeliveryCommand().getPayload());
		future.await();
		ResponseMessage responseMessage = future.getResult();

		if (responseMessage == null) {
			return new RouteResult(RouteStatus.AGENT_TIMEOUT);
		}

		return new RouteResult(RouteStatus.OK, responseMessage);

	}

	private TargetClusterPoint findClusterPoint(RequestEvent event) {
		TCommandTransfer deliveryCommand = event.getDeliveryCommand();

		String applicationName = deliveryCommand.getApplicationName();
		String agentId = deliveryCommand.getAgentId();
		long startTimeStamp = deliveryCommand.getStartTime();

		List<TargetClusterPoint> result = new ArrayList<TargetClusterPoint>();

		for (TargetClusterPoint targetClusterPoint : targetClusterPointLocator.getClusterPointList()) {
			if (!targetClusterPoint.getApplicationName().equals(applicationName)) {
				continue;
			}

			if (!targetClusterPoint.getAgentId().equals(agentId)) {
				continue;
			}

			if (!(targetClusterPoint.getStartTimeStamp() == startTimeStamp)) {
				continue;
			}

			result.add(targetClusterPoint);
		}

		if (result.size() == 1) {
			return result.get(0);
		}

		if (result.size() > 1) {
			logger.warn("Ambiguous ClusterPoint {}, {}, {} (Valid Agent list={}).", applicationName, agentId, startTimeStamp, result);
			return null;
		}

		return null;
	}

}
