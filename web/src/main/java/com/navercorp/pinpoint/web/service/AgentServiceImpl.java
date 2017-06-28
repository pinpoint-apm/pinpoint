/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCount;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import com.navercorp.pinpoint.web.cluster.ClusterManager;
import com.navercorp.pinpoint.web.cluster.DefaultPinpointRouteResponse;
import com.navercorp.pinpoint.web.cluster.FailedPinpointRouteResponse;
import com.navercorp.pinpoint.web.cluster.PinpointRouteResponse;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCount;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCountFactory;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCountList;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 * @author Taejin Koo
 */
@Service
public class AgentServiceImpl implements AgentService {

    private static final long DEFAULT_FUTURE_TIMEOUT = 3000;

    private long timeDiffMs;

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private ClusterManager clusterManager;

    @Autowired
    private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    @Autowired
    @Qualifier("commandHeaderTBaseDeserializerFactory")
    private DeserializerFactory commandDeserializerFactory;

    @Value("#{pinpointWebProps['web.activethread.activeAgent.duration.days'] ?: 7}")
    private void setTimeDiffMs(int durationDays) {
        this.timeDiffMs = TimeUnit.MILLISECONDS.convert(durationDays, TimeUnit.DAYS);
    }

    @Override
    public AgentInfo getAgentInfo(String applicationName, String agentId) {
        long currentTime = System.currentTimeMillis();

        Set<AgentInfo> agentInfos = agentInfoService.getAgentsByApplicationName(applicationName, currentTime);
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

            return agentInfo;
        }

        return null;
    }

    @Override
    public AgentInfo getAgentInfo(String applicationName, String agentId, long startTimeStamp) {
        return getAgentInfo(applicationName, agentId, startTimeStamp, false);
    }

    @Override
    public AgentInfo getAgentInfo(String applicationName, String agentId, long startTimeStamp, boolean checkDB) {
        if (checkDB) {
            long currentTime = System.currentTimeMillis();

            Set<AgentInfo> agentInfos = agentInfoService.getAgentsByApplicationName(applicationName, currentTime);
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

                return agentInfo;
            }
            return null;
        } else {
            AgentInfo agentInfo = new AgentInfo();
            agentInfo.setApplicationName(applicationName);
            agentInfo.setAgentId(agentId);
            agentInfo.setStartTimestamp(startTimeStamp);
            return agentInfo;
        }
    }

    @Override
    public List<AgentInfo> getRecentAgentInfoList(String applicationName) {
        return this.getRecentAgentInfoList(applicationName, this.timeDiffMs);
    }

    @Override
    public List<AgentInfo> getRecentAgentInfoList(String applicationName, long timeDiff) {
        List<AgentInfo> agentInfoList = new ArrayList<>();

        long currentTime = System.currentTimeMillis();

        Set<AgentInfo> agentInfos = agentInfoService.getRecentAgentsByApplicationName(applicationName, currentTime, timeDiff);
        for (AgentInfo agentInfo : agentInfos) {
            ListUtils.addIfValueNotNull(agentInfoList, agentInfo);
        }
        return agentInfoList;
    }

    @Override
    public boolean isConnected(AgentInfo agentInfo) {
        return clusterManager.isConnected(agentInfo);
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfo agentInfo, TBase<?, ?> tBase) throws TException {
        byte[] payload = serializeRequest(tBase);
        return invoke(agentInfo, payload);
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfo agentInfo, TBase<?, ?> tBase, long timeout) throws TException {
        byte[] payload = serializeRequest(tBase);
        return invoke(agentInfo, payload, timeout);
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfo agentInfo, byte[] payload) throws TException {
        return invoke(agentInfo, payload, DEFAULT_FUTURE_TIMEOUT);
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfo agentInfo, byte[] payload, long timeout) throws TException {
        TCommandTransfer transferObject = createCommandTransferObject(agentInfo, payload);
        PinpointSocket socket = clusterManager.getSocket(agentInfo);

        Future<ResponseMessage> future = null;
        if (socket != null) {
            future = socket.request(serializeRequest(transferObject));
        }

        PinpointRouteResponse response = getResponse(future, timeout);
        return response;
    }

    @Override
    public Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, TBase<?, ?> tBase)
            throws TException {
        byte[] payload = serializeRequest(tBase);
        return invoke(agentInfoList, payload);
    }

    @Override
    public Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, TBase<?, ?> tBase, long timeout)
            throws TException {
        byte[] payload = serializeRequest(tBase);
        return invoke(agentInfoList, payload, timeout);
    }

    @Override
    public Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, byte[] payload)
            throws TException {
        return invoke(agentInfoList, payload, DEFAULT_FUTURE_TIMEOUT);
    }

    @Override
    public Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, byte[] payload, long timeout)
            throws TException {
        Map<AgentInfo, Future<ResponseMessage>> futureMap = new HashMap<>();
        for (AgentInfo agentInfo : agentInfoList) {
            TCommandTransfer transferObject = createCommandTransferObject(agentInfo, payload);
            PinpointSocket socket = clusterManager.getSocket(agentInfo);
            if (socket != null) {
                Future<ResponseMessage> future = socket.request(serializeRequest(transferObject));
                futureMap.put(agentInfo, future);
            } else {
                futureMap.put(agentInfo, null);
            }
        }

        long startTime = System.currentTimeMillis();

        Map<AgentInfo, PinpointRouteResponse> result = new HashMap<>();
        for (Map.Entry<AgentInfo, Future<ResponseMessage>> futureEntry : futureMap.entrySet()) {
            AgentInfo agentInfo = futureEntry.getKey();
            Future<ResponseMessage> future = futureEntry.getValue();
            PinpointRouteResponse response = getResponse(future, getTimeoutMillis(startTime, timeout));
            result.put(agentInfo, response);
        }

        return result;
    }

    @Override
    public ClientStreamChannelContext openStream(AgentInfo agentInfo, TBase<?, ?> tBase, ClientStreamChannelMessageListener messageListener) throws TException {
        byte[] payload = serializeRequest(tBase);
        return openStream(agentInfo, payload, messageListener, null);
    }

    @Override
    public ClientStreamChannelContext openStream(AgentInfo agentInfo, byte[] payload, ClientStreamChannelMessageListener messageListener) throws TException {
        return openStream(agentInfo, payload, messageListener, null);
    }

    @Override
    public ClientStreamChannelContext openStream(AgentInfo agentInfo, TBase<?, ?> tBase, ClientStreamChannelMessageListener messageListener, StreamChannelStateChangeEventHandler<ClientStreamChannel> stateChangeListener) throws TException {
        byte[] payload = serializeRequest(tBase);
        return openStream(agentInfo, payload, messageListener, stateChangeListener);
    }

    @Override
    public ClientStreamChannelContext openStream(AgentInfo agentInfo, byte[] payload, ClientStreamChannelMessageListener messageListener, StreamChannelStateChangeEventHandler<ClientStreamChannel> stateChangeListener) throws TException {
        TCommandTransfer transferObject = createCommandTransferObject(agentInfo, payload);
        PinpointSocket socket = clusterManager.getSocket(agentInfo);

        if (socket != null) {
            return socket.openStream(serializeRequest(transferObject), messageListener, stateChangeListener);
        }

        return null;
    }

    @Override
    public AgentActiveThreadCountList getActiveThreadCount(List<AgentInfo> agentInfoList) throws TException {
        byte[] activeThread = serializeRequest(new TCmdActiveThreadCount());
        return getActiveThreadCount(agentInfoList, activeThread);
    }

    @Override
    public AgentActiveThreadCountList getActiveThreadCount(List<AgentInfo> agentInfoList, byte[] payload)
            throws TException {
        AgentActiveThreadCountList activeThreadCountList = new AgentActiveThreadCountList(agentInfoList.size());

        Map<AgentInfo, PinpointRouteResponse> responseList = invoke(agentInfoList, payload);
        for (Map.Entry<AgentInfo, PinpointRouteResponse> entry : responseList.entrySet()) {
            AgentInfo agentInfo = entry.getKey();
            PinpointRouteResponse response = entry.getValue();

            AgentActiveThreadCount activeThreadCount = createActiveThreadCount(agentInfo.getAgentId(), response);
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

    private TCommandTransfer createCommandTransferObject(AgentInfo agentInfo, byte[] payload) {
        TCommandTransfer transferObject = new TCommandTransfer();
        transferObject.setApplicationName(agentInfo.getApplicationName());
        transferObject.setAgentId(agentInfo.getAgentId());
        transferObject.setStartTime(agentInfo.getStartTimestamp());
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
        return SerializationUtils.deserialize(objectData, commandDeserializerFactory);
    }

    @Override
    public TBase<?, ?> deserializeResponse(byte[] objectData, TBase<?, ?> defaultValue) {
        return SerializationUtils.deserialize(objectData, commandDeserializerFactory, defaultValue);
    }

}
