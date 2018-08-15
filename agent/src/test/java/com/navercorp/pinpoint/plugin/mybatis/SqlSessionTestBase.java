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

package com.navercorp.pinpoint.plugin.mybatis;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

import java.lang.reflect.Method;

import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;

/**
 * @author HyunGil Jeong
 */
public abstract class SqlSessionTestBase {

    protected abstract SqlSession getSqlSession();

    protected final void testAndVerifyInsertWithNullParameter() throws Exception {
        // Given
        SqlSession sqlSession = getSqlSession();
        // When
        sqlSession.insert(null);
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method insert = sqlSession.getClass().getDeclaredMethod("insert", String.class);
        verifier.verifyTrace(event("MYBATIS", insert));
    }

    protected final void testAndVerifySelect() throws Exception {
        // Given
        final String selectId = "selectId";
        SqlSession sqlSession = getSqlSession();
        // When
        sqlSession.select(selectId, new DefaultResultHandler());
        sqlSession.select(selectId, new Object(), new DefaultResultHandler());
        sqlSession.select(selectId, new Object(), RowBounds.DEFAULT, new DefaultResultHandler());
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method select1 = sqlSession.getClass().getDeclaredMethod("select", String.class, ResultHandler.class);
        verifier.verifyTrace(event("MYBATIS", select1, Expectations.cachedArgs(selectId)));
        Method select2 = sqlSession.getClass().getDeclaredMethod("select", String.class, Object.class,
                ResultHandler.class);
        verifier.verifyTrace(event("MYBATIS", select2, Expectations.cachedArgs(selectId)));
        Method select3 = sqlSession.getClass().getDeclaredMethod("select", String.class, Object.class, RowBounds.class,
                ResultHandler.class);
        verifier.verifyTrace(event("MYBATIS", select3, Expectations.cachedArgs(selectId)));
    }

    protected final void testAndVerifySelectOne() throws Exception {
        // Given
        final String selectOneId = "selectOneId";
        SqlSession sqlSession = getSqlSession();
        // When
        sqlSession.selectOne(selectOneId);
        sqlSession.selectOne(selectOneId, new Object());
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method selectOne1 = sqlSession.getClass().getDeclaredMethod("selectOne", String.class);
        Method selectOne2 = sqlSession.getClass().getDeclaredMethod("selectOne", String.class, Object.class);
        verifier.verifyTrace(event("MYBATIS", selectOne1, Expectations.cachedArgs(selectOneId)));
        verifier.verifyTrace(event("MYBATIS", selectOne2, Expectations.cachedArgs(selectOneId)));
    }

    protected final void testAndVerifySelectList() throws Exception {
        // Given
        final String selectListId = "selectListId";
        SqlSession sqlSession = getSqlSession();
        // When
        sqlSession.selectList(selectListId);
        sqlSession.selectList(selectListId, new Object());
        sqlSession.selectList(selectListId, new Object(), RowBounds.DEFAULT);
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method selectList1 = sqlSession.getClass().getDeclaredMethod("selectList", String.class);
        Method selectList2 = sqlSession.getClass().getDeclaredMethod("selectList", String.class, Object.class);
        Method selectList3 = sqlSession.getClass().getDeclaredMethod("selectList", String.class, Object.class,
                RowBounds.class);
        verifier.verifyTrace(event("MYBATIS", selectList1, Expectations.cachedArgs(selectListId)));
        verifier.verifyTrace(event("MYBATIS", selectList2, Expectations.cachedArgs(selectListId)));
        verifier.verifyTrace(event("MYBATIS", selectList3, Expectations.cachedArgs(selectListId)));
    }

    protected final void testAndVerifySelectMap() throws Exception {
        // Given
        final String selectMapId = "selectListId";
        SqlSession sqlSession = getSqlSession();
        // When
        sqlSession.selectMap(selectMapId, "key");
        sqlSession.selectMap(selectMapId, new Object(), "key");
        sqlSession.selectMap(selectMapId, new Object(), "key", RowBounds.DEFAULT);
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method selectMap1 = sqlSession.getClass().getDeclaredMethod("selectMap", String.class, String.class);
        Method selectMap2 = sqlSession.getClass().getDeclaredMethod("selectMap", String.class, Object.class,
                String.class);
        Method selectMap3 = sqlSession.getClass().getDeclaredMethod("selectMap", String.class, Object.class,
                String.class, RowBounds.class);
        verifier.verifyTrace(event("MYBATIS", selectMap1, Expectations.cachedArgs(selectMapId)));
        verifier.verifyTrace(event("MYBATIS", selectMap2, Expectations.cachedArgs(selectMapId)));
        verifier.verifyTrace(event("MYBATIS", selectMap3, Expectations.cachedArgs(selectMapId)));
    }

    protected final void testAndVerifyInsert() throws Exception {
        // Given
        final String insertId = "insertId";
        SqlSession sqlSession = getSqlSession();
        // When
        sqlSession.insert(insertId);
        sqlSession.insert(insertId, new Object());
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method insert1 = sqlSession.getClass().getDeclaredMethod("insert", String.class);
        Method insert2 = sqlSession.getClass().getDeclaredMethod("insert", String.class, Object.class);
        verifier.verifyTrace(event("MYBATIS", insert1, Expectations.cachedArgs(insertId)));
        verifier.verifyTrace(event("MYBATIS", insert2, Expectations.cachedArgs(insertId)));
    }

    protected final void testAndVerifyUpdate() throws Exception {
        // Given
        final String updateId = "updateId";
        SqlSession sqlSession = getSqlSession();
        // When
        sqlSession.update(updateId);
        sqlSession.update(updateId, new Object());
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method update1 = sqlSession.getClass().getDeclaredMethod("update", String.class);
        Method update2 = sqlSession.getClass().getDeclaredMethod("update", String.class, Object.class);
        verifier.verifyTrace(event("MYBATIS", update1, Expectations.cachedArgs(updateId)));
        verifier.verifyTrace(event("MYBATIS", update2, Expectations.cachedArgs(updateId)));
    }

    protected final void testAndVerifyDelete() throws Exception {
        // Given
        final String deleteId = "deleteId";
        SqlSession sqlSession = getSqlSession();
        // When
        sqlSession.delete(deleteId);
        sqlSession.delete(deleteId, new Object());
        // Then
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        Method delete1 = sqlSession.getClass().getDeclaredMethod("delete", String.class);
        Method delete2 = sqlSession.getClass().getDeclaredMethod("delete", String.class, Object.class);
        verifier.verifyTrace(event("MYBATIS", delete1, Expectations.cachedArgs(deleteId)));
        verifier.verifyTrace(event("MYBATIS", delete2, Expectations.cachedArgs(deleteId)));
    }

}
