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

import com.navercorp.pinpoint.web.cluster.ClusterDataManager;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class EmptyClusterDataManager implements ClusterDataManager {
    @Override
    public void start() throws InterruptedException, IOException, KeeperException, Exception {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean registerWebCluster(String zNodeName, byte[] contents) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getRegisteredAgentList(AgentInfo agentInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getRegisteredAgentList(String applicationName, String agentId, long startTimeStamp) {
        throw new UnsupportedOperationException();
    }
}
