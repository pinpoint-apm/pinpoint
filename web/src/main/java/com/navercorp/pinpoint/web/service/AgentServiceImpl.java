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
import com.navercorp.pinpoint.thrift.dto.command.TActiveThread;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadResponse;
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
import com.navercorp.pinpoint.web.vo.Range;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author Taejin Koo
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
    public AgentInfoBo getAgentInfo(String applicationName, String agentId, long startTimeStamp) {
        return getAgentInfo(applicationName, agentId, startTimeStamp, false);
    }

    @Override
    public AgentInfoBo getAgentInfo(String applicationName, String agentId, long startTimeStamp, boolean checkDB) {
        if (checkDB) {
            long currentTime = System.currentTimeMillis();
            Range range = new Range(currentTime, currentTime);

            Set<AgentInfoBo> agentInfoBos = agentInfoService.selectAgent(applicationName, range);
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

                return agentInfo;
            }
            return null;
        } else {
            AgentInfoBo.Builder builder = new AgentInfoBo.Builder();
            builder.setApplicationName(applicationName);
            builder.setAgentId(agentId);
            builder.setStartTime(startTimeStamp);
            return builder.build();
        }
    }

    @Override
    public List<AgentInfoBo> getAgentInfoList(String applicationName) {
        List<AgentInfoBo> agentInfoList = new ArrayList<AgentInfoBo>();

        long currentTime = System.currentTimeMillis();
        Range range = new Range(currentTime, currentTime);

        Set<AgentInfoBo> agentInfoBos = agentInfoService.selectAgent(applicationName, range);
        for (AgentInfoBo agentInfo : agentInfoBos) {
            ListUtils.addIfValueNotNull(agentInfoList, agentInfo);
        }
        return agentInfoList;
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfoBo agentInfo, TBase tBase) throws TException {
        byte[] payload = serialize(tBase);
        return invoke(agentInfo, payload);
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfoBo agentInfo, TBase tBase, long timeout) throws TException {
        byte[] payload = serialize(tBase);
        return invoke(agentInfo, payload, timeout);
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfoBo agentInfo, byte[] payload) throws TException {
        return invoke(agentInfo, payload, DEFUALT_FUTURE_TIMEOUT);
    }

    @Override
    public PinpointRouteResponse invoke(AgentInfoBo agentInfo, byte[] payload, long timeout) throws TException {
        TCommandTransfer transferObject = createCommandTransferObject(agentInfo, payload);
        PinpointServer collector = pinpointSocketManager.getCollector(agentInfo);

        Future<ResponseMessage> future = collector.request(serialize(transferObject));
        PinpointRouteResponse response = getResponse(future, timeout);
        return response;
    }

    @Override
    public Map<AgentInfoBo, PinpointRouteResponse> invoke(List<AgentInfoBo> agentInfoList, TBase tBase) throws TException {
        byte[] payload = serialize(tBase);
        return invoke(agentInfoList, payload);
    }

    @Override
    public Map<AgentInfoBo, PinpointRouteResponse> invoke(List<AgentInfoBo> agentInfoList, TBase tBase, long timeout) throws TException {
        byte[] payload = serialize(tBase);
        return invoke(agentInfoList, payload, timeout);
    }

    @Override
    public Map<AgentInfoBo, PinpointRouteResponse> invoke(List<AgentInfoBo> agentInfoList, byte[] payload) throws TException {
        return invoke(agentInfoList, payload, DEFUALT_FUTURE_TIMEOUT);
    }

    @Override
    public Map<AgentInfoBo, PinpointRouteResponse> invoke(List<AgentInfoBo> agentInfoList, byte[] payload, long timeout) throws TException {
        Map<AgentInfoBo, Future<ResponseMessage>> futureMap = new HashMap<AgentInfoBo, Future<ResponseMessage>>();
        for (AgentInfoBo agentInfo : agentInfoList) {
            TCommandTransfer transferObject = createCommandTransferObject(agentInfo, payload);
            PinpointServer collector = pinpointSocketManager.getCollector(agentInfo);
            Future<ResponseMessage> future = collector.request(serialize(transferObject));
            futureMap.put(agentInfo, future);
        }

        long startTime = System.currentTimeMillis();

        Map<AgentInfoBo, PinpointRouteResponse> result = new HashMap<AgentInfoBo, PinpointRouteResponse>();
        for (Map.Entry<AgentInfoBo, Future<ResponseMessage>> futureEntry : futureMap.entrySet()) {
            AgentInfoBo agentInfo = futureEntry.getKey();
            Future<ResponseMessage> future = futureEntry.getValue();
            PinpointRouteResponse response = getResponse(future, getTimeoutMillis(startTime, timeout));
            result.put(agentInfo, response);
        }

        return result;
    }

    @Override
    public AgentActiveThreadStatusList getActiveThreadStatus(List<AgentInfoBo> agentInfoList) throws TException {
        byte[] activeThread = serialize(new TActiveThread());
        return getActiveThreadStatus(agentInfoList, activeThread);
    }

    @Override
    public AgentActiveThreadStatusList getActiveThreadStatus(List<AgentInfoBo> agentInfoList, byte[] payload) throws TException {
        AgentActiveThreadStatusList agentActiveThreadStatusList = new AgentActiveThreadStatusList(agentInfoList.size());

        Map<AgentInfoBo, PinpointRouteResponse> responseList = invoke(agentInfoList, payload);
        for (Map.Entry<AgentInfoBo, PinpointRouteResponse> entry : responseList.entrySet()) {
            AgentInfoBo agentInfo = entry.getKey();
            PinpointRouteResponse response = entry.getValue();

            AgentActiveThreadStatus agentActiveThreadStatus = new AgentActiveThreadStatus(agentInfo.getHostName(), response.getRouteResult(), response.getResponse(TActiveThreadResponse.class, null));
            agentActiveThreadStatusList.add(agentActiveThreadStatus);
        }
        return agentActiveThreadStatusList;
    }

    private byte[] serialize(TBase tBase) throws TException {
        return SerializationUtils.serialize(tBase, commandSerializerFactory);
    }

    private TBase deserialize(byte[] objectData) throws TException {
        return SerializationUtils.deserialize(objectData, commandDeserializerFactory);
    }

    private TBase deserialize(byte[] objectData, TBase defaultValue) throws TException {
        return SerializationUtils.deserialize(objectData, commandDeserializerFactory, defaultValue);
    }

    private TCommandTransfer createCommandTransferObject(AgentInfoBo agentInfo, byte[] payload) {
        TCommandTransfer transferObject = new TCommandTransfer();
        transferObject.setApplicationName(agentInfo.getApplicationName());
        transferObject.setAgentId(agentInfo.getAgentId());
        transferObject.setStartTime(agentInfo.getStartTime());
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
