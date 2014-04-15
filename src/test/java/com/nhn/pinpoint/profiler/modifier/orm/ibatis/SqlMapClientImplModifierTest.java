package com.nhn.pinpoint.profiler.modifier.orm.ibatis;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;

import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;

/**
 *
 * @author Hyun Jeong
 */
public class SqlMapClientImplModifierTest {

	private final Logger logger = LoggerFactory.getLogger(SqlMapClientImplModifierTest.class);
	
	@Mock
	private SqlMapExecutorDelegate mockSqlMapExecutorDelegate;


	
	@Test
	public void testModify() throws Exception {
		final String className = SqlMapClientImplModifier.TARGET_CLASS_NAME.replace('/', '.');


//		Class<SqlMapClientImpl> sqlMapClientClazz = (Class<SqlMapClientImpl>)loader.loadClass(className);
//        Class<SqlMapClientImpl> sqlMapClientClazz = (Class<SqlMapClientImpl>)loader.loadClass(className);
//
//        Constructor<?>[] constructors = sqlMapClientClazz.getConstructors();
//		SqlMapClient sqlMapClient = (SqlMapClient)constructors[0].newInstance(mockSqlMapExecutorDelegate);

        SqlMapClient sqlMapClient = new SqlMapClientImpl(mockSqlMapExecutorDelegate);
//		SqlMapClient sqlMapClient = sqlMapClientClazz.getDeclaredConstructor(SqlMapExecutorDelegate.class).newInstance(mockSqlMapExecutorDelegate);
		sqlMapClient.queryForList("abc");
	}

}
