/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ibatis;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.scope.SessionScope;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.Version;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;

/**
 * Tests against spring-ibatis 2.0.7+. Prior versions require com.ibatis:ibatis2 dependency, which is not available in the repository.
 * 
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent("agent/target/pinpoint-agent-" + Version.VERSION)
@Dependency({ "org.springframework:spring-ibatis:[2.0.7,)", "org.apache.ibatis:ibatis-sqlmap:[2.3.4.726]",
        "org.mockito:mockito-all:1.8.4" })
public class SqlMapClientTemplateIT {

    public class MockSqlMapExecutorDelegate extends SqlMapExecutorDelegate {
        @Override
        public SessionScope beginSessionScope() {
            return mockSessionScope;
        }
    }

    @Mock
    private MockSqlMapExecutorDelegate mockSqlMapExecutorDelegate;

    @Mock
    private SessionScope mockSessionScope;

    @Mock
    private DataSource mockDataSource;

    private SqlMapClient sqlMapClient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.mockSqlMapExecutorDelegate.beginSessionScope()).thenReturn(this.mockSessionScope);
        this.sqlMapClient = new SqlMapClientImpl(this.mockSqlMapExecutorDelegate);
    }

    @Test
    public void methodCallWithNullSqlIdShouldOnlyTraceMethodName() throws Exception {
        // Given
        SqlMapClientTemplate template = new SqlMapClientTemplate(this.mockDataSource, this.sqlMapClient);
        // When
        template.insert(null);
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method insert = SqlMapClientTemplate.class.getDeclaredMethod("insert", String.class);
        verifier.verifyTrace(event("IBATIS_SPRING", insert));
    }

    @Test
    public void insertShouldBeTraced() throws Exception {
        // Given
        final String insertId = "insertId";
        SqlMapClientTemplate clientTemplate = new SqlMapClientTemplate(this.mockDataSource, this.sqlMapClient);
        // When
        clientTemplate.insert(insertId);
        clientTemplate.insert(insertId, new Object());
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method insert = SqlMapClientTemplate.class.getDeclaredMethod("insert", String.class);
        Method insertWithParameter = SqlMapClientTemplate.class.getDeclaredMethod("insert", String.class, Object.class);
        verifier.verifyTrace(event("IBATIS_SPRING", insert, Expectations.cachedArgs(insertId)));
        verifier.verifyTrace(event("IBATIS_SPRING", insertWithParameter, Expectations.cachedArgs(insertId)));
    }

    @Test
    public void deleteShouldBeTraced() throws Exception {
        // Given
        final String deleteId = "deleteId";
        SqlMapClientTemplate clientTemplate = new SqlMapClientTemplate(this.mockDataSource, this.sqlMapClient);
        // When
        clientTemplate.delete(deleteId);
        clientTemplate.delete(deleteId, new Object());
        clientTemplate.delete(deleteId, new Object(), 0);
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method delete1 = SqlMapClientTemplate.class.getDeclaredMethod("delete", String.class);
        Method delete2 = SqlMapClientTemplate.class.getDeclaredMethod("delete", String.class, Object.class);
        Method delete3 = SqlMapClientTemplate.class.getDeclaredMethod("delete", String.class, Object.class, int.class);
        verifier.verifyTrace(event("IBATIS_SPRING", delete1, Expectations.cachedArgs(deleteId)));
        verifier.verifyTrace(event("IBATIS_SPRING", delete2, Expectations.cachedArgs(deleteId)));
        verifier.verifyTrace(event("IBATIS_SPRING", delete3, Expectations.cachedArgs(deleteId)));
    }

    @Test
    public void updateShouldBeTraced() throws Exception {
        // Given
        final String updateId = "updateId";
        SqlMapClientTemplate clientTemplate = new SqlMapClientTemplate(this.mockDataSource, this.sqlMapClient);
        // When
        clientTemplate.update(updateId);
        clientTemplate.update(updateId, new Object());
        clientTemplate.update(updateId, new Object(), 0);
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method update1 = SqlMapClientTemplate.class.getDeclaredMethod("update", String.class);
        Method update2 = SqlMapClientTemplate.class.getDeclaredMethod("update", String.class, Object.class);
        Method update3 = SqlMapClientTemplate.class.getDeclaredMethod("update", String.class, Object.class, int.class);
        verifier.verifyTrace(event("IBATIS_SPRING", update1, Expectations.cachedArgs(updateId)));
        verifier.verifyTrace(event("IBATIS_SPRING", update2, Expectations.cachedArgs(updateId)));
        verifier.verifyTrace(event("IBATIS_SPRING", update3, Expectations.cachedArgs(updateId)));
    }

    @Test
    public void queryForListShouldBeTraced() throws Exception {
        // Given
        final String queryForListId = "queryForListId";
        SqlMapClientTemplate clientTemplate = new SqlMapClientTemplate(this.mockDataSource, this.sqlMapClient);
        // When
        clientTemplate.queryForList(queryForListId);
        clientTemplate.queryForList(queryForListId, new Object());
        clientTemplate.queryForList(queryForListId, 0, 1);
        clientTemplate.queryForList(queryForListId, new Object(), 0, 1);
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method queryForList1 = SqlMapClientTemplate.class.getDeclaredMethod("queryForList", String.class);
        Method queryForList2 = SqlMapClientTemplate.class.getDeclaredMethod("queryForList", String.class, Object.class);
        Method queryForList3 = SqlMapClientTemplate.class.getDeclaredMethod("queryForList", String.class, int.class,
                int.class);
        Method queryForList4 = SqlMapClientTemplate.class.getDeclaredMethod("queryForList", String.class, Object.class,
                int.class, int.class);
        verifier.verifyTrace(event("IBATIS_SPRING", queryForList1, Expectations.cachedArgs(queryForListId)));
        verifier.verifyTrace(event("IBATIS_SPRING", queryForList2, Expectations.cachedArgs(queryForListId)));
        verifier.verifyTrace(event("IBATIS_SPRING", queryForList3, Expectations.cachedArgs(queryForListId)));
        verifier.verifyTrace(event("IBATIS_SPRING", queryForList4, Expectations.cachedArgs(queryForListId)));
    }

    @Test
    public void queryForMapShouldBeTraced() throws Exception {
        // Given
        final String queryForMapId = "queryForMapId";
        SqlMapClientTemplate clientTemplate = new SqlMapClientTemplate(this.mockDataSource, this.sqlMapClient);
        // When
        clientTemplate.queryForMap(queryForMapId, new Object(), "key");
        clientTemplate.queryForMap(queryForMapId, new Object(), "key", "value");
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method queryForMap1 = SqlMapClientTemplate.class.getDeclaredMethod("queryForMap", String.class, Object.class,
                String.class);
        Method queryForMap2 = SqlMapClientTemplate.class.getDeclaredMethod("queryForMap", String.class, Object.class,
                String.class, String.class);
        verifier.verifyTrace(event("IBATIS_SPRING", queryForMap1, Expectations.cachedArgs(queryForMapId)));
        verifier.verifyTrace(event("IBATIS_SPRING", queryForMap2, Expectations.cachedArgs(queryForMapId)));
    }

    @Test
    public void queryForObjectShouldBeTraced() throws Exception {
        // Given
        final String queryForObjectId = "queryForObjectId";
        SqlMapClientTemplate clientTemplate = new SqlMapClientTemplate(this.mockDataSource, this.sqlMapClient);
        // When
        clientTemplate.queryForObject(queryForObjectId);
        clientTemplate.queryForObject(queryForObjectId, new Object());
        clientTemplate.queryForObject(queryForObjectId, new Object(), new Object());
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method queryForObject1 = SqlMapClientTemplate.class.getDeclaredMethod("queryForObject", String.class);
        Method queryForObject2 = SqlMapClientTemplate.class.getDeclaredMethod("queryForObject", String.class,
                Object.class);
        Method queryForObject3 = SqlMapClientTemplate.class.getDeclaredMethod("queryForObject", String.class,
                Object.class, Object.class);
        verifier.verifyTrace(event("IBATIS_SPRING", queryForObject1, Expectations.cachedArgs(queryForObjectId)));
        verifier.verifyTrace(event("IBATIS_SPRING", queryForObject2, Expectations.cachedArgs(queryForObjectId)));
        verifier.verifyTrace(event("IBATIS_SPRING", queryForObject3, Expectations.cachedArgs(queryForObjectId)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void queryForPaginagedListShouldBeTraced() throws Exception {
        // Given
        final String queryForPaginatedListId = "queryForPaginatedListId";
        // to emulate lazy-loading, otherwise exception is thrown
        TransactionManager mockTxManager = mock(TransactionManager.class);
        when(this.mockSqlMapExecutorDelegate.getTxManager()).thenReturn(mockTxManager);
        SqlMapClientTemplate clientTemplate = new SqlMapClientTemplate(this.mockDataSource, this.sqlMapClient);
        // When
        clientTemplate.queryForPaginatedList(queryForPaginatedListId, 1);
        clientTemplate.queryForPaginatedList(queryForPaginatedListId, new Object(), 1);
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method queryForPaginatedList1 = SqlMapClientTemplate.class.getDeclaredMethod("queryForPaginatedList",
                String.class, int.class);
        Method queryForPaginatedList2 = SqlMapClientTemplate.class.getDeclaredMethod("queryForPaginatedList",
                String.class, Object.class, int.class);
        verifier.verifyTrace(event("IBATIS_SPRING", queryForPaginatedList1,
                Expectations.cachedArgs(queryForPaginatedListId)));
        verifier.verifyTrace(event("IBATIS_SPRING", queryForPaginatedList2,
                Expectations.cachedArgs(queryForPaginatedListId)));
    }

}
