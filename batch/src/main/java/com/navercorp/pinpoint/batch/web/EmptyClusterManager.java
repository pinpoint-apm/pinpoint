/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.batch.web;

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.web.cluster.ClusterManager;
import com.navercorp.pinpoint.web.config.WebClusterConfig;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class EmptyClusterManager extends ClusterManager {

    public EmptyClusterManager() {
        super(new WebClusterConfig(), new EmptyClusterConnectionManager(new WebClusterConfig()), new EmptyClusterDataManager());
    };

    @Override
    public void start() throws InterruptedException, IOException, KeeperException {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isConnected(AgentInfo agentInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PinpointSocket> getSocket(AgentInfo agentInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<PinpointSocket> getSocket(String applicationName, String agentId, long startTimeStamp) {
        throw new UnsupportedOperationException();
    }


}
