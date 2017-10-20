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

import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelContext;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelMessageListener;
import com.navercorp.pinpoint.rpc.stream.StreamChannelStateChangeEventHandler;
import com.navercorp.pinpoint.web.cluster.PinpointRouteResponse;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCountList;
import com.navercorp.pinpoint.web.vo.AgentInfo;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public interface AgentService {

    AgentInfo getAgentInfo(String applicationName, String agentId);
    AgentInfo getAgentInfo(String applicationName, String agentId, long startTimeStamp);
    AgentInfo getAgentInfo(String applicationName, String agentId, long startTimeStamp, boolean checkDB);

    List<AgentInfo> getRecentAgentInfoList(String applicationName);
    List<AgentInfo> getRecentAgentInfoList(String applicationName, long timeDiff);

    boolean isConnected(AgentInfo agentInfo);

    PinpointRouteResponse invoke(AgentInfo agentInfo, TBase<?, ?> tBase) throws TException;
    PinpointRouteResponse invoke(AgentInfo agentInfo, TBase<?, ?> tBase, long timeout) throws TException;
    PinpointRouteResponse invoke(AgentInfo agentInfo, byte[] payload) throws TException;
    PinpointRouteResponse invoke(AgentInfo agentInfo, byte[] payload, long timeout) throws TException;

    Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, TBase<?, ?> tBase) throws TException;
    Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, TBase<?, ?> tBase, long timeout) throws TException;
    Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, byte[] payload) throws TException;
    Map<AgentInfo, PinpointRouteResponse> invoke(List<AgentInfo> agentInfoList, byte[] payload, long timeout) throws TException;

    ClientStreamChannelContext openStream(AgentInfo agentInfo, TBase<?, ?> tBase, ClientStreamChannelMessageListener messageListener) throws TException;
    ClientStreamChannelContext openStream(AgentInfo agentInfo, TBase<?, ?> tBase, ClientStreamChannelMessageListener messageListener, StreamChannelStateChangeEventHandler<ClientStreamChannel> stateChangeListener) throws TException;

    ClientStreamChannelContext openStream(AgentInfo agentInfo, byte[] payload, ClientStreamChannelMessageListener messageListener) throws TException;
    ClientStreamChannelContext openStream(AgentInfo agentInfo, byte[] payload, ClientStreamChannelMessageListener messageListener, StreamChannelStateChangeEventHandler<ClientStreamChannel> stateChangeListener) throws TException;

    AgentActiveThreadCountList getActiveThreadCount(List<AgentInfo> agentInfoList) throws TException;
    AgentActiveThreadCountList getActiveThreadCount(List<AgentInfo> agentInfoList, byte[] payload) throws TException;

    byte[] serializeRequest(TBase<?, ?> tBase) throws TException;
    byte[] serializeRequest(TBase<?, ?> tBase, byte[] defaultValue);

    TBase<?, ?> deserializeResponse(byte[] objectData) throws TException;
    TBase<?, ?> deserializeResponse(byte[] objectData, TBase<?, ?> defaultValue);

}
