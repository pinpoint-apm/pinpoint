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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatCodecTestBase;
import com.navercorp.pinpoint.common.server.bo.codec.stat.TestAgentStatFactory;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author Taejin Koo
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class DataSourceCodecV2Test extends AgentStatCodecTestBase<DataSourceListBo> {

    @Autowired
    private DataSourceCodecV2 dataSourceCodecV2;

    @Override
    protected List<DataSourceListBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createDataSourceListBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<DataSourceListBo> getCodec() {
        return dataSourceCodecV2;
    }

    @Override
    protected void verify(DataSourceListBo expected, DataSourceListBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("startTimestamp", expected.getStartTimestamp(), actual.getStartTimestamp());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals(expected.size(), actual.size());

        List<DataSourceBo> expectedDataSourceList = expected.getList();
        List<DataSourceBo> actualDataSourceList = actual.getList();

        for (int i = 0; i < expectedDataSourceList.size(); i++) {
            verify(expectedDataSourceList.get(i), actualDataSourceList.get(i));
        }
    }

    private void verify(DataSourceBo expected, DataSourceBo actual) {
        Assert.assertEquals("agentId", expected.getAgentId(), actual.getAgentId());
        Assert.assertEquals("startTimestamp", expected.getStartTimestamp(), actual.getStartTimestamp());
        Assert.assertEquals("timestamp", expected.getTimestamp(), actual.getTimestamp());

        Assert.assertEquals("id", expected.getId(), actual.getId());
        Assert.assertEquals("serviceTypeCode", expected.getServiceTypeCode(), actual.getServiceTypeCode());
        Assert.assertEquals("name", expected.getDatabaseName(), actual.getDatabaseName());
        Assert.assertEquals("jdbcUrl", expected.getJdbcUrl(), actual.getJdbcUrl());
        Assert.assertEquals("activeConnectionSize", expected.getActiveConnectionSize(), actual.getActiveConnectionSize());
        Assert.assertEquals("maxConnectionSize", expected.getMaxConnectionSize(), actual.getMaxConnectionSize());
    }

}

