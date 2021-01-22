/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.controller;

import com.navercorp.pinpoint.collector.cluster.AgentInfo;
import com.navercorp.pinpoint.collector.cluster.ClusterPoint;
import com.navercorp.pinpoint.collector.cluster.ClusterPointLocator;
import com.navercorp.pinpoint.collector.cluster.GrpcAgentConnection;
import com.navercorp.pinpoint.collector.receiver.grpc.PinpointGrpcServer;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Controller
@RequestMapping("/cluster/grpc")
public class ClusterPointController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final TCommandEcho CONNECTION_CHECK_COMMAND = new TCommandEcho("PING");

    private final HeaderTBaseDeserializer tBaseDeserializer = CommandHeaderTBaseDeserializerFactory.getDefaultInstance().createDeserializer();


    private final ClusterPointLocator clusterPointLocator;

    private final ObjectMapper mapper;


    @Autowired
    public ClusterPointController(ClusterPointLocator clusterPointLocator, ObjectMapper mapper) {
        this.clusterPointLocator = Objects.requireNonNull(clusterPointLocator, "clusterPointLocator");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @RequestMapping(value = "/html/getClusterPoint", method = RequestMethod.GET)
    @ResponseBody
    public String getClusterPointToHtml(
            @RequestParam("applicationName") String applicationName,
            @RequestParam(value = "agentId", defaultValue = "") String agentId,
            @RequestParam(value = "startTimestamp", defaultValue = "-1") long startTimestamp) {

        List<GrpcAgentConnectionStats> result = getClusterPoint0(applicationName, agentId, startTimestamp);
        return buildHtml(result);
    }

    @RequestMapping(value = "/getClusterPoint", method = RequestMethod.GET)
    @ResponseBody
    public String getClusterPoint(
            @RequestParam("applicationName") String applicationName,
            @RequestParam(value = "agentId", defaultValue = "") String agentId,
            @RequestParam(value = "startTimestamp", defaultValue = "-1") long startTimestamp) throws JsonProcessingException {

        List<GrpcAgentConnectionStats> result = getClusterPoint0(applicationName, agentId, startTimestamp);
        return mapper.writeValueAsString(result);
    }

    @RequestMapping(value = "/checkConnectionStatus", method = RequestMethod.GET)
    @ResponseBody
    public String checkConnectionStatus(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("agentId") String agentId,
            @RequestParam("startTimestamp") long startTimestamp) throws JsonProcessingException {
        List<GrpcAgentConnection> grpcAgentConnectionList = getGrpcAgentConnectionList(applicationName, agentId, startTimestamp);

        List<GrpcAgentConnectionStats> result = new ArrayList<>(grpcAgentConnectionList.size());
        for (GrpcAgentConnection grpcAgentConnection : grpcAgentConnectionList) {
            if (!grpcAgentConnection.isSupportCommand(CONNECTION_CHECK_COMMAND)) {
                result.add(new GrpcAgentConnectionStats(grpcAgentConnection, CheckConnectionStatusResult.STATUS_CHECK_NOT_SUPPORTED));
                continue;
            }

            CheckConnectionStatusResult connectionStatusResult = request(grpcAgentConnection);
            result.add(new GrpcAgentConnectionStats(grpcAgentConnection, connectionStatusResult));
        }

        return mapper.writeValueAsString(result);
    }

    private List<GrpcAgentConnectionStats> getClusterPoint0(final String applicationName, final String agentId, final long startTimestamp) {
        List<GrpcAgentConnection> grpcAgentConnectionList = getGrpcAgentConnectionList(applicationName, agentId, startTimestamp);

        List<GrpcAgentConnectionStats> result = new ArrayList<>(grpcAgentConnectionList.size());
        for (GrpcAgentConnection grpcAgentConnection : grpcAgentConnectionList) {
            result.add(new GrpcAgentConnectionStats(grpcAgentConnection, CheckConnectionStatusResult.NOT_CHECKED));
        }

        return result;
    }

    private List<GrpcAgentConnection> getGrpcAgentConnectionList(final String applicationName, final String agentId, final long startTimestamp) {
        Objects.requireNonNull(applicationName, "applicationName");

        List<GrpcAgentConnection> result = new ArrayList<>();
        List<ClusterPoint> clusterPointList = clusterPointLocator.getClusterPointList();
        for (ClusterPoint clusterPoint : clusterPointList) {
            if (!(clusterPoint instanceof GrpcAgentConnection)) {
                continue;
            }
            AgentInfo destAgentInfo = clusterPoint.getDestAgentInfo();

            if (!destAgentInfo.getApplicationName().equals(applicationName)) {
                continue;
            }

            if (StringUtils.hasText(agentId) && !destAgentInfo.getAgentId().equals(agentId)) {
                continue;
            }

            if (startTimestamp > 0 && destAgentInfo.getStartTimestamp() != startTimestamp) {
                continue;
            }

            result.add((GrpcAgentConnection) clusterPoint);
        }

        return result;
    }

    private CheckConnectionStatusResult request(GrpcAgentConnection grpcAgentConnection) {
        logger.info("ping  message will be sent. collector => {}.", grpcAgentConnection.getDestAgentInfo().getAgentKey());

        Future<ResponseMessage> response = null;
        try {
            response = grpcAgentConnection.request(CONNECTION_CHECK_COMMAND);
        } catch (StatusRuntimeException e) {
            logger.warn("Exception occurred while request message. message:{}", e.getMessage(), e);
            if (e.getStatus().getCode() == Status.CANCELLED.getCode()) {
                PinpointGrpcServer pinpointGrpcServer = grpcAgentConnection.getPinpointGrpcServer();
                pinpointGrpcServer.close(SocketStateCode.ERROR_UNKNOWN);
                return CheckConnectionStatusResult.FAIL_AND_CLEAR_CONNECTION;
            }
            return CheckConnectionStatusResult.FAIL;
        }

        try {
            response.await();
            ResponseMessage result = response.getResult();

            Message<TBase<?, ?>> deserialize = tBaseDeserializer.deserialize(result.getMessage());

            TBase<?, ?> data = deserialize.getData();
            if (data instanceof TCommandEcho) {
                if (CONNECTION_CHECK_COMMAND.getMessage().equals(((TCommandEcho) data).getMessage())) {
                    return CheckConnectionStatusResult.SUCCESS;
                }
            }
        } catch (Exception e) {
            logger.warn("Exception occurred while handles response message. message:{}", e.getMessage(), e);
        }
        return CheckConnectionStatusResult.FAIL;
    }

    private <T> String buildHtml(List<T> stats) {
        StringBuilder buffer = new StringBuilder();
        for (T stat : stats) {
            String html = new HTMLBuilder().build(stat);
            buffer.append(html);
            buffer.append("<br>");
        }
        return buffer.toString();
    }

    private static class GrpcAgentConnectionStats {

        private final InetSocketAddress remoteAddress;

        private final String agentKey;

        private final String socketStateCode;

        private final boolean availableCheckConnectionState;

        private final String checkConnectionStatusResult;

        public GrpcAgentConnectionStats(GrpcAgentConnection grpcAgentConnection, CheckConnectionStatusResult checkConnectionStatusResult) {
            PinpointGrpcServer pinpointGrpcServer = grpcAgentConnection.getPinpointGrpcServer();
            this.socketStateCode = pinpointGrpcServer.getState().name();
            this.agentKey = pinpointGrpcServer.getAgentInfo().getAgentKey();
            this.remoteAddress = pinpointGrpcServer.getRemoteAddress();

            this.availableCheckConnectionState = grpcAgentConnection.isSupportCommand(CONNECTION_CHECK_COMMAND);
            this.checkConnectionStatusResult = checkConnectionStatusResult.name();
        }

        public InetSocketAddress getRemoteAddress() {
            return remoteAddress;
        }

        public String getAgentKey() {
            return agentKey;
        }

        public String getSocketStateCode() {
            return socketStateCode;
        }

        public boolean isAvailableCheckConnectionState() {
            return availableCheckConnectionState;
        }

        public String getCheckConnectionStatusResult() {
            return checkConnectionStatusResult;
        }
    }

    private static enum CheckConnectionStatusResult {

        NOT_CHECKED,
        STATUS_CHECK_NOT_SUPPORTED,
        SUCCESS,
        FAIL,
        FAIL_AND_CLEAR_CONNECTION;

    }

}
