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

import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapSessionImpl;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author HyunGil Jeong
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({ "org.apache.ibatis:ibatis-sqlmap:[2.3.4.726]", "org.mockito:mockito-core:4.8.1" })
public class SqlMapSessionIT extends SqlMapExecutorTestBase {

    private SqlMapClientImpl sqlMapClient;
    
    @BeforeEach
    public void beforeEach() {
        super.beforeEach();
        this.sqlMapClient = new SqlMapClientImpl(super.mockSqlMapExecutorDelegate);
    }

    @AfterEach
    public void afterEach() {
        this.sqlMapClient = null;
    }

    @Test
    public void methodCallWithNullSqlIdShouldOnlyTraceMethodName() throws Exception {
        SqlMapSession sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
        super.testAndVerifyInsertWithNullSqlId(sqlMapSession);
    }

    @Test
    public void insertShouldBeTraced() throws Exception {
        SqlMapSession sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
        super.testAndVerifyInsert(sqlMapSession);
    }

    @Test
    public void deleteShouldBeTraced() throws Exception {
        SqlMapSession sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
        super.testAndVerifyDelete(sqlMapSession);
    }

    @Test
    public void updateShouldBeTraced() throws Exception {
        SqlMapSession sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
        super.testAndVerifyUpdate(sqlMapSession);
    }

    @Test
    public void queryForListShouldBeTraced() throws Exception {
        SqlMapSession sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
        super.testAndVerifyQueryForList(sqlMapSession);
    }

    @Test
    public void queryForMapShouldBeTraced() throws Exception {
        SqlMapSession sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
        super.testAndVerifyQueryForMap(sqlMapSession);
    }

    @Test
    public void queryForObjectShouldBeTraced() throws Exception {
        SqlMapSession sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
        super.testAndVerifyQueryForObject(sqlMapSession);
    }

    @Test
    public void queryForPaginagedListShouldBeTraced() throws Exception {
        SqlMapSession sqlMapSession = new SqlMapSessionImpl(this.sqlMapClient);
        super.testAndVerifyQueryForPaginatedList(sqlMapSession);
    }

}
