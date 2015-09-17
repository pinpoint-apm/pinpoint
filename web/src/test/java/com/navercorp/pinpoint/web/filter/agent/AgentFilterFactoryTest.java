/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.filter.agent;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class AgentFilterFactoryTest {


    @Test
    public void fromFilter() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory("a", "b");
        AgentFilter filter = factory.createFromAgentFilter();

        Assert.assertTrue(filter.accept("a"));
        Assert.assertFalse(filter.accept("b"));
        Assert.assertFalse(filter.accept(null));
    }

    @Test
    public void fromFilter_skip() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory(null, "b");
        AgentFilter filter = factory.createFromAgentFilter();

        Assert.assertTrue(filter.accept("a"));
        Assert.assertTrue(filter.accept("b"));
        Assert.assertTrue(filter.accept(null));
    }

    @Test
    public void toFilter() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory("a", "b");
        AgentFilter filter = factory.createToAgentFilter();

        Assert.assertTrue(filter.accept("b"));

        Assert.assertFalse(filter.accept("a"));
        Assert.assertFalse(filter.accept(null));

        Assert.assertTrue(factory.toAgentExist());
    }

    @Test
    public void toFilter_skip() throws Exception {
        AgentFilterFactory factory = new AgentFilterFactory("a", null);
        AgentFilter filter = factory.createToAgentFilter();

        Assert.assertTrue(filter.accept("b"));
        Assert.assertTrue(filter.accept("a"));
        Assert.assertTrue(filter.accept(null));

    }



}