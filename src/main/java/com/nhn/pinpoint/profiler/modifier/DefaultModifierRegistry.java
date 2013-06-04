package com.nhn.pinpoint.profiler.modifier;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.modifier.arcus.ArcusClientModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.BaseOperationModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.CacheManagerModifier;
import com.nhn.pinpoint.profiler.modifier.arcus.MemcachedClientModifier;
import com.nhn.pinpoint.profiler.modifier.bloc.handler.HTTPHandlerModifier;
import com.nhn.pinpoint.profiler.modifier.connector.httpclient4.HttpClient4Modifier;
import com.nhn.pinpoint.profiler.modifier.connector.jdkhttpconnector.HttpURLConnectionModifier;
import com.nhn.pinpoint.profiler.modifier.db.cubrid.CubridPreparedStatementModifier;
import com.nhn.pinpoint.profiler.modifier.db.cubrid.CubridResultSetModifier;
import com.nhn.pinpoint.profiler.modifier.db.cubrid.CubridStatementModifier;
import com.nhn.pinpoint.profiler.modifier.db.cubrid.CubridUStatementModifier;
import com.nhn.pinpoint.profiler.modifier.db.dbcp.DBCPBasicDataSourceModifier;
import com.nhn.pinpoint.profiler.modifier.db.dbcp.DBCPPoolModifier;
import com.nhn.pinpoint.profiler.modifier.db.mssql.MSSQLConnectionModifier;
import com.nhn.pinpoint.profiler.modifier.db.mssql.MSSQLPreparedStatementModifier;
import com.nhn.pinpoint.profiler.modifier.db.mssql.MSSQLResultSetModifier;
import com.nhn.pinpoint.profiler.modifier.db.mssql.MSSQLStatementModifier;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLConnectionImplModifier;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLNonRegisteringDriverModifier;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLPreparedStatementJDBC4Modifier;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLPreparedStatementModifier;
import com.nhn.pinpoint.profiler.modifier.db.mysql.MySQLStatementModifier;
import com.nhn.pinpoint.profiler.modifier.db.oracle.PhysicalConnectionModifier;
import com.nhn.pinpoint.profiler.modifier.method.MethodModifier;
import com.nhn.pinpoint.profiler.modifier.servlet.FilterModifier;
import com.nhn.pinpoint.profiler.modifier.db.oracle.OracleDriverModifier;
import com.nhn.pinpoint.profiler.modifier.db.oracle.OracleStatementWrapperModifier;
import com.nhn.pinpoint.profiler.modifier.servlet.HttpServletModifier;
import com.nhn.pinpoint.profiler.modifier.servlet.SpringFrameworkServletModifier;
import com.nhn.pinpoint.profiler.modifier.tomcat.CatalinaModifier;
import com.nhn.pinpoint.profiler.modifier.tomcat.StandardHostValveInvokeModifier;
import com.nhn.pinpoint.profiler.modifier.tomcat.TomcatConnectorModifier;
import com.nhn.pinpoint.profiler.modifier.tomcat.TomcatStandardServiceModifier;
import com.nhn.pinpoint.profiler.modifier.db.oracle.OraclePreparedStatementWrapperModifier;

public class DefaultModifierRegistry implements ModifierRegistry {

	// 왠간해서는 동시성 상황이 안나올것으로 보임. 사이즈를 크게 잡아서 체인을 가능한 뒤지지 않도록함.
	private final Map<String, Modifier> registry = new HashMap<String, Modifier>(512);

	private final ByteCodeInstrumentor byteCodeInstrumentor;
	private final ProfilerConfig profilerConfig;
	private final Agent agent;

	public DefaultModifierRegistry(Agent agent) {
		this.agent = agent;
        // classLoader계층 구조 때문에 직접 type을 넣기가 애매하여 그냥 casting
        this.byteCodeInstrumentor = (ByteCodeInstrumentor) agent.getByteCodeInstrumentor();
        this.profilerConfig = agent.getProfilerConfig();
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
//			addDbcpDriver();
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
//      result set fectch counter를 만들어야 될듯.
//		Modifier mysqlResultSetModifier = new MySQLResultSetModifier(byteCodeInstrumentor, agent);
//		addModifier(mysqlResultSetModifier);
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
        Modifier oracleDriverModifier = new OracleDriverModifier(byteCodeInstrumentor, agent);
        addModifier(oracleDriverModifier);

        // TODO PhysicalConnection으로 하니 view에서 api가 phy로 나와 모양이 나쁘다.
        // 최상위인 클래스인 T4C T2C, OCI 따로 다 처리하는게 이쁠듯하다.
        Modifier oracleConnectionModifier = new PhysicalConnectionModifier(byteCodeInstrumentor, agent);
        addModifier(oracleConnectionModifier);

		Modifier oraclePreparedStatementModifier = new OraclePreparedStatementWrapperModifier(byteCodeInstrumentor, agent);
		addModifier(oraclePreparedStatementModifier);

		Modifier oracleStatement = new OracleStatementWrapperModifier(byteCodeInstrumentor, agent);
		addModifier(oracleStatement);
//
//		Modifier oracleResultSetModifier = new OracleResultSetModifier(byteCodeInstrumentor, agent);
//		addModifier(oracleResultSetModifier);
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
