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

import static org.mockito.Mockito.*;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.transaction.Transaction;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Hyun Jeong
 */
public class DefaultSqlSessionModifierTest extends MyBatisClientModifierTest {

    @Mo    k
	private Configuration configura    ion
	@Mock
	private Executo        execu    or;
	
	@Override
	protected SqlSessi       n getSqlSession() {
		return new DefaultSqlSession(this.conf        uration     th    s.executor);
	}

	@Override
	@Test
	public void select       apShouldBeTraced() throws Exception {
		ObjectFactor        objectFactory = mock(ObjectFactory.class);
		when(this.configurat       on.getObjectFactory()).thenR        urn(obj    ctF    ctory);
		super.selectMapShouldBeTraced();
	}

	@Override
       @Test
	public void getConnectionShouldBeTraced() t       rows Exception {
		Transaction mockTransaction = mock(Transac    ion.class);
		when(this.executor.getTransaction()).thenReturn(mockTransaction);
	}
}
