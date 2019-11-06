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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCount;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class AgentActiveThreadCountFactoryTest {

    @Test(expected = NullPointerException.class)
    public void assertAgentIdTest() throws Exception {
        AgentActiveThreadCountFactory factory = new AgentActiveThreadCountFactory();
        factory.create(new TCmdActiveThreadCountRes());
    }

    @Test
    public void invalidActiveThreadCountTest1() throws Exception {
        TCmdActiveThreadCountRes response = new TCmdActiveThreadCountRes();
        response.setActiveThreadCount(Arrays.asList(1, 2, 3));

        AgentActiveThreadCountFactory factory = new AgentActiveThreadCountFactory();
        factory.setAgentId("test");
        AgentActiveThreadCount agentActiveThreadCount = factory.create(response);

        Assert.assertEquals(factory.INTERNAL_ERROR.getCode(), agentActiveThreadCount.getCode());
        Assert.assertTrue(CollectionUtils.nullSafeSize(agentActiveThreadCount.getActiveThreadCountList()) == 0);
    }

    @Test
    public void invalidActiveThreadCountTest2() throws Exception {
        TCmdActiveThreadCountRes response = new TCmdActiveThreadCountRes();
        response.setActiveThreadCount(Arrays.asList(1, 2, 3, 4, 5));

        AgentActiveThreadCountFactory factory = new AgentActiveThreadCountFactory();
        factory.setAgentId("test");
        AgentActiveThreadCount agentActiveThreadCount = factory.create(response);

        Assert.assertEquals(factory.INTERNAL_ERROR.getCode(), agentActiveThreadCount.getCode());
        Assert.assertTrue(CollectionUtils.nullSafeSize(agentActiveThreadCount.getActiveThreadCountList()) == 0);
    }

    @Test
    public void invalidArgumentTest1() throws Exception {
        TCmdActiveThreadCount response = new TCmdActiveThreadCount();

        AgentActiveThreadCountFactory factory = new AgentActiveThreadCountFactory();
        factory.setAgentId("test");
        AgentActiveThreadCount agentActiveThreadCount = factory.create(response);

        Assert.assertEquals(factory.INTERNAL_ERROR.getCode(), agentActiveThreadCount.getCode());
        Assert.assertTrue(CollectionUtils.nullSafeSize(agentActiveThreadCount.getActiveThreadCountList()) == 0);
    }

}
