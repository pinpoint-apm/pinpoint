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

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mybatis.spring.SqlSessionTemplate;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
@Ignore
public class SqlSessionTemplateITBase extends SqlSessionTestBase {

    private static final ExecutorType EXECUTOR_TYPE = ExecutorType.SIMPLE;

    @Mock
    private SqlSessionFactory sqlSessionFactory;

    @Mock
    private SqlSession sqlSessionProxy;

    private SqlSessionTemplate sqlSessionTemplate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Configuration configuration = mock(Configuration.class);
        TransactionFactory transactionFactory = mock(TransactionFactory.class);
        DataSource dataSource = mock(DataSource.class);
        Environment environment = new Environment("test", transactionFactory, dataSource);
        when(configuration.getEnvironment()).thenReturn(environment);
        when(this.sqlSessionFactory.getConfiguration()).thenReturn(configuration);
        when(this.sqlSessionFactory.openSession(EXECUTOR_TYPE)).thenReturn(this.sqlSessionProxy);
        this.sqlSessionTemplate = new SqlSessionTemplate(this.sqlSessionFactory, EXECUTOR_TYPE);
    }

    @Override
    protected SqlSession getSqlSession() {
        return this.sqlSessionTemplate;
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
