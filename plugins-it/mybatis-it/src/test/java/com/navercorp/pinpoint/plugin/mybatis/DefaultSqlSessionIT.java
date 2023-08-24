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

package com.navercorp.pinpoint.plugin.mybatis;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests against mybatis 3.0.3+. Prior versions are missing some APIs that are called during the IT. (Most notably,
 * SqlSession's select and selectMap methods)
 * 
 * @author HyunGil Jeong
 */
@PinpointAgent(AgentPath.PATH)
@Dependency({ "org.mybatis:mybatis:[3.0.3,)", "org.mockito:mockito-core:4.8.1" })
@ImportPlugin("com.navercorp.pinpoint:pinpoint-mybatis-plugin")
public class DefaultSqlSessionIT extends SqlSessionTestBase {

    @Mock
    private Configuration configuration;

    @Mock
    private ObjectFactory objectFactory;

    @Mock
    private MappedStatement mappedStatement;

    @Mock
    private Executor executor;

    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        when(this.configuration.getObjectFactory()).thenReturn(this.objectFactory);
        when(this.configuration.getMappedStatement(anyString())).thenReturn(mappedStatement);
    }

    @AfterEach
    public void afterEach() throws Exception {
        openMocks.close();
    }

    @Override
    protected SqlSession getSqlSession() {
        return new DefaultSqlSession(this.configuration, this.executor, false);
    }

    @Test
    public void methodCallWithNullSqlIdShouldOnlyTraceMethodName() throws Exception {
        super.testAndVerifyInsertWithNullParameter();
    }

    @Test
    public void selectShouldBeTraced() throws Exception {
        super.testAndVerifySelect();
    }

    @Test
    public void selectOneShouldBeTraced() throws Exception {
        super.testAndVerifySelectOne();
    }

    @Test
    public void selectListShouldBeTraced() throws Exception {
        super.testAndVerifySelectList();
    }

    @Test
    public void selectMapShouldBeTraced() throws Exception {
        super.testAndVerifySelectMap();
    }

    @Test
    public void insertShouldBeTraced() throws Exception {
        super.testAndVerifyInsert();
    }

    @Test
    public void updateShouldBeTraced() throws Exception {
        super.testAndVerifyUpdate();
    }

    @Test
    public void deleteShouldBeTraced() throws Exception {
        super.testAndVerifyDelete();
    }

}
