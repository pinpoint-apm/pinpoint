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

package com.navercorp.pinpoint.realtime.collector.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.grpc.trace.PCmdEcho;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.realtime.collector.receiver.ClusterPoint;
import com.navercorp.pinpoint.realtime.collector.receiver.ClusterPointLocator;
import com.navercorp.pinpoint.realtime.collector.receiver.grpc.GrpcAgentConnection;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Taejin Koo
 */
@RestController
@RequestMapping(value = { "/cluster/grpc", "/cluster" })
public class ClusterPointController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ClusterPointLocator clusterPointLocator;
    private final SinkRepository<MonoSink<PCmdEchoResponse>> echoSinkRepository;

    public ClusterPointController(
            ClusterPointLocator clusterPointLocator,
            SinkRepository<MonoSink<PCmdEchoResponse>> echoSinkRepository
    ) {
        this.clusterPointLocator = Objects.requireNonNull(clusterPointLocator, "clusterPointLocator");
        this.echoSinkRepository = Objects.requireNonNull(echoSinkRepository, "sinkRepository");
    }

    @GetMapping(value = "/html/getClusterPoint")
    public String getClusterPointToHtml(
            @RequestParam("applicationName") String applicationName,
            @RequestParam(value = "agentId", defaultValue = "") String agentId,
            @RequestParam(value = "startTimestamp", defaultValue = "-1") long startTimestamp) {

        List<GrpcAgentConnectionStats> result = getClusterPoint0(applicationName, agentId, startTimestamp);
        return buildHtml(result);
    }

    @GetMapping(value = "/getClusterPoint")
    public List<GrpcAgentConnectionStats> getClusterPoint(
            @RequestParam("applicationName") String applicationName,
            @RequestParam(value = "agentId", defaultValue = "") String agentId,
            @RequestParam(value = "startTimestamp", defaultValue = "-1") long startTimestamp) {

        return getClusterPoint0(applicationName, agentId, startTimestamp);
    }

    @GetMapping(value = "/checkConnectionStatus")
    public List<GrpcAgentConnectionStats> checkConnectionStatus(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("agentId") String agentId,
            @RequestParam("startTimestamp") long startTimestamp,
            @RequestParam(value = "checkCount", defaultValue = "3") int checkCount) {
        Assert.isTrue(checkCount > 0, "checkCount must be ' > 0'");

        List<GrpcAgentConnection> grpcAgentConnectionList =
                getGrpcAgentConnectionList(applicationName, agentId, startTimestamp);

        List<GrpcAgentConnectionStats> result = new ArrayList<>(grpcAgentConnectionList.size());
        for (GrpcAgentConnection grpcAgentConnection : grpcAgentConnectionList) {
            if (!grpcAgentConnection.getSupportCommandList().contains(TCommandType.ECHO)) {
                result.add(new GrpcAgentConnectionStats(
                        grpcAgentConnection,
                        CheckConnectionStatusResult.STATUS_CHECK_NOT_SUPPORTED
                ));
                continue;
            }

            CheckConnectionStatusResult connectionStatusResult = request(grpcAgentConnection, checkCount);
            result.add(new GrpcAgentConnectionStats(grpcAgentConnection, connectionStatusResult));
        }

        return result;
    }

    private List<GrpcAgentConnectionStats> getClusterPoint0(
            String applicationName,
            String agentId,
            long startTimestamp
    ) {
        List<GrpcAgentConnection> grpcAgentConnectionList =
                getGrpcAgentConnectionList(applicationName, agentId, startTimestamp);

        List<GrpcAgentConnectionStats> result = new ArrayList<>(grpcAgentConnectionList.size());
        for (GrpcAgentConnection grpcAgentConnection : grpcAgentConnectionList) {
            result.add(new GrpcAgentConnectionStats(grpcAgentConnection, CheckConnectionStatusResult.NOT_CHECKED));
        }

        return result;
    }

    private List<GrpcAgentConnection> getGrpcAgentConnectionList(
            String applicationName,
            String agentId,
            long startTimestamp
    ) {
        Objects.requireNonNull(applicationName, "applicationName");

        List<GrpcAgentConnection> result = new ArrayList<>();
        Collection<ClusterPoint> clusterPointList = clusterPointLocator.getClusterPointList();
        for (ClusterPoint clusterPoint : clusterPointList) {
            if (!(clusterPoint instanceof GrpcAgentConnection)) {
                continue;
            }
            ClusterKey destClusterInfo = clusterPoint.getClusterKey();

            if (!destClusterInfo.getApplicationName().equals(applicationName)) {
                continue;
            }

            if (StringUtils.hasText(agentId) && !destClusterInfo.getAgentId().equals(agentId)) {
                continue;
            }

            if (startTimestamp > 0 && destClusterInfo.getStartTimestamp() != startTimestamp) {
                continue;
            }

            result.add((GrpcAgentConnection) clusterPoint);
        }

        return result;
    }

    private CheckConnectionStatusResult request(GrpcAgentConnection grpcAgentConnection, int checkCount) {
        logger.info("Ping  message will be sent. collector => {}.", grpcAgentConnection.getClusterKey());

        CompletableFuture<PCmdEchoResponse> response = null;
        try {
            response = request0(grpcAgentConnection, checkCount);
        } catch (StatusRuntimeException e) {
            logger.warn("Exception occurred while request message. message:{}", e.getMessage(), e);
            if (e.getStatus().getCode() == Status.CANCELLED.getCode()) {
                clearConnection();
                return CheckConnectionStatusResult.FAIL_AND_CLEAR_CONNECTION;
            }
            return CheckConnectionStatusResult.FAIL;
        } catch (PinpointSocketException e) {
            logger.warn("Exception occurred while request message. message:{}", e.getMessage(), e);
            clearConnection();
            return CheckConnectionStatusResult.FAIL_AND_CLEAR_CONNECTION;
        }
        try {
            PCmdEchoResponse result = response.get(3000, TimeUnit.MILLISECONDS);
            if (result.getMessage().equals("PING")) {
                return CheckConnectionStatusResult.SUCCESS;
            }
            logger.warn("Receive unexpected response: result = {}", result);
        } catch (Exception cause) {
            logger.warn("Failed while request message. message:{}", cause.getMessage(), cause);
            return CheckConnectionStatusResult.FAIL;
        }

        return CheckConnectionStatusResult.FAIL;
    }

    private void clearConnection() {
    }

    // If the occur exception in connection, do not retry
    // Multiple attempts only at timeout
    private CompletableFuture<PCmdEchoResponse> request0(GrpcAgentConnection conn, int maxCount) {
        Mono<PCmdEchoResponse> mono = Mono.create(sink -> {
            long sinkId = this.echoSinkRepository.put(sink);
            sink.onDispose(() -> {
                this.echoSinkRepository.invalidate(sinkId);
            });
            conn.request(PCmdRequest.newBuilder()
                    .setRequestId(Long.valueOf(sinkId).intValue())
                    .setCommandEcho(PCmdEcho.newBuilder()
                            .setMessage("PING"))
                    .build());
        });

        for (int i = 0; i < maxCount; i++) {
            CompletableFuture<PCmdEchoResponse> responseFuture = mono.toFuture();
            try {
                responseFuture.get(3000, TimeUnit.MILLISECONDS);
                return responseFuture;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PinpointSocketException(e);
            } catch (ExecutionException e) {
                throw new PinpointSocketException(e.getCause());
            } catch (TimeoutException e) {
                throw new PinpointSocketException(e);
            } catch (Exception ignored) {
            }
        }

        throw new PinpointSocketException("Request limit exceeded. limit:" +  maxCount);
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

        private final ClusterKey clusterKey;

        private final boolean availableCheckConnectionState;

        private final String checkConnectionStatusResult;

        public GrpcAgentConnectionStats(
                GrpcAgentConnection conn,
                CheckConnectionStatusResult checkConnectionStatusResult
        ) {
            this.clusterKey = conn.getClusterKey();
            this.remoteAddress = conn.getRemoteAddress();

            this.availableCheckConnectionState =
                    checkConnectionStatusResult != CheckConnectionStatusResult.STATUS_CHECK_NOT_SUPPORTED;
            this.checkConnectionStatusResult = checkConnectionStatusResult.name();
        }

        @JsonProperty("remoteAddress")
        public InetSocketAddress getRemoteAddress() {
            return remoteAddress;
        }

        @JsonProperty("clusterKey")
        public ClusterKey getClusterKey() {
            return clusterKey;
        }

        @JsonProperty("availableCheckConnectionState")
        public boolean isAvailableCheckConnectionState() {
            return availableCheckConnectionState;
        }

        @JsonProperty("checkConnectionStatusResult")
        public String getCheckConnectionStatusResult() {
            return checkConnectionStatusResult;
        }
    }

    private enum CheckConnectionStatusResult {

        NOT_CHECKED,
        STATUS_CHECK_NOT_SUPPORTED,
        SUCCESS,
        FAIL,
        FAIL_AND_CLEAR_CONNECTION,

    }

}
