/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamException;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCount;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import com.navercorp.pinpoint.web.cluster.ClusterKeyAndStatus;
import com.navercorp.pinpoint.web.cluster.ClusterKeyUtils;
import com.navercorp.pinpoint.web.cluster.ClusterManager;
import com.navercorp.pinpoint.web.cluster.DefaultPinpointRouteResponse;
import com.navercorp.pinpoint.web.cluster.FailedPinpointRouteResponse;
import com.navercorp.pinpoint.web.cluster.PinpointRouteResponse;
import com.navercorp.pinpoint.web.vo.activethread.AgentActiveThreadCount;
import com.navercorp.pinpoint.web.vo.activethread.AgentActiveThreadCountFactory;
import com.navercorp.pinpoint.web.vo.activethread.AgentActiveThreadCountList;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author HyunGil Jeong
 * @author Taejin Koo
 */
@Service
public class AgentServiceImpl implements AgentService {

    private static final long DEFAULT_FUTURE_TIMEOUT = 3000;

    private long timeDiffMs;

    private final AgentInfoService agentInfoService;

    private final ClusterManager clusterManager;

    private final SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    private final DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    public AgentServiceImpl(AgentInfoService agentInfoService, ClusterManager clusterManager,
                            @Qualifier("commandHeaderTBaseSerializerFactory") SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory,
                            @Qualifier("commandHeaderTBaseDeserializerFactory") DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.clusterManager = Objects.requireNonNull(clusterManager, "clusterManager");
        this.commandSerializerFactory = Objects.requireNonNull(commandSerializerFactory, "commandSerializerFactory");
        this.commandDeserializerFactory = Objects.requireNonNull(commandDeserializerFactory, "commandDeserializerFactory");
    }

    @Value("${web.activethread.activeAgent.duration.days:7}")
    private void setTimeDiffMs(int durationDays) {
        this.timeDiffMs = TimeUnit.MILLISECONDS.convert(durationDays, TimeUnit.DAYS);
    }

    @Override
    public ClusterKey getClusterKey(String applicationName, String agentId) {
        long currentTime = System.currentTimeMillis();

        Set<AgentInfo> agentInfos = agentInfoService.getAgentsByApplicationNameWithoutStatus(applicationName, currentTime);
        for (AgentInfo agentInfo : agentInfos) {
            if (agentInfo == null) {
                continue;
            }
            if (!agentInfo.getApplicationName().equals(applicationName)) {
                continue;
            }
            if (!agentInfo.getAgentId().equals(agentId)) {
                continue;
            }

            return ClusterKeyUtils.from(agentInfo);
        }

        return null;
    }

    @Override
    public ClusterKey getClusterKey(String applicationName, String agentId, long startTimeStamp) {
        return getClusterKey(applicationName, agentId, startTimeStamp, false);
    }

    @Override
    public ClusterKey getClusterKey(String applicationName, String agentId, long startTimeStamp, boolean checkDB) {
        if (checkDB) {
            long currentTime = System.currentTimeMillis();

            Set<AgentInfo> agentInfos = agentInfoService.getAgentsByApplicationNameWithoutStatus(applicationName, currentTime);
            for (AgentInfo agentInfo : agentInfos) {
                if (agentInfo == null) {
                    continue;
                }
                if (!agentInfo.getApplicationName().equals(applicationName)) {
                    continue;
                }
                if (!agentInfo.getAgentId().equals(agentId)) {
                    continue;
                }
                if (agentInfo.getStartTimestamp() != startTimeStamp) {
                    continue;
                }

                return ClusterKeyUtils.from(agentInfo);
            }
            return null;
        } else {
            return new ClusterKey(applicationName, agentId, startTimeStamp);
        }
    }

    @Override
    public List<ClusterKeyAndStatus> getRecentAgentInfoList(String applicationName) {
        return this.getRecentAgentInfoList(applicationName, this.timeDiffMs);
    }

    @Override
    public List<ClusterKeyAndStatus> getRecentAgentInfoList(String applicationName, long timeDiff) {

        long currentTime = System.currentTimeMillis();

        Set<AgentAndStatus> agentInfoAndStatusSet = agentInfoService.getRecentAgentsByApplicationName(applicationName, currentTime, timeDiff);
        return agentInfoAndStatusSet.stream()
                .filter(Objects::nonNull)
                .map(ClusterKeyUtils::withStatusFrom)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isConnected(ClusterKey clusterKey) {
        return clusterManager.isConnected(clusterKey);
    }

    @Override
    public PinpointRouteResponse invoke(ClusterKey clusterKey, TBase<?, ?> tBase) throws TException {
        byte[] payload = serializeRequest(tBase);
        return invoke(clusterKey, payload);
    }

    @Override
    public PinpointRouteResponse invoke(ClusterKey clusterKey, TBase<?, ?> tBase, long timeout) throws TException {
        byte[] payload = serializeRequest(tBase);
        return invoke(clusterKey, payload, timeout);
    }

    @Override
    public PinpointRouteResponse invoke(ClusterKey clusterKey, byte[] payload) throws TException {
        return invoke(clusterKey, payload, DEFAULT_FUTURE_TIMEOUT);
    }

    @Override
    public PinpointRouteResponse invoke(ClusterKey clusterKey, byte[] payload, long timeout) throws TException {
        TCommandTransfer transferObject = createCommandTransferObject(clusterKey, payload);
        List<PinpointSocket> socketList = clusterManager.getSocket(clusterKey);

        Future<ResponseMessage> future = null;
        if (CollectionUtils.nullSafeSize(socketList) == 1) {
            PinpointSocket socket = socketList.get(0);
            future = socket.request(serializeRequest(transferObject));
        }

        return getResponse(future, timeout);
    }

    @Override
    public Map<ClusterKey, PinpointRouteResponse> invoke(List<ClusterKey> agentInfoList, TBase<?, ?> tBase)
            throws TException {
        byte[] payload = serializeRequest(tBase);
        return invoke(agentInfoList, payload);
    }

    @Override
    public Map<ClusterKey, PinpointRouteResponse> invoke(List<ClusterKey> agentInfoList, TBase<?, ?> tBase, long timeout)
            throws TException {
        byte[] payload = serializeRequest(tBase);
        return invoke(agentInfoList, payload, timeout);
    }

    @Override
    public Map<ClusterKey, PinpointRouteResponse> invoke(List<ClusterKey> agentInfoList, byte[] payload)
            throws TException {
        return invoke(agentInfoList, payload, DEFAULT_FUTURE_TIMEOUT);
    }

    @Override
    public Map<ClusterKey, PinpointRouteResponse> invoke(List<ClusterKey> agentInfoList, byte[] payload, long timeout)
            throws TException {
        Map<ClusterKey, Future<ResponseMessage>> futureMap = new HashMap<>();
        for (ClusterKey clusterKey : agentInfoList) {
            TCommandTransfer transferObject = createCommandTransferObject(clusterKey, payload);
            List<PinpointSocket> socketList = clusterManager.getSocket(clusterKey);

            if (CollectionUtils.nullSafeSize(socketList) == 1) {
                PinpointSocket socket = socketList.get(0);
                Future<ResponseMessage> future = socket.request(serializeRequest(transferObject));
                futureMap.put(clusterKey, future);
            } else {
                futureMap.put(clusterKey, null);
            }
        }

        long startTime = System.currentTimeMillis();

        Map<ClusterKey, PinpointRouteResponse> result = new HashMap<>();
        for (Map.Entry<ClusterKey, Future<ResponseMessage>> futureEntry : futureMap.entrySet()) {
            ClusterKey clusterKey = futureEntry.getKey();
            Future<ResponseMessage> future = futureEntry.getValue();
            PinpointRouteResponse response = getResponse(future, getTimeoutMillis(startTime, timeout));
            result.put(clusterKey, response);
        }

        return result;
    }

    @Override
    public ClientStreamChannel openStream(ClusterKey clusterKey, TBase<?, ?> tBase, ClientStreamChannelEventHandler streamChannelEventHandler) throws TException, StreamException {
        byte[] payload = serializeRequest(tBase);
        return openStream(clusterKey, payload, streamChannelEventHandler);
    }

    @Override
    public ClientStreamChannel openStream(ClusterKey clusterKey, byte[] payload, ClientStreamChannelEventHandler streamChannelEventHandler) throws TException, StreamException {
        assertClusterEnabled();

        TCommandTransfer transferObject = createCommandTransferObject(clusterKey, payload);
        List<PinpointSocket> socketList = clusterManager.getSocket(clusterKey);
        if (CollectionUtils.nullSafeSize(socketList) == 1) {
            PinpointSocket socket = socketList.get(0);
            return socket.openStream(serializeRequest(transferObject), streamChannelEventHandler);
        } else if (CollectionUtils.isEmpty(socketList)) {
            throw new StreamException(StreamCode.CONNECTION_NOT_FOUND);
        } else {
            throw new StreamException(StreamCode.CONNECTION_DUPLICATED);
        }
    }

    @Override
    public ClientStreamChannel openStreamAndAwait(ClusterKey clusterKey, TBase<?, ?> tBase, ClientStreamChannelEventHandler streamChannelEventHandler, long timeout) throws TException, StreamException {
        byte[] payload = serializeRequest(tBase);
        return openStreamAndAwait(clusterKey, payload, streamChannelEventHandler, timeout);
    }

    @Override
    public ClientStreamChannel openStreamAndAwait(ClusterKey clusterKey, byte[] payload, ClientStreamChannelEventHandler streamChannelEventHandler, long timeout) throws TException, StreamException {
        assertClusterEnabled();

        TCommandTransfer transferObject = createCommandTransferObject(clusterKey, payload);
        List<PinpointSocket> socketList = clusterManager.getSocket(clusterKey);
        if (CollectionUtils.nullSafeSize(socketList) == 1) {
            PinpointSocket socket = socketList.get(0);
            return socket.openStreamAndAwait(serializeRequest(transferObject), streamChannelEventHandler, timeout);
        } else if (CollectionUtils.isEmpty(socketList)) {
            throw new StreamException(StreamCode.CONNECTION_NOT_FOUND);
        } else {
            throw new StreamException(StreamCode.CONNECTION_DUPLICATED);
        }
    }

    private void assertClusterEnabled() throws StreamException {
        if (!clusterManager.isEnabled()) {
            throw new StreamException(StreamCode.CONNECTION_UNSUPPORT);
        }
    }

    @Override
    public AgentActiveThreadCountList getActiveThreadCount(List<ClusterKey> agentInfoList) throws TException {
        byte[] activeThread = serializeRequest(new TCmdActiveThreadCount());
        return getActiveThreadCount(agentInfoList, activeThread);
    }

    @Override
    public AgentActiveThreadCountList getActiveThreadCount(List<ClusterKey> agentInfoList, byte[] payload) throws TException {
        AgentActiveThreadCountList activeThreadCountList = new AgentActiveThreadCountList(agentInfoList.size());

        Map<ClusterKey, PinpointRouteResponse> responseList = invoke(agentInfoList, payload);
        for (Map.Entry<ClusterKey, PinpointRouteResponse> entry : responseList.entrySet()) {
            ClusterKey clusterKey = entry.getKey();
            PinpointRouteResponse response = entry.getValue();

            AgentActiveThreadCount activeThreadCount = createActiveThreadCount(clusterKey.getAgentId(), response);
            activeThreadCountList.add(activeThreadCount);
        }

        return activeThreadCountList;
    }

    private AgentActiveThreadCount createActiveThreadCount(String agentId, PinpointRouteResponse response) {
        TRouteResult routeResult = response.getRouteResult();
        if (routeResult == TRouteResult.OK) {
            AgentActiveThreadCountFactory factory = new AgentActiveThreadCountFactory();
            factory.setAgentId(agentId);
            return factory.create(response.getResponse(TCmdActiveThreadCountRes.class, null));
        } else {
            AgentActiveThreadCountFactory factory = new AgentActiveThreadCountFactory();
            factory.setAgentId(agentId);
            return factory.createFail(routeResult.name());
        }
    }

    private TCommandTransfer createCommandTransferObject(ClusterKey clusterKey, byte[] payload) {
        Objects.requireNonNull(clusterKey, "agentInfoKey");

        TCommandTransfer transferObject = new TCommandTransfer();
        transferObject.setApplicationName(clusterKey.getApplicationName());
        transferObject.setAgentId(clusterKey.getAgentId());
        transferObject.setStartTime(clusterKey.getStartTimestamp());
        transferObject.setPayload(payload);

        return transferObject;
    }

    private PinpointRouteResponse getResponse(Future<ResponseMessage> future, long timeout) {
        if (future == null) {
            return new FailedPinpointRouteResponse(TRouteResult.NOT_FOUND, null);
        }

        boolean completed = future.await(timeout);
        if (completed) {
            DefaultPinpointRouteResponse response = new DefaultPinpointRouteResponse(future.getResult().getMessage());
            response.parse(commandDeserializerFactory);
            return response;
        } else {
            return new FailedPinpointRouteResponse(TRouteResult.TIMEOUT, null);
        }
    }

    private long getTimeoutMillis(long startTime, long timeout) {
        return Math.max(startTime + timeout - System.currentTimeMillis(), 100L);
    }


    @Override
    public byte[] serializeRequest(TBase<?, ?> tBase) throws TException {
        return SerializationUtils.serialize(tBase, commandSerializerFactory);
    }

    @Override
    public byte[] serializeRequest(TBase<?, ?> tBase, byte[] defaultValue) {
        return SerializationUtils.serialize(tBase, commandSerializerFactory, defaultValue);
    }

    @Override
    public TBase<?, ?> deserializeResponse(byte[] objectData) throws TException {
        Message<TBase<?, ?>> message = SerializationUtils.deserialize(objectData, commandDeserializerFactory);
        if (message == null) {
            return null;
        }
        return message.getData();
    }

    @Override
    public TBase<?, ?> deserializeResponse(byte[] objectData, Message<TBase<?, ?>> defaultValue) {
        Message<TBase<?, ?>> message = SerializationUtils.deserialize(objectData, commandDeserializerFactory, defaultValue);
        if (message == null) {
            return null;
        }
        return message.getData();
    }

}
