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

package com.navercorp.pinpoint.profiler.modifier.orm.mybatis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mybatis.spring.SqlSessionTemplate;

import com.navercorp.pinpoint.common.bo.SpanEventBo;

/**
 * @author Hyun Jeong
 */
public class SqlSessionTemplateModifierTest extends MyBatisClientModifierTest {

    private static final ExecutorType executorType = ExecutorType.SIMPL       ;
	
	private SqlSessionTemplate sqlSessionT       mp    ate;
	
	@Mock
	private SqlSessionFactory s       lS    ssionFactory;
	
	@Mock
	priv       te Sql    ession sqlSession;
	
	@Override
	pro       ected SqlSession getSqlSess          on() {    		ret    rn this.sqlSessionTemplate;
	}
	
	@O       erride
	@B       fore
	public void set       p() throws Exc       ption {
		super.setUp();
		setUpSqlSessionFactory();
		setUpSqlSession();
		this.sq          SessionTemplate = new SqlSessionTemplate(this.sqlSess       onFactory, executorType);
	}
	
	private void setUpSq       SessionFactory() throws Exception {
		Configuration configuration =       mock(Configuration.class);
		TransactionFac       ory transactionFactory = mock(TransactionFactory.class);
		DataSource dataSour       e = mock(DataSource.class);
		Environment environment = new Environmen       ("test", transactionFactory, dataSource);
		when(this.sql          essionFactory.getConfiguration()).thenReturn(c       nfiguration);
		when(configuration.getEnvironment()).thenReturn(environment);

	priv    te v    id setUpSqlSession() throws Exception
		when(this.sqlSessionFactory.openSession(        ecutorType)).thenReturn(this.sqlSession);
	}

	    Overrid
	@    fter
	public void cleanUp() throws Exception {
		//       S          ould not manually close          SqlSessionTemplate
	}

	@Ignore // Changed to trac        only query operations
	@Override
	@Tes
	public void commitShouldBeTraced() throws Exception
		try {
			super.commitShouldB          Traced();
			fail("SqlSessionTemplate cannot manua          ly call commit.");
		} catch (UnsupportedOperatio          Exception e) {
			final List<SpanEventBo> spanEvents = get             urrentSpanEvents();
			assertThat(spanEvents.s    ze(), i    (1)    ;
			final SpanEventBo commitSpanEventBo = spanEvents       g          t(0);
			assertThat(commi          SpanEventBo.hasException(), is(true));
			assertThat       commitSpanEventBo.getExceptionId(), not          NOT_CACHED));
		}
	}

	@Ignore // Changed to trace onl           query operations
	@Override
	@          est
	public void rollbackShouldBeTraced() throws Exc          ption {
		try {
			super.rollbackShouldBeTraced();
          		fail("SqlSessionTemplate cannot manually call rollback.");             		} catch (UnsupportedOperationException e) {
    		final    Lis    <SpanEventBo> spanEvents = getCurrentSpanEvents();       	          	assertThat(spanEvents       size(), is(1));
			final SpanEventBo ro          lbackSpanEventBo = spanEvents.get(0);
			assertThat(ro          lbackSpanEventBo.hasException()           is(true));
			assertThat(rollbackSpanEventBo.get          xceptionId(), not(NOT_CACHED));
		}
	}

	@Ignore          // Changed to trace only query operations
	@Override
	@Te                t
	public void closeShouldBeTraced() throws Exception {
		try {
			super.closeShouldBeTraced();
		} catch (UnsupportedOperationException e) {
			final List<SpanEventBo> spanEvents = getCurrentSpanEvents();
			assertThat(spanEvents.size(), is(1));
			final SpanEventBo closeSpanEventBo = spanEvents.get(0);
			assertThat(closeSpanEventBo.hasException(), is(true));
			assertThat(closeSpanEventBo.getExceptionId(), not(NOT_CACHED));
		}
	}
	
	
}
