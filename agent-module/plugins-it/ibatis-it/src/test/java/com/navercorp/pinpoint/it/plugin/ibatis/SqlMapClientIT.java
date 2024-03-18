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

package com.navercorp.pinpoint.it.plugin.ibatis;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.junit.jupiter.api.Test;


/**
 * @author HyunGil Jeong
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({ "org.apache.ibatis:ibatis-sqlmap:[2.3.4.726]", "org.mockito:mockito-core:4.8.1" })
public class SqlMapClientIT extends SqlMapExecutorTestBase {

    @Test
    public void methodCallWithNullSqlIdShouldOnlyTraceMethodName() throws Exception {
        SqlMapClient sqlMapClient = new SqlMapClientImpl(super.mockSqlMapExecutorDelegate);
        super.testAndVerifyInsertWithNullSqlId(sqlMapClient);
    }

    @Test
    public void insertShouldBeTraced() throws Exception {
        SqlMapClient sqlMapClient = new SqlMapClientImpl(super.mockSqlMapExecutorDelegate);
        super.testAndVerifyInsert(sqlMapClient);
    }

    @Test
    public void deleteShouldBeTraced() throws Exception {
        SqlMapClient sqlMapClient = new SqlMapClientImpl(super.mockSqlMapExecutorDelegate);
        super.testAndVerifyDelete(sqlMapClient);
    }

    @Test
    public void updateShouldBeTraced() throws Exception {
        SqlMapClient sqlMapClient = new SqlMapClientImpl(super.mockSqlMapExecutorDelegate);
        super.testAndVerifyUpdate(sqlMapClient);
    }

    @Test
    public void queryForListShouldBeTraced() throws Exception {
        SqlMapClient sqlMapClient = new SqlMapClientImpl(super.mockSqlMapExecutorDelegate);
        super.testAndVerifyQueryForList(sqlMapClient);
    }

    @Test
    public void queryForMapShouldBeTraced() throws Exception {
        SqlMapClient sqlMapClient = new SqlMapClientImpl(super.mockSqlMapExecutorDelegate);
        super.testAndVerifyQueryForMap(sqlMapClient);
    }

    @Test
    public void queryForObjectShouldBeTraced() throws Exception {
        SqlMapClient sqlMapClient = new SqlMapClientImpl(super.mockSqlMapExecutorDelegate);
        super.testAndVerifyQueryForObject(sqlMapClient);
    }

    @Test
    public void queryForPaginagedListShouldBeTraced() throws Exception {
        SqlMapClient sqlMapClient = new SqlMapClientImpl(super.mockSqlMapExecutorDelegate);
        super.testAndVerifyQueryForPaginatedList(sqlMapClient);
    }
}
