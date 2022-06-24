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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

/**
 * @author Taejin Koo
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class DataSourceCodecV2Test extends AgentStatCodecTestBase<DataSourceListBo> {

    @Autowired
    private AgentStatCodec<DataSourceListBo> codec;

    @Override
    protected List<DataSourceListBo> createAgentStats(String agentId, long startTimestamp, long initialTimestamp) {
        return TestAgentStatFactory.createDataSourceListBos(agentId, startTimestamp, initialTimestamp);
    }

    @Override
    protected AgentStatCodec<DataSourceListBo> getCodec() {
        return codec;
    }

    @Override
    protected void verify(DataSourceListBo expected, DataSourceListBo actual) {
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId(), "agentId");
        Assertions.assertEquals(expected.getStartTimestamp(), actual.getStartTimestamp(), "startTimestamp");
        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp(), "timestamp");
        Assertions.assertEquals(expected.size(), actual.size());

        List<DataSourceBo> expectedDataSourceList = expected.getList();
        List<DataSourceBo> actualDataSourceList = actual.getList();

        for (int i = 0; i < expectedDataSourceList.size(); i++) {
            verify(expectedDataSourceList.get(i), actualDataSourceList.get(i));
        }
    }

    private void verify(DataSourceBo expected, DataSourceBo actual) {
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId(), "agentId");
        Assertions.assertEquals(expected.getStartTimestamp(), actual.getStartTimestamp(), "startTimestamp");
        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp(), "timestamp");

        Assertions.assertEquals(expected.getId(), actual.getId(), "id");
        Assertions.assertEquals(expected.getServiceTypeCode(), actual.getServiceTypeCode(), "serviceTypeCode");
        Assertions.assertEquals(expected.getDatabaseName(), actual.getDatabaseName(), "name");
        Assertions.assertEquals(expected.getJdbcUrl(), actual.getJdbcUrl(), "jdbcUrl");
        Assertions.assertEquals(expected.getActiveConnectionSize(), actual.getActiveConnectionSize(), "activeConnectionSize");
        Assertions.assertEquals(expected.getMaxConnectionSize(), actual.getMaxConnectionSize(), "maxConnectionSize");
    }

}

