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
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannel;
import com.navercorp.pinpoint.rpc.stream.ClientStreamChannelEventHandler;
import com.navercorp.pinpoint.rpc.stream.StreamException;
import com.navercorp.pinpoint.web.cluster.ClusterKeyAndStatus;
import com.navercorp.pinpoint.web.cluster.PinpointRouteResponse;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.List;

/**
 * @author Taejin Koo
 */
public interface AgentService {

    ClusterKey getClusterKey(String applicationName, String agentId);
    ClusterKey getClusterKey(String applicationName, String agentId, long startTimeStamp);
    ClusterKey getClusterKey(String applicationName, String agentId, long startTimeStamp, boolean checkDB);

    List<ClusterKeyAndStatus> getRecentAgentInfoList(String applicationName, long timeDiff);

    boolean isConnected(ClusterKey clusterKey);

    PinpointRouteResponse invoke(ClusterKey clusterKey, TBase<?, ?> tBase) throws TException;
    PinpointRouteResponse invoke(ClusterKey clusterKey, byte[] payload) throws TException;
    PinpointRouteResponse invoke(ClusterKey clusterKey, byte[] payload, long timeout) throws TException;

    ClientStreamChannel openStream(ClusterKey clusterKey, TBase<?, ?> tBase, ClientStreamChannelEventHandler streamChannelEventHandler) throws TException, StreamException;
    ClientStreamChannel openStream(ClusterKey clusterKey, byte[] payload, ClientStreamChannelEventHandler streamChannelEventHandler) throws TException, StreamException;

    byte[] serializeRequest(TBase<?, ?> tBase) throws TException;

    TBase<?, ?> deserializeResponse(byte[] objectData, Message<TBase<?, ?>> defaultValue);

}
