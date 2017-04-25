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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;


/**
 * @author Woonduk Kang(emeroad)
 */
public class CollectorClusterInfoRepositoryTest {

    private static final String PROFILER_SEPARATOR = CollectorClusterInfoRepository.PROFILER_SEPARATOR;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void test() throws Exception {

        CollectorClusterInfoRepository info = new CollectorClusterInfoRepository();

        final String agent1 = "app:agent1:0";
        final String agent2 = "app:agent2:1";
        final String profilerInfo = agent1 + PROFILER_SEPARATOR + agent2;

        byte[] profilterInfoBytes = profilerInfo.getBytes(StandardCharsets.UTF_8);
        info.put("collectorA", profilterInfoBytes);

        List<String> collectorList = info.get("app", "agent1", 0);
        logger.debug("{}", collectorList);
        Assert.assertEquals("collectorA", collectorList.get(0));

        info.remove("collectorA");
        Assert.assertTrue("Not found", info.get("app", "agent1", 0).isEmpty());
    }


}