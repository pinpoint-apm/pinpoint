/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCount;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import com.navercorp.pinpoint.web.cluster.DefaultPinpointRouteResponse;
import com.navercorp.pinpoint.web.cluster.FailedPinpointRouteResponse;
import com.navercorp.pinpoint.web.cluster.PinpointRouteResponse;
import com.navercorp.pinpoint.web.server.PinpointSocketManager;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadStatus;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadStatusList;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author Taejin Koo
 * @author HyunGil Jeong
 */
@Service
public class AgentServiceImpl implements AgentService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final long DEFUALT_FUTURE_TIMEOUT = 3000;

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private PinpointSocketManager pinpointSocketManager;

    @Autowired
    private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    @Autowired
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;


    @Override
    public AgentInfo getAgentInfo(String applicationName, String agentId, long startTimeStamp) {
        return getAgentInfo(applicationName, agentId, startTimeStamp, false);
    }

    @Override
    public AgentInfo getAgentInfo(String applicationName, String agentId, long startTimeStamp, boolean checkDB) {
        if (checkDB) {
            long currentTime = System.currentTimeMillis();

            Set<AgentInfoBo> agentInfoBos = agentInfoService.getAgentsByApplicationName(applicationName, currentTime);
            for (AgentInfoBo agentInfo : agentInfoBos) {
                if (agentInfo == null) {
                    continue;
                }
                if (!agentInfo.getApplicationName().equals(applicationName)) {
                    continue;
                }
                if (!agentInfo.getAgentId().equals(agentId)) {
                    continue;
                }
                if (agentInfo.getStartTime() != startTimeStamp) {
                    continue;
                }

                return new AgentInfo(agentInfo);
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
    public List<AgentInfo> getAgentInfoList(String applicationName) {
        List<AgentInfo> agentInfoList = new ArrayList<AgentInfo>();

        long currentTime = System.currentTimeMillis();

        Set<AgentInfoBo> agentInfoBos = agentInfoService.getAgentsByApplicationName(applicationName, currentTime);
        for (AgentInfoBo agentInfoBo : agentInfoBos) {
            ListUtils.addIfValueNotNull(agentInfoList, new AgentInfo(agentInfoBo));
        }
        return agentInfoList;
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfo agentInfo, TBase<?, ?> tBase) throws TException {
        byte[] payload = serialize(tBase);
        return invoke(agentInfo, payload);
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfo agentInfo, TBase<?, ?> tBase, long timeout) throws TException {
        byte[] payload = serialize(tBase);
        return invoke(agentInfo, payload, timeout);
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfo agentInfo, byte[] payload) throws TException {
        return invoke(agentInfo, payload, DEFUALT_FUTURE_TIMEOUT);
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfo agentInfo, byte[] payload, long timeout) throws TException {
        TCommandTransfer transferObject = createCommandTransferObject(agentInfo, payload);
        PinpointServer collector = pinpointSocketManager.getCollector(agentInfo);

        Future<ResponseMessage> future = collector.request(serialize(transferObject));
        PinpointRouteResponse response = getResponse(future, timeout);
        return response;
    }

    @Override
    public Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, TBase<?, ?> tBase) throws TException {
        byte[] payload = serialize(tBase);
        return invoke(agentInfoList, payload);
    }

    @Override
    public Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, TBase<?, ?> tBase, long timeout) throws TException {
        byte[] payload = serialize(tBase);
        return invoke(agentInfoList, payload, timeout);
    }

    @Override
    public Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, byte[] payload) throws TException {
        return invoke(agentInfoList, payload, DEFUALT_FUTURE_TIMEOUT);
    }

    @Override
    public Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, byte[] payload, long timeout) throws TException {
        Map<AgentInfo, Future<ResponseMessage>> futureMap = new HashMap<AgentInfo, Future<ResponseMessage>>();
        for (AgentInfo agentInfo : agentInfoList) {
            TCommandTransfer transferObject = createCommandTransferObject(agentInfo, payload);
            PinpointServer collector = pinpointSocketManager.getCollector(agentInfo);
            Future<ResponseMessage> future = collector.request(serialize(transferObject));
            futureMap.put(agentInfo, future);
        }

        long startTime = System.currentTimeMillis();

        Map<AgentInfo, PinpointRouteResponse> result = new HashMap<AgentInfo, PinpointRouteResponse>();
        for (Map.Entry<AgentInfo, Future<ResponseMessage>> futureEntry : futureMap.entrySet()) {
            AgentInfo agentInfo = futureEntry.getKey();
            Future<ResponseMessage> future = futureEntry.getValue();
            PinpointRouteResponse response = getResponse(future, getTimeoutMillis(startTime, timeout));
            result.put(agentInfo, response);
        }

        return result;
    }

    @Override
    public AgentActiveThreadStatusList getActiveThreadStatus(List<AgentInfo> agentInfoList) throws TException {
        byte[] activeThread = serialize(new TCmdActiveThreadCount());
        return getActiveThreadStatus(agentInfoList, activeThread);
    }

    @Override
    public AgentActiveThreadStatusList getActiveThreadStatus(List<AgentInfo> agentInfoList, byte[] payload) throws TException {
        AgentActiveThreadStatusList agentActiveThreadStatusList = new AgentActiveThreadStatusList(agentInfoList.size());

        Map<AgentInfo, PinpointRouteResponse> responseList = invoke(agentInfoList, payload);
        for (Map.Entry<AgentInfo, PinpointRouteResponse> entry : responseList.entrySet()) {
            AgentInfo agentInfo = entry.getKey();
            PinpointRouteResponse response = entry.getValue();

            AgentActiveThreadStatus agentActiveThreadStatus = new AgentActiveThreadStatus(agentInfo.getHostName(), response.getRouteResult(), response.getResponse(TCmdActiveThreadCountRes.class, null));
            agentActiveThreadStatusList.add(agentActiveThreadStatus);
        }
        return agentActiveThreadStatusList;
    }

    private byte[] serialize(TBase<?, ?> tBase) throws TException {
        return SerializationUtils.serialize(tBase, commandSerializerFactory);
    }

    private TBase<?, ?> deserialize(byte[] objectData) throws TException {
        return SerializationUtils.deserialize(objectData, commandDeserializerFactory);
    }

    private TBase<?, ?> deserialize(byte[] objectData, TBase<?, ?> defaultValue) throws TException {
        return SerializationUtils.deserialize(objectData, commandDeserializerFactory, defaultValue);
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
        boolean completed = future.await(DEFUALT_FUTURE_TIMEOUT);
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

}
