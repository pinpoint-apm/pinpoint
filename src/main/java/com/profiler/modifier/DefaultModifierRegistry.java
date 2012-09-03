package com.profiler.modifier;

import java.util.HashMap;
import java.util.Map;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.modifier.arcus.ArcusClientModifier;
import com.profiler.modifier.connector.HTTPClientModifier;
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
import com.profiler.modifier.db.mysql.MySQLPreparedStatementModifier;
import com.profiler.modifier.db.mysql.MySQLResultSetModifier;
import com.profiler.modifier.db.mysql.MySQLStatementModifier;
import com.profiler.modifier.db.oracle.OraclePreparedStatementModifier;
import com.profiler.modifier.db.oracle.OracleResultSetModifier;
import com.profiler.modifier.db.oracle.OracleStatementModifier;
import com.profiler.modifier.tomcat.CatalinaModifier;
import com.profiler.modifier.tomcat.EntryPointStandardHostValveModifier;
import com.profiler.modifier.tomcat.TomcatConnectorModifier;
import com.profiler.modifier.tomcat.TomcatStandardServiceModifier;

public class DefaultModifierRegistry implements ModifierRegistry {
	// TODO 혹시 동시성을 고려 해야 되는지 검토.
	private Map<String, Modifier> registry = new HashMap<String, Modifier>();

	private final ByteCodeInstrumentor byteCodeInstrumentor;

	public DefaultModifierRegistry(ByteCodeInstrumentor byteCodeInstrumentor) {
		this.byteCodeInstrumentor = byteCodeInstrumentor;
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

	public void addConnectorModifier() {
		HTTPClientModifier httpClientModifier = new HTTPClientModifier(byteCodeInstrumentor);
		addModifier(httpClientModifier);

		ArcusClientModifier arcusClientModifier = new ArcusClientModifier(byteCodeInstrumentor);
		addModifier(arcusClientModifier);
	}

	public void addTomcatModifier() {
		Modifier entryPointStandardHostValveModifier = new EntryPointStandardHostValveModifier(byteCodeInstrumentor);
		addModifier(entryPointStandardHostValveModifier);

		Modifier tomcatStandardServiceModifier = new TomcatStandardServiceModifier(byteCodeInstrumentor);
		addModifier(tomcatStandardServiceModifier);

		Modifier tomcatConnectorModifier = new TomcatConnectorModifier(byteCodeInstrumentor);
		addModifier(tomcatConnectorModifier);

		Modifier tomcatCatalinaModifier = new CatalinaModifier(byteCodeInstrumentor);
		addModifier(tomcatCatalinaModifier);
	}

	public void addJdbcModifier() {
		// TODO 드라이버 존재 체크 로직을 앞단으로 이동 시킬수 없는지 검토
		addMySqlDriver();

		addMsSqlDriver();

		addOracleDriver();

		addCubridDriver();

		addDbcpDriver();
	}

	private void addMySqlDriver() {
		// TODO MySqlDriver는 버전별로 Connection이 interface인지 class인지가 다름. 문제 없는지
		// 확인필요.
		Modifier mysqlConnectionImplModifier = new MySQLConnectionImplModifier(byteCodeInstrumentor);
		addModifier(mysqlConnectionImplModifier);

		Modifier mysqlStatementModifier = new MySQLStatementModifier(byteCodeInstrumentor);
		addModifier(mysqlStatementModifier);

		Modifier mysqlPreparedStatementModifier = new MySQLPreparedStatementModifier(byteCodeInstrumentor);
		addModifier(mysqlPreparedStatementModifier);

		Modifier mysqlResultSetModifier = new MySQLResultSetModifier(byteCodeInstrumentor);
		addModifier(mysqlResultSetModifier);
	}

	private void addMsSqlDriver() {
		Map<String, Modifier> registry = this.registry;
		Modifier mssqlConnectionModifier = new MSSQLConnectionModifier(byteCodeInstrumentor);
		addModifier(mssqlConnectionModifier);

		Modifier mssqlStatementModifier = new MSSQLStatementModifier(byteCodeInstrumentor);
		addModifier(mssqlStatementModifier);

		Modifier mssqlPreparedStatementModifier = new MSSQLPreparedStatementModifier(byteCodeInstrumentor);
		addModifier(mssqlPreparedStatementModifier);

		Modifier mssqlResultSetModifier = new MSSQLResultSetModifier(byteCodeInstrumentor);
		addModifier(mssqlResultSetModifier);

	}

	private void addOracleDriver() {
		Map<String, Modifier> registry = this.registry;
		// TODO oracle의 경우 connection에 대한 impl이 없음. 확인필요.
		Modifier oraclePreparedStatementModifier = new OraclePreparedStatementModifier(byteCodeInstrumentor);
		addModifier(oraclePreparedStatementModifier);

		Modifier oracleStatement = new OracleStatementModifier(byteCodeInstrumentor);
		addModifier(oracleStatement);

		Modifier oracleResultSetModifier = new OracleResultSetModifier(byteCodeInstrumentor);
		addModifier(oracleResultSetModifier);
	}

	private void addCubridDriver() {
		Map<String, Modifier> registry = this.registry;
		// TODO cubrid의 경우도 connection에 대한 impl이 없음. 확인필요.
		Modifier cubridStatementModifier = new CubridStatementModifier(byteCodeInstrumentor);
		addModifier(cubridStatementModifier);

		Modifier cubridPreparedStatementModifier = new CubridPreparedStatementModifier(byteCodeInstrumentor);
		addModifier(cubridPreparedStatementModifier);

		Modifier cubridResultSetModifier = new CubridResultSetModifier(byteCodeInstrumentor);
		addModifier(cubridResultSetModifier);

		Modifier cubridUStatementModifier = new CubridUStatementModifier(byteCodeInstrumentor);
		addModifier(cubridUStatementModifier);
	}

	private void addDbcpDriver() {
		Map<String, Modifier> registry = this.registry;
		// TODO cubrid의 경우도 connection에 대한 impl이 없음. 확인필요.
		Modifier dbcpBasicDataSourceModifier = new DBCPBasicDataSourceModifier(byteCodeInstrumentor);
		addModifier(dbcpBasicDataSourceModifier);

		Modifier dbcpPoolModifier = new DBCPPoolModifier(byteCodeInstrumentor);
		addModifier(dbcpPoolModifier);
	}
}
