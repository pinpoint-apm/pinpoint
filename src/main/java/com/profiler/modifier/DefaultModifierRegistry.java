package com.profiler.modifier;

import java.util.HashMap;
import java.util.Map;

import com.profiler.Agent;
import com.profiler.config.ProfilerConfig;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.modifier.arcus.ArcusClientModifier;
import com.profiler.modifier.arcus.BaseOperationModifier;
import com.profiler.modifier.arcus.CacheManagerModifier;
import com.profiler.modifier.arcus.MemcachedClientModifier;
import com.profiler.modifier.bloc.handler.HTTPHandlerModifier;
import com.profiler.modifier.connector.httpclient4.HttpClient4Modifier;
import com.profiler.modifier.connector.jdkhttpconnector.HttpURLConnectionModifier;
import com.profiler.modifier.db.cubrid.CubridPreparedStatementModifier;
import com.profiler.modifier.db.cubrid.CubridResultSetModifier;
import com.profiler.modifier.db.cubrid.CubridStatementModifier;
import com.profiler.modifier.db.cubrid.CubridUStatementModifier;
import com.profiler.modifier.db.dbcp.DBCPBasicDataSourceModifier;
import com.profiler.modifier.db.dbcp.DBCPPoolModifier;
import com.profiler.modifier.db.mssql.MSSQLConnectionModifier;
import com.profiler.modifier.db.mssql.MSSQLPreparedStatementModifier;
import com.profiler.modifier.db.mssql.MSSQLResultSetModifier;
import com.profiler.modifier.db.mssql.MSSQLStatementModifier;
import com.profiler.modifier.db.mysql.MySQLConnectionImplModifier;
import com.profiler.modifier.db.mysql.MySQLNonRegisteringDriverModifier;
import com.profiler.modifier.db.mysql.MySQLPreparedStatementJDBC4Modifier;
import com.profiler.modifier.db.mysql.MySQLPreparedStatementModifier;
import com.profiler.modifier.db.mysql.MySQLResultSetModifier;
import com.profiler.modifier.db.mysql.MySQLStatementModifier;
import com.profiler.modifier.db.oracle.OraclePreparedStatementModifier;
import com.profiler.modifier.db.oracle.OracleResultSetModifier;
import com.profiler.modifier.db.oracle.OracleStatementModifier;
import com.profiler.modifier.method.MethodModifier;
import com.profiler.modifier.servlet.FilterModifier;
import com.profiler.modifier.servlet.HttpServletModifier;
import com.profiler.modifier.servlet.SpringFrameworkServletModifier;
import com.profiler.modifier.tomcat.CatalinaModifier;
import com.profiler.modifier.tomcat.StandardHostValveInvokeModifier;
import com.profiler.modifier.tomcat.TomcatConnectorModifier;
import com.profiler.modifier.tomcat.TomcatStandardServiceModifier;

public class DefaultModifierRegistry implements ModifierRegistry {
	// TODO 혹시 동시성을 고려 해야 되는지 검토.
	// 왠간해서는 동시성 상황이 안나올것으로 보임.
	private Map<String, Modifier> registry = new HashMap<String, Modifier>(512);

	private final ByteCodeInstrumentor byteCodeInstrumentor;
	private final ProfilerConfig profilerConfig;
	private final Agent agent;

	public DefaultModifierRegistry(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent, ProfilerConfig profilerConfig) {
		this.byteCodeInstrumentor = byteCodeInstrumentor;
		this.agent = agent;
		this.profilerConfig = profilerConfig;
	}

	@Override
	public Modifier findModifier(String className) {
		return registry.get(className);
	}

	private void addModifier(Modifier modifier) {
		Modifier old = registry.put(modifier.getTargetClass(), modifier);
		if (old != null) {
			throw new IllegalStateException("Modifier already exist new:" + modifier.getClass() + " old:" + old.getTargetClass());
		}
	}
	
	public void addMethodModifier() {
		MethodModifier methodModifier = new MethodModifier(byteCodeInstrumentor, agent);
		addModifier(methodModifier);
	}

	public void addConnectorModifier() {
		FilterModifier filterModifier = new FilterModifier(byteCodeInstrumentor, agent);
		addModifier(filterModifier);

		HttpClient4Modifier httpClient4Modifier = new HttpClient4Modifier(byteCodeInstrumentor, agent);
		addModifier(httpClient4Modifier);

		MemcachedClientModifier memcachedClientModifier = new MemcachedClientModifier(byteCodeInstrumentor, agent);
		addModifier(memcachedClientModifier);

		ArcusClientModifier arcusClientModifier = new ArcusClientModifier(byteCodeInstrumentor, agent);
		addModifier(arcusClientModifier);

		BaseOperationModifier baseOperationModifier = new BaseOperationModifier(byteCodeInstrumentor, agent);
		addModifier(baseOperationModifier);

		CacheManagerModifier cacheManagerModifier = new CacheManagerModifier(byteCodeInstrumentor, agent);
		addModifier(cacheManagerModifier);

        // jdk HTTPUrlConnector
        HttpURLConnectionModifier httpURLConnectionModifier = new HttpURLConnectionModifier(byteCodeInstrumentor, agent);
        addModifier(httpURLConnectionModifier);
	}

	public void addBLOCModifier() {
		HTTPHandlerModifier httpHandlerModifier = new HTTPHandlerModifier(byteCodeInstrumentor, agent);
		addModifier(httpHandlerModifier);
	}

	public void addTomcatModifier() {
		StandardHostValveInvokeModifier standardHostValveInvokeModifier = new StandardHostValveInvokeModifier(byteCodeInstrumentor, agent);
		addModifier(standardHostValveInvokeModifier);

		HttpServletModifier httpServletModifier = new HttpServletModifier(byteCodeInstrumentor, agent);
		addModifier(httpServletModifier);

		SpringFrameworkServletModifier springServletModifier = new SpringFrameworkServletModifier(byteCodeInstrumentor, agent);
		addModifier(springServletModifier);

		Modifier tomcatStandardServiceModifier = new TomcatStandardServiceModifier(byteCodeInstrumentor, agent);
		addModifier(tomcatStandardServiceModifier);

		Modifier tomcatConnectorModifier = new TomcatConnectorModifier(byteCodeInstrumentor, agent);
		addModifier(tomcatConnectorModifier);

		Modifier tomcatCatalinaModifier = new CatalinaModifier(byteCodeInstrumentor, agent);
		addModifier(tomcatCatalinaModifier);
	}

	public void addJdbcModifier() {
		// TODO 드라이버 존재 체크 로직을 앞단으로 이동 시킬수 없는지 검토
		if (!profilerConfig.isJdbcProfile()) {
			return;
		}

		if (profilerConfig.isJdbcProfileMySql()) {
			addMySqlDriver();
		}

		if (profilerConfig.isJdbcProfileMsSql()) {
			addMsSqlDriver();
		}

		if (profilerConfig.isJdbcProfileOracle()) {
			addOracleDriver();
		}
		if (profilerConfig.isJdbcProfileCubrid()) {
			addCubridDriver();
		}

		if (profilerConfig.isJdbcProfileDbcp()) {
			addDbcpDriver();
		}
	}

	private void addMySqlDriver() {
		// TODO MySqlDriver는 버전별로 Connection이 interface인지 class인지가 다름. 문제 없는지
		// 확인필요.

		Modifier mysqlNonRegisteringDriverModifier = new MySQLNonRegisteringDriverModifier(byteCodeInstrumentor, agent);
		addModifier(mysqlNonRegisteringDriverModifier);

		Modifier mysqlConnectionImplModifier = new MySQLConnectionImplModifier(byteCodeInstrumentor, agent);
		addModifier(mysqlConnectionImplModifier);

		Modifier mysqlStatementModifier = new MySQLStatementModifier(byteCodeInstrumentor, agent);
		addModifier(mysqlStatementModifier);

		Modifier mysqlPreparedStatementModifier = new MySQLPreparedStatementModifier(byteCodeInstrumentor, agent);
		addModifier(mysqlPreparedStatementModifier);

		MySQLPreparedStatementJDBC4Modifier myqlPreparedStatementJDBC4Modifier = new MySQLPreparedStatementJDBC4Modifier(byteCodeInstrumentor, agent);
		addModifier(myqlPreparedStatementJDBC4Modifier);

		Modifier mysqlResultSetModifier = new MySQLResultSetModifier(byteCodeInstrumentor, agent);
		addModifier(mysqlResultSetModifier);
	}

	private void addMsSqlDriver() {

		Modifier mssqlConnectionModifier = new MSSQLConnectionModifier(byteCodeInstrumentor, agent);
		addModifier(mssqlConnectionModifier);

		Modifier mssqlStatementModifier = new MSSQLStatementModifier(byteCodeInstrumentor, agent);
		addModifier(mssqlStatementModifier);

		Modifier mssqlPreparedStatementModifier = new MSSQLPreparedStatementModifier(byteCodeInstrumentor, agent);
		addModifier(mssqlPreparedStatementModifier);

		Modifier mssqlResultSetModifier = new MSSQLResultSetModifier(byteCodeInstrumentor, agent);
		addModifier(mssqlResultSetModifier);

	}

	private void addOracleDriver() {

		// TODO oracle의 경우 connection에 대한 impl이 없음. 확인필요.
		Modifier oraclePreparedStatementModifier = new OraclePreparedStatementModifier(byteCodeInstrumentor, agent);
		addModifier(oraclePreparedStatementModifier);

		Modifier oracleStatement = new OracleStatementModifier(byteCodeInstrumentor, agent);
		addModifier(oracleStatement);

		Modifier oracleResultSetModifier = new OracleResultSetModifier(byteCodeInstrumentor, agent);
		addModifier(oracleResultSetModifier);
	}

	private void addCubridDriver() {

		// TODO cubrid의 경우도 connection에 대한 impl이 없음. 확인필요.
		Modifier cubridStatementModifier = new CubridStatementModifier(byteCodeInstrumentor, agent);
		addModifier(cubridStatementModifier);

		Modifier cubridPreparedStatementModifier = new CubridPreparedStatementModifier(byteCodeInstrumentor, agent);
		addModifier(cubridPreparedStatementModifier);

		Modifier cubridResultSetModifier = new CubridResultSetModifier(byteCodeInstrumentor, agent);
		addModifier(cubridResultSetModifier);

		Modifier cubridUStatementModifier = new CubridUStatementModifier(byteCodeInstrumentor, agent);
		addModifier(cubridUStatementModifier);
	}

	private void addDbcpDriver() {

		// TODO cubrid의 경우도 connection에 대한 impl이 없음. 확인필요.
		Modifier dbcpBasicDataSourceModifier = new DBCPBasicDataSourceModifier(byteCodeInstrumentor, agent);
		addModifier(dbcpBasicDataSourceModifier);

		Modifier dbcpPoolModifier = new DBCPPoolModifier(byteCodeInstrumentor, agent);
		addModifier(dbcpPoolModifier);
	}
}
