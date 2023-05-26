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

import com.navercorp.pinpoint.collector.cluster.ClusterPoint;
import com.navercorp.pinpoint.collector.cluster.ClusterPointLocator;
import com.navercorp.pinpoint.collector.cluster.GrpcAgentConnection;
import com.navercorp.pinpoint.collector.cluster.route.filter.RouteFilter;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.thrift.TBase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author koo.taejin
 * @author HyunGil Jeong
 */
public class DefaultRouteHandler extends AbstractRouteHandler<RequestEvent> {

    private final RouteFilterChain<RequestEvent> requestFilterChain;
    private final RouteFilterChain<ResponseEvent> responseFilterChain;

    public DefaultRouteHandler(ClusterPointLocator<ClusterPoint<?>> targetClusterPointLocator,
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
        TBase<?, ?> requestObject = event.getRequestObject();
        if (requestObject == null) {
            return createResponse(TRouteResult.EMPTY_REQUEST);
        }

        ClusterPoint<?> clusterPoint = findClusterPoint(event.getDeliveryCommand());
        if (clusterPoint == null) {
            return createResponse(TRouteResult.NOT_FOUND);
        }

        if (!clusterPoint.isSupportCommand(requestObject)) {
            return createResponse(TRouteResult.NOT_SUPPORTED_REQUEST);
        }

        CompletableFuture<ResponseMessage> future;
        if (clusterPoint instanceof GrpcAgentConnection) {
            GrpcAgentConnection grpcAgentConnection = (GrpcAgentConnection) clusterPoint;
            future = grpcAgentConnection.request(event.getRequestObject());
        } else {
            return createResponse(TRouteResult.NOT_ACCEPTABLE);
        }

        try {
            ResponseMessage responseMessage = future.get(3000, TimeUnit.MILLISECONDS);
            if (responseMessage == null) {
                return createResponse(TRouteResult.EMPTY_RESPONSE);
            }

            final byte[] responsePayload = responseMessage.getMessage();
            if (ArrayUtils.isEmpty(responsePayload)) {
                return createResponse(TRouteResult.EMPTY_RESPONSE, new byte[0]);
            }

            return createResponse(TRouteResult.OK, responsePayload);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return createResponse(TRouteResult.UNKNOWN, e.getMessage());
        } catch (ExecutionException e) {
            return createResponse(TRouteResult.UNKNOWN, e.getCause().getMessage());
        } catch (TimeoutException e) {
            return createResponse(TRouteResult.TIMEOUT);
        }
    }

}
