/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.cluster;

import com.navercorp.pinpoint.common.server.cluster.AgentInfoKey;
import org.junit.Assert;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Set;


/**
 * @author Woonduk Kang(emeroad)
 */
public class CollectorClusterInfoRepositoryTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void test() throws Exception {

        CollectorClusterInfoRepository info = new CollectorClusterInfoRepository();

        final AgentInfoKey agent1 = new AgentInfoKey("app", "agent1", 0);
        final AgentInfoKey agent2 = new AgentInfoKey("app", "agent2", 1);
        final Set<AgentInfoKey> profilerInfos = Set.of(agent1, agent2);

        ClusterId clusterId = new ClusterId("/path", "/collectorA", "appName");
        info.put(clusterId, profilerInfos);

        List<ClusterId> collectorList = info.get(agent1);
        logger.debug("{}", collectorList);
        Assert.assertEquals(clusterId, collectorList.get(0));

        info.remove(clusterId);
        Assert.assertTrue("Not found", info.get(agent1).isEmpty());
    }


}