package com.nhn.pinpoint.profiler.modifier.orm.ibatis;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.DummyInstrumentation;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.util.MockAgent;
import com.nhn.pinpoint.profiler.util.TestClassLoader;

/**
 *
 * @author Hyun Jeong
 */
public class SqlMapClientImplModifierTest {

	private final Logger logger = LoggerFactory.getLogger(SqlMapClientImplModifierTest.class);
	
	private static TestClassLoader loader;
	
	@Mock
	private SqlMapExecutorDelegate mockSqlMapExecutorDelegate;

	@Before
	public void setUp() throws Exception {
		System.setProperty("catalina.home", "test");
        PLoggerFactory.initialize(new Slf4jLoggerBinder());

        ProfilerConfig profilerConfig = new ProfilerConfig();

        String path = ProfilerConfig.class.getClassLoader().getResource("pinpoint.config").getPath();
        profilerConfig.readConfigFile(path);

        profilerConfig.setApplicationServerType(ServiceType.STAND_ALONE);
        DefaultAgent agent = new MockAgent("", new DummyInstrumentation(), profilerConfig);
        loader = new TestClassLoader(agent);
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testModify() throws Exception {
		final String className = SqlMapClientImplModifier.TARGET_CLASS_NAME.replace('/', '.');
		@SuppressWarnings("unchecked")
		Class<SqlMapClientImpl> sqlMapClientClazz = (Class<SqlMapClientImpl>)loader.loadClass(className);

		Constructor<?>[] constructors = sqlMapClientClazz.getConstructors();
		SqlMapClient sqlMapClient = (SqlMapClient)constructors[0].newInstance(mockSqlMapExecutorDelegate);
//		SqlMapClient sqlMapClient = sqlMapClientClazz.getDeclaredConstructor(SqlMapExecutorDelegate.class).newInstance(mockSqlMapExecutorDelegate);
		sqlMapClient.queryForList("abc");
	}

}
