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

import java.lang.reflect.Method;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.ibatis.sqlmap.engine.scope.SessionScope;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;

/**
 * @author HyunGil Jeong
 */
public abstract class SqlMapExecutorTestBase {

    public class MockSqlMapExecutorDelegate extends SqlMapExecutorDelegate {
        @Override
        public SessionScope beginSessionScope() {
            return mockSessionScope;
        }
    }

    @Mock
    protected MockSqlMapExecutorDelegate mockSqlMapExecutorDelegate;

    @Mock
    private SessionScope mockSessionScope;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(this.mockSqlMapExecutorDelegate.beginSessionScope()).thenReturn(this.mockSessionScope);
    }

    protected final void testAndVerifyInsertWithNullSqlId(SqlMapExecutor executor) throws Exception {
        executor.insert(null);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method insert = executor.getClass().getDeclaredMethod("insert", String.class);
        verifier.verifyTrace(event("IBATIS", insert));
    }

    protected final void testAndVerifyInsert(SqlMapExecutor executor) throws Exception {
        final String insertId = "insertId";
        executor.insert(insertId);
        executor.insert(insertId, new Object());

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method insert = executor.getClass().getDeclaredMethod("insert", String.class);
        Method insertWithParameter = executor.getClass().getDeclaredMethod("insert", String.class, Object.class);
        verifier.verifyTrace(event("IBATIS", insert, Expectations.cachedArgs(insertId)));
        verifier.verifyTrace(event("IBATIS", insertWithParameter, Expectations.cachedArgs(insertId)));
    }

    protected final void testAndVerifyDelete(SqlMapExecutor executor) throws Exception {
        final String deleteId = "deleteId";
        executor.delete(deleteId);
        executor.delete(deleteId, new Object());

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        Method delete = executor.getClass().getDeclaredMethod("delete", String.class);
        Method deleteWithParameter = executor.getClass().getDeclaredMethod("delete", String.class, Object.class);
        verifier.verifyTrace(event("IBATIS", delete, Expectations.cachedArgs(deleteId)));
        verifier.verifyTrace(event("IBATIS", deleteWithParameter, Expectations.cachedArgs(deleteId)));
    }

    protected final void testAndVerifyUpdate(SqlMapExecutor executor) throws Exception {
        final String updateId = "updateId";
        executor.update(updateId);
        executor.update(updateId, new Object());

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method update = executor.getClass().getDeclaredMethod("update", String.class);
        Method updateWithParameter = executor.getClass().getDeclaredMethod("update", String.class, Object.class);
        verifier.verifyTrace(event("IBATIS", update, Expectations.cachedArgs(updateId)));
        verifier.verifyTrace(event("IBATIS", updateWithParameter, Expectations.cachedArgs(updateId)));
    }

    protected final void testAndVerifyQueryForList(SqlMapExecutor executor) throws Exception {
        final String queryForListId = "queryForListId";
        executor.queryForList(queryForListId);
        executor.queryForList(queryForListId, new Object());
        executor.queryForList(queryForListId, 0, 1);
        executor.queryForList(queryForListId, new Object(), 0, 1);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method queryForList1 = executor.getClass().getDeclaredMethod("queryForList", String.class);
        Method queryForList2 = executor.getClass().getDeclaredMethod("queryForList", String.class, Object.class);
        Method queryForList3 = executor.getClass()
                .getDeclaredMethod("queryForList", String.class, int.class, int.class);
        Method queryForList4 = executor.getClass().getDeclaredMethod("queryForList", String.class, Object.class,
                int.class, int.class);
        verifier.verifyTrace(event("IBATIS", queryForList1, Expectations.cachedArgs(queryForListId)));
        verifier.verifyTrace(event("IBATIS", queryForList2, Expectations.cachedArgs(queryForListId)));
        verifier.verifyTrace(event("IBATIS", queryForList3, Expectations.cachedArgs(queryForListId)));
        verifier.verifyTrace(event("IBATIS", queryForList4, Expectations.cachedArgs(queryForListId)));
    }

    protected final void testAndVerifyQueryForMap(SqlMapExecutor executor) throws Exception {
        final String queryForMapId = "queryForMapId";
        executor.queryForMap(queryForMapId, new Object(), "key");
        executor.queryForMap(queryForMapId, new Object(), "key", "value");

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method queryForMap1 = executor.getClass().getDeclaredMethod("queryForMap", String.class, Object.class,
                String.class);
        Method queryForMap2 = executor.getClass().getDeclaredMethod("queryForMap", String.class, Object.class,
                String.class, String.class);
        verifier.verifyTrace(event("IBATIS", queryForMap1, Expectations.cachedArgs(queryForMapId)));
        verifier.verifyTrace(event("IBATIS", queryForMap2, Expectations.cachedArgs(queryForMapId)));
    }

    protected final void testAndVerifyQueryForObject(SqlMapExecutor executor) throws Exception {
        final String queryForObjectId = "queryForObjectId";
        executor.queryForObject(queryForObjectId);
        executor.queryForObject(queryForObjectId, new Object());
        executor.queryForObject(queryForObjectId, new Object(), new Object());

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method queryForObject1 = executor.getClass().getDeclaredMethod("queryForObject", String.class);
        Method queryForObject2 = executor.getClass().getDeclaredMethod("queryForObject", String.class, Object.class);
        Method queryForObject3 = executor.getClass().getDeclaredMethod("queryForObject", String.class, Object.class,
                Object.class);
        verifier.verifyTrace(event("IBATIS", queryForObject1, Expectations.cachedArgs(queryForObjectId)));
        verifier.verifyTrace(event("IBATIS", queryForObject2, Expectations.cachedArgs(queryForObjectId)));
        verifier.verifyTrace(event("IBATIS", queryForObject3, Expectations.cachedArgs(queryForObjectId)));
    }

    @SuppressWarnings("deprecation")
    protected final void testAndVerifyQueryForPaginatedList(SqlMapExecutor executor) throws Exception {
        final String queryForPaginatedListId = "queryForPaginatedListId";
        executor.queryForPaginatedList(queryForPaginatedListId, 1);
        executor.queryForPaginatedList(queryForPaginatedListId, new Object(), 1);

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method queryForPaginatedList1 = executor.getClass().getDeclaredMethod("queryForPaginatedList", String.class,
                int.class);
        Method queryForPaginatedList2 = executor.getClass().getDeclaredMethod("queryForPaginatedList", String.class,
                Object.class, int.class);
        verifier.verifyTrace(event("IBATIS", queryForPaginatedList1, Expectations.cachedArgs(queryForPaginatedListId)));
        verifier.verifyTrace(event("IBATIS", queryForPaginatedList2, Expectations.cachedArgs(queryForPaginatedListId)));
    }

}
