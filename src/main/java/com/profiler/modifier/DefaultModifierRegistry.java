package com.profiler.modifier;

import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;

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
import com.profiler.modifier.tomcat.EntryPointStandardHostValveModifier;
import com.profiler.modifier.tomcat.TomcatConnectorModifier;
import com.profiler.modifier.tomcat.TomcatStandardServiceModifier;

public class DefaultModifierRegistry implements ModifierRegistry {
	// TODO 혹시 동시성을 고려 해야 되는지 검토.
	private Map<String, Modifier> registry = new HashMap<String, Modifier>();

	private final ClassPool classPool;

	public DefaultModifierRegistry(ClassPool classPool) {
		this.classPool = classPool;
	}

	@Override
	public Modifier findModifier(String className) {
		return registry.get(className);
	}

	public void addTomcatModifier() {
		Map<String, Modifier> registry = this.registry;
		Modifier entryPointStandardHostValveModifier = new EntryPointStandardHostValveModifier(classPool);
		registry.put(entryPointStandardHostValveModifier.getTargetClass(), entryPointStandardHostValveModifier);

		Modifier tomcatStandardServiceModifier = new TomcatStandardServiceModifier(classPool);
		registry.put(tomcatStandardServiceModifier.getTargetClass(), tomcatStandardServiceModifier);

		Modifier tomcatConnectorModifier = new TomcatConnectorModifier(classPool);
		registry.put(tomcatConnectorModifier.getTargetClass(), tomcatConnectorModifier);
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
		Modifier mysqlConnectionImplModifier = new MySQLConnectionImplModifier(classPool);
		registry.put(mysqlConnectionImplModifier.getTargetClass(), mysqlConnectionImplModifier);

		Modifier mysqlStatementModifier = new MySQLStatementModifier(classPool);
		registry.put(mysqlStatementModifier.getTargetClass(), mysqlStatementModifier);

		Modifier mysqlPreparedStatementModifier = new MySQLPreparedStatementModifier(classPool);
		registry.put(mysqlPreparedStatementModifier.getTargetClass(), mysqlPreparedStatementModifier);

		Modifier mysqlResultSetModifier = new MySQLResultSetModifier(classPool);
		registry.put(mysqlResultSetModifier.getTargetClass(), mysqlResultSetModifier);
	}

	private void addMsSqlDriver() {
		Map<String, Modifier> registry = this.registry;
		Modifier mssqlConnectionModifier = new MSSQLConnectionModifier(classPool);
		registry.put(mssqlConnectionModifier.getTargetClass(), mssqlConnectionModifier);

		Modifier mssqlStatementModifier = new MSSQLStatementModifier(classPool);
		registry.put(mssqlStatementModifier.getTargetClass(), mssqlStatementModifier);

		Modifier mssqlPreparedStatementModifier = new MSSQLPreparedStatementModifier(classPool);
		registry.put(mssqlPreparedStatementModifier.getTargetClass(), mssqlPreparedStatementModifier);

		Modifier mssqlResultSetModifier = new MSSQLResultSetModifier(classPool);
		registry.put(mssqlResultSetModifier.getTargetClass(), mssqlResultSetModifier);

	}

	private void addOracleDriver() {
		Map<String, Modifier> registry = this.registry;
		// TODO oracle의 경우 connection에 대한 impl이 없음. 확인필요.
		Modifier oraclePreparedStatementModifier = new OraclePreparedStatementModifier(classPool);
		registry.put(oraclePreparedStatementModifier.getTargetClass(), oraclePreparedStatementModifier);

		Modifier oracleStatement = new OracleStatementModifier(classPool);
		registry.put(oracleStatement.getTargetClass(), oracleStatement);

		Modifier oracleResultSetModifier = new OracleResultSetModifier(classPool);
		registry.put(oracleResultSetModifier.getTargetClass(), oracleResultSetModifier);
	}

	private void addCubridDriver() {
		Map<String, Modifier> registry = this.registry;
		// TODO cubrid의 경우도 connection에 대한 impl이 없음. 확인필요.
		Modifier cubridStatementModifier = new CubridStatementModifier(classPool);
		registry.put(cubridStatementModifier.getTargetClass(), cubridStatementModifier);

		Modifier cubridPreparedStatementModifier = new CubridPreparedStatementModifier(classPool);
		registry.put(cubridPreparedStatementModifier.getTargetClass(), cubridPreparedStatementModifier);

		Modifier cubridResultSetModifier = new CubridResultSetModifier(classPool);
		registry.put(cubridResultSetModifier.getTargetClass(), cubridResultSetModifier);

		Modifier cubridUStatementModifier = new CubridUStatementModifier(classPool);
		registry.put(cubridStatementModifier.getTargetClass(), cubridUStatementModifier);
	}

	private void addDbcpDriver() {
		Map<String, Modifier> registry = this.registry;
		// TODO cubrid의 경우도 connection에 대한 impl이 없음. 확인필요.
		Modifier dbcpBasicDataSourceModifier = new DBCPBasicDataSourceModifier(classPool);
		registry.put(dbcpBasicDataSourceModifier.getTargetClass(), dbcpBasicDataSourceModifier);

		Modifier dbcpPoolModifier = new DBCPPoolModifier(classPool);
		registry.put(dbcpPoolModifier.getTargetClass(), dbcpPoolModifier);
	}
}
