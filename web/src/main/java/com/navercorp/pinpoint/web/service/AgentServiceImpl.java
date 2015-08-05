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
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import com.navercorp.pinpoint.web.server.PinpointSocketManager;
import com.navercorp.pinpoint.web.vo.Range;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author Taejin Koo
 */
@Service
public class AgentServiceImpl implements AgentService {

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private PinpointSocketManager pinpointSocketManager;

    @Autowired
    private SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory;

    @Autowired
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    @Override
    public List<AgentInfoBo> get(String applicationName) {
        List<AgentInfoBo> agentInfoList = new ArrayList<AgentInfoBo>();

        long currentTime = System.currentTimeMillis();
        Range range = new Range(currentTime, currentTime);
        SortedMap<String, List<AgentInfoBo>> applicationAgentList = agentInfoService.getApplicationAgentList(applicationName, range);
        for (Map.Entry<String, List<AgentInfoBo>> entry : applicationAgentList.entrySet()) {
            AgentInfoBo agentInfo = ListUtils.getFirst(entry.getValue(), null);
            ListUtils.addIfValueNotNull(agentInfoList, agentInfo);
        }

        return agentInfoList;
    }

    @Override
    public Map<String, TActiveThreadResponse> getActiveThreadStatus(List<AgentInfoBo> agentInfoList) throws TException {
        byte[] activeThread = serialize(new TActiveThread());
        return getActiveThreadStatus(agentInfoList, activeThread);
    }

    @Override
    public Map<String, TActiveThreadResponse> getActiveThreadStatus(List<AgentInfoBo> agentInfoList, byte[] payload) throws TException {
        Map<String, Future<ResponseMessage>> futureMap = invoke(agentInfoList, payload);

        Map<String, TActiveThreadResponse> responseMap = new HashMap<String, TActiveThreadResponse>();
        for (Map.Entry<String, Future<ResponseMessage>> futureEntry : futureMap.entrySet()) {
            String hostName = futureEntry.getKey();
            Future<ResponseMessage> future = futureEntry.getValue();
            future.await();

            ResponseMessage responseMessage = future.getResult();
            TBase result = deserialize(responseMessage.getMessage());
            if (result instanceof TActiveThreadResponse) {
                responseMap.put(hostName, (TActiveThreadResponse) result);
            }
        }

        return responseMap;
    }

    private Map<String, Future<ResponseMessage>> invoke(List<AgentInfoBo> agentInfoList, byte[] payload) throws TException {
        Map<String, Future<ResponseMessage>> futureMap = new HashMap<String, Future<ResponseMessage>>();
        for (AgentInfoBo agentInfo : agentInfoList) {
            TCommandTransfer transferObject = createCommandTransferObject(agentInfo, payload);
            PinpointServer collector = pinpointSocketManager.getCollector(agentInfo);
            Future<ResponseMessage> future = collector.request(serialize(transferObject));

            futureMap.put(agentInfo.getHostName(), future);
        }
        return futureMap;
    }

    private byte[] serialize(TBase tBase) throws TException {
        return SerializationUtils.serialize(tBase, commandSerializerFactory);
    }

    private TBase deserialize(byte[] objectData) throws TException {
        return SerializationUtils.deserialize(objectData, commandDeserializerFactory);
    }

    private TCommandTransfer createCommandTransferObject(AgentInfoBo agentInfo, byte[] payload) {
        TCommandTransfer transferObject = new TCommandTransfer();
        transferObject.setApplicationName(agentInfo.getApplicationName());
        transferObject.setAgentId(agentInfo.getAgentId());
        transferObject.setStartTime(agentInfo.getStartTime());
        transferObject.setPayload(payload);

        return transferObject;
    }

}
