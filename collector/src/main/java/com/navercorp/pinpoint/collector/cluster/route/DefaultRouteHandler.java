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

import com.navercorp.pinpoint.collector.cluster.ClusterPointLocator;
import com.navercorp.pinpoint.collector.cluster.TargetClusterPoint;
import com.navercorp.pinpoint.collector.cluster.route.filter.RouteFilter;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.thrift.TBase;

/**
 * @author koo.taejin
 * @author HyunGil Jeong
 */
public class DefaultRouteHandler extends AbstractRouteHandler<RequestEvent> {

    private final RouteFilterChain<RequestEvent> requestFilterChain;
    private final RouteFilterChain<ResponseEvent> responseFilterChain;

    public DefaultRouteHandler(ClusterPointLocator<TargetClusterPoint> targetClusterPointLocator,
            RouteFilterChain<RequestEvent> requestFilterChain,
            RouteFilterChain<ResponseEvent> responseFilterChain) {
        super(targetClusterPointLocator);

        this.requestFilterChain = requestFilterChain;
        this.responseFilterChain = responseFilterChain;
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
    public TCommandTransferResponse onRoute(RequestEvent event) {
        requestFilterChain.doEvent(event);

        TCommandTransferResponse routeResult = onRoute0(event);

        responseFilterChain.doEvent(new ResponseEvent(event, event.getRequestId(), routeResult));

        return routeResult;
    }

    private TCommandTransferResponse onRoute0(RequestEvent event) {
        TBase<?,?> requestObject = event.getRequestObject();
        if (requestObject == null) {
            return createResponse(TRouteResult.EMPTY_REQUEST);
        }

        TargetClusterPoint clusterPoint = findClusterPoint(event.getDeliveryCommand());
        if (clusterPoint == null) {
            return createResponse(TRouteResult.NOT_FOUND);
        }

        if (!clusterPoint.isSupportCommand(requestObject)) {
            return createResponse(TRouteResult.NOT_SUPPORTED_REQUEST);
        }

        Future<ResponseMessage> future = clusterPoint.request(event.getDeliveryCommand().getPayload());
        boolean isCompleted = future.await();
        if (!isCompleted) {
            return createResponse(TRouteResult.TIMEOUT);
        }

        ResponseMessage responseMessage = future.getResult();
        if (responseMessage == null) {
            return createResponse(TRouteResult.EMPTY_RESPONSE);
        }

        final byte[] responsePayload = responseMessage.getMessage();
        if (ArrayUtils.isEmpty(responsePayload)) {
            return createResponse(TRouteResult.EMPTY_RESPONSE, new byte[0]);
        }

        return createResponse(TRouteResult.OK, responsePayload);
    }

    private TCommandTransferResponse createResponse(TRouteResult result) {
        return createResponse(result, new byte[0]);
    }

    private TCommandTransferResponse createResponse(TRouteResult result, byte[] payload) {
        TCommandTransferResponse response = new TCommandTransferResponse();
        response.setRouteResult(result);
        response.setPayload(payload);
        return response;
    }

}
