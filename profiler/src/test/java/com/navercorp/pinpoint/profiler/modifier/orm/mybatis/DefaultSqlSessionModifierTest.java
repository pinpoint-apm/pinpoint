package com.nhn.pinpoint.profiler.modifier.orm.mybatis;

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

	@Mock
	private Configuration configuration;
	@Mock
	private Executor executor;
	
	@Override
	protected SqlSession getSqlSession() {
		return new DefaultSqlSession(this.configuration, this.executor);
	}

	@Override
	@Test
	public void selectMapShouldBeTraced() throws Exception {
		ObjectFactory objectFactory = mock(ObjectFactory.class);
		when(this.configuration.getObjectFactory()).thenReturn(objectFactory);
		super.selectMapShouldBeTraced();
	}

	@Override
	@Test
	public void getConnectionShouldBeTraced() throws Exception {
		Transaction mockTransaction = mock(Transaction.class);
		when(this.executor.getTransaction()).thenReturn(mockTransaction);
	}
}
