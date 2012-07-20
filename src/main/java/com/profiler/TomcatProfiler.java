package com.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

import com.profiler.modifier.Modifier;
import javassist.ClassPool;
import javassist.NotFoundException;

import com.profiler.config.TomcatProfilerConfig;
import com.profiler.logging.Logger;
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

public class TomcatProfiler implements ClassFileTransformer {

	private static final Logger logger = Logger.getLogger(TomcatProfiler.class);

	private String agentArgString = "";
	private Instrumentation instrumentation;
	private ClassPool classPool;



	public static void premain(String agentArgs, Instrumentation inst) {
		new TomcatProfiler(agentArgs, inst);
	}

	public TomcatProfiler(String agentArgs, Instrumentation inst) {
		this.agentArgString = agentArgs;
		this.instrumentation = inst;
		this.instrumentation.addTransformer(this);
        this.classPool = createClassPool();
	}

    private ClassPool createClassPool() {
        ClassPool classPool = new ClassPool(null);
        classPool.appendSystemPath();

        String catalinaHome = System.getProperty("catalina.home");
        logger.info("CATALINA_HOME=%s", catalinaHome);
        appendClassPath(classPool, catalinaHome + "/lib/servlet-api.jar");
        appendClassPath(classPool, catalinaHome + "/lib/catalina.jar");
        return classPool;
    }

    private void appendClassPath(ClassPool classPool, String pathName) {
        try {
            classPool.appendClassPath(pathName);
        } catch (NotFoundException e) {
            logger.error("lib not found. " + e.getMessage());
        }
    }

    @Override
	public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
		if (className.startsWith("org/apache/catalina")) {
			String javassistClassName = className.replace('/', '.');
			if (javassistClassName.equals("org.apache.catalina.core.StandardHostValve")) {
				// Add code to monitor Request and Response
                Modifier modifier = new EntryPointStandardHostValveModifier();
				byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
				if (result != null)
					return result;
			} else if (javassistClassName.equals("org.apache.catalina.core.StandardService")) {
				// Add code to monitor Tomcat start and stop
                Modifier modifier = new TomcatStandardServiceModifier();
				byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
				if (result != null)
					return result;
			} else if (javassistClassName.equals("org.apache.catalina.connector.Connector")) {
				// Add code to set Tomcat's port numbers
                TomcatConnectorModifier modifier = new TomcatConnectorModifier();
				byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
				if (result != null)
					return result;
			}
		}
		// #### If JDBC_PROFILE is true, SQL data will be collected
		if (TomcatProfilerConfig.JDBC_PROFILE) {
			if (className.startsWith("com/mysql/jdbc")) {
				// MySQL !!!!!!!!!!
				String javassistClassName = className.replace('/', '.');
				if (javassistClassName.equals("com.mysql.jdbc.ConnectionImpl")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new MySQLConnectionImplModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("com.mysql.jdbc.StatementImpl")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new MySQLStatementModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("com.mysql.jdbc.PreparedStatement")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new MySQLPreparedStatementModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("com.mysql.jdbc.ResultSetImpl")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new MySQLResultSetModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				}
			} else if (className.startsWith("net/sourceforge/jtds/jdbc")) {
				// MSSQL !!!!!!!!!!
				String javassistClassName = className.replace('/', '.');
				if (javassistClassName.equals("net.sourceforge.jtds.jdbc.ConnectionJDBC2")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new MSSQLConnectionModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("net.sourceforge.jtds.jdbc.JtdsStatement")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new MSSQLStatementModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("net.sourceforge.jtds.jdbc.JtdsPreparedStatement")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new MSSQLPreparedStatementModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("net.sourceforge.jtds.jdbc.JtdsResultSet")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new MSSQLResultSetModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				}
			} else if (className.startsWith("org/apache/commons/dbcp")) {
				// DBCP !!!!!!!!!!
				String javassistClassName = className.replace('/', '.');
				if (javassistClassName.equals("org.apache.commons.dbcp.BasicDataSource")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new DBCPBasicDataSourceModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new DBCPPoolModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				}
			} else if (className.startsWith("cubrid/jdbc")) {
				// CUBRID !!!!!!!!!!
				String javassistClassName = className.replace('/', '.');
				/*
				 * if(!javassistClassName.equals(
				 * "cubrid.jdbc.driver.CUBRIDResultSet") &&
				 * !javassistClassName.startsWith
				 * ("cubrid.jdbc.driver.ConnectionProperties")) { byte[]
				 * result=AbstractModifier.addBeforeAfterLogics(classPool,
				 * javassistClassName); if(result!=null) return result; }
				 */

				if (javassistClassName.equals("cubrid.jdbc.driver.CUBRIDStatement")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new CubridStatementModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("cubrid.jdbc.driver.CUBRIDPreparedStatement")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new CubridPreparedStatementModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("cubrid.jdbc.driver.CUBRIDResultSet")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new CubridResultSetModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("cubrid.jdbc.jci.UStatement")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new CubridUStatementModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				}
			} else if (className.startsWith("oracle/jdbc")) {
				String javassistClassName = className.replace('/', '.');
				if (javassistClassName.equals("oracle.jdbc.driver.OraclePreparedStatement")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new OraclePreparedStatementModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("oracle.jdbc.driver.OracleStatement")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new OracleStatementModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				} else if (javassistClassName.equals("oracle.jdbc.driver.OracleResultSetImpl")) {
					checkLibrary(javassistClassName, classLoader);
                    Modifier modifier = new OracleResultSetModifier();
					byte[] result = modifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if (result != null)
						return result;
				}
			}
		}

		return null;
	}

	private void checkLibrary(String javassistClassName, ClassLoader classLoader) {
		try {
			classPool.get(javassistClassName);
		} catch (NotFoundException nfe) {
			// cnfe.printStackTrace();
			loadClassLoaderLibraries(classLoader);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadClassLoaderLibraries(ClassLoader classLoader) {
		if (classLoader instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
			URL[] urlList = urlClassLoader.getURLs();
			for (URL tempURL : urlList) {
				String filePath = tempURL.getFile();
				try {
					classPool.appendClassPath(filePath);
					// log("Loaded "+filePath+" library.");
				} catch (Exception e) {

				}
			}
		}
	}
}
