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
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author koo.taejin
 */
public abstract class AbstractRouteHandler<T extends RouteEvent> implements RouteHandler<T> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ClusterPointLocator<ClusterPoint<?>> targetClusterPointLocator;

    public AbstractRouteHandler(ClusterPointLocator<ClusterPoint<?>> targetClusterPointLocator) {
        this.targetClusterPointLocator = targetClusterPointLocator;
    }

    protected ClusterPoint<?> findClusterPoint(TCommandTransfer deliveryCommand) {
        String applicationName = deliveryCommand.getApplicationName();
        String agentId = deliveryCommand.getAgentId();
        long startTimeStamp = deliveryCommand.getStartTime();
        final ClusterKey sourceKey = new ClusterKey(applicationName, agentId, startTimeStamp);
        return findClusterPoint(sourceKey);
    }

    public ClusterPoint<?> findClusterPoint(ClusterKey sourceKey) {
        List<ClusterPoint<?>> result = new ArrayList<>();

        for (ClusterPoint<?> targetClusterPoint : targetClusterPointLocator.getClusterPointList()) {
            ClusterKey destAgentInfo = targetClusterPoint.getDestClusterKey();
            if (destAgentInfo.equals(sourceKey)) {
                result.add(targetClusterPoint);
            }
        }

        if (result.size() == 1) {
            return result.get(0);
        }

        if (result.size() > 1) {
            logger.warn("Ambiguous ClusterPoint {} (Valid Agent list={}).", sourceKey, result);
            return null;
        }

        return null;
    }

    protected TCommandTransferResponse createResponse(TRouteResult result) {
        return createResponse(result, new byte[0]);
    }

    protected TCommandTransferResponse createResponse(TRouteResult result, byte[] payload) {
        TCommandTransferResponse response = new TCommandTransferResponse();
        response.setRouteResult(result);
        response.setPayload(payload);
        return response;
    }

    protected TCommandTransferResponse createResponse(TRouteResult result, String message) {
        TCommandTransferResponse response = new TCommandTransferResponse();
        response.setRouteResult(result);
        response.setMessage(message);
        return response;
    }

}
