package com.profiler;

import static com.profiler.config.TomcatProfilerConfig.TOMCAT_LIB_PATH;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

import javassist.ClassPool;
import javassist.NotFoundException;

import com.profiler.config.TomcatProfilerConfig;
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
	protected String agentArgString = "";
	protected Instrumentation instrumentation;
	ClassPool classPool ;
	public static void premain(String agentArgs, Instrumentation inst) {
		new TomcatProfiler(agentArgs, inst);
	}

	public TomcatProfiler(String agentArgs, Instrumentation inst) {
		agentArgString = agentArgs;
		instrumentation = inst;
		instrumentation.addTransformer(this);
		classPool= ClassPool.getDefault();
		try {
			classPool.appendClassPath(TOMCAT_LIB_PATH+"/lib/servlet-api.jar");
			classPool.appendClassPath(TOMCAT_LIB_PATH+"/lib/catalina.jar");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void debug(String className) {
//		System.out.println(className);
//		if(className.startsWith("java/sql")) {
//			System.out.println(className);
//		}
//		System.out.print(".");		
	}
	@Override
	public byte[] transform(ClassLoader classLoader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classFileBuffer) throws IllegalClassFormatException {
		debug(className);
		if(className.startsWith("org/apache/catalina")) {
			String javassistClassName = className.replace('/', '.');
			if(javassistClassName.equals("org.apache.catalina.core.StandardHostValve")) {
				//Add code to monitor Request and Response
				byte[] result=EntryPointStandardHostValveModifier.modify(classPool,classLoader,javassistClassName,classFileBuffer);
				if(result!=null) return result;
			} else if(javassistClassName.equals("org.apache.catalina.core.StandardService")) {
				//Add code to monitor Tomcat start and stop
				byte[] result=TomcatStandardServiceModifier.modify(classPool,classLoader,javassistClassName,classFileBuffer);
				if(result!=null) return result;
			} else if(javassistClassName.equals("org.apache.catalina.connector.Connector")) {
				//Add code to set Tomcat's port numbers
				byte[] result=TomcatConnectorModifier.modify(classPool,classLoader,javassistClassName,classFileBuffer);
				if(result!=null) return result;
			}
		}
		//#### If JDBC_PROFILE is true, SQL data will be collected
		if(TomcatProfilerConfig.JDBC_PROFILE) {
				if(className.startsWith("com/mysql/jdbc")) {
				// MySQL !!!!!!!!!!
				String javassistClassName = className.replace('/', '.');
				if(javassistClassName.equals("com.mysql.jdbc.ConnectionImpl")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=MySQLConnectionImplModifier.modify(classPool,classLoader,javassistClassName,classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("com.mysql.jdbc.StatementImpl")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=MySQLStatementModifier.modify(classPool,classLoader,javassistClassName,classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("com.mysql.jdbc.PreparedStatement")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=MySQLPreparedStatementModifier.modify(classPool,classLoader,javassistClassName,classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("com.mysql.jdbc.ResultSetImpl")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=MySQLResultSetModifier.modify(classPool,classLoader,javassistClassName,classFileBuffer);
					if(result!=null) return result;
				}
				
			} else if(className.startsWith("net/sourceforge/jtds/jdbc")) {
				// MSSQL !!!!!!!!!!
				String javassistClassName = className.replace('/', '.');
				if(javassistClassName.equals("net.sourceforge.jtds.jdbc.ConnectionJDBC2")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=MSSQLConnectionModifier.modify(classPool,classLoader,javassistClassName,classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("net.sourceforge.jtds.jdbc.JtdsStatement")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=MSSQLStatementModifier.modify(classPool,classLoader,javassistClassName,classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("net.sourceforge.jtds.jdbc.JtdsPreparedStatement")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=MSSQLPreparedStatementModifier.modify(classPool,classLoader,javassistClassName,classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("net.sourceforge.jtds.jdbc.JtdsResultSet")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=MSSQLResultSetModifier.modify(classPool,classLoader,javassistClassName,classFileBuffer);
					if(result!=null) return result;
				} 
				
			} else if(className.startsWith("org/apache/commons/dbcp")) {
				// DBCP !!!!!!!!!!
				String javassistClassName = className.replace('/', '.');
				if(javassistClassName.equals("org.apache.commons.dbcp.BasicDataSource")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=DBCPBasicDataSourceModifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("org.apache.commons.dbcp.PoolingDataSource$PoolGuardConnectionWrapper")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=DBCPPoolModifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if(result!=null) return result;
				} 
	
			} else if(className.startsWith("cubrid/jdbc")) {
				// CUBRID !!!!!!!!!!
				String javassistClassName = className.replace('/', '.');
				/*if(!javassistClassName.equals("cubrid.jdbc.driver.CUBRIDResultSet") &&
						!javassistClassName.startsWith("cubrid.jdbc.driver.ConnectionProperties")) {
					byte[] result=AbstractModifier.addBeforeAfterLogics(classPool, javassistClassName);
					if(result!=null) return result;
				}*/
				
				if(javassistClassName.equals("cubrid.jdbc.driver.CUBRIDStatement")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=CubridStatementModifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("cubrid.jdbc.driver.CUBRIDPreparedStatement")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=CubridPreparedStatementModifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("cubrid.jdbc.driver.CUBRIDResultSet")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=CubridResultSetModifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("cubrid.jdbc.jci.UStatement")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=CubridUStatementModifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if(result!=null) return result;
				}
			} else if(className.startsWith("oracle/jdbc")) {
				String javassistClassName = className.replace('/', '.');
				if(javassistClassName.equals("oracle.jdbc.driver.OraclePreparedStatement")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=OraclePreparedStatementModifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("oracle.jdbc.driver.OracleStatement")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=OracleStatementModifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if(result!=null) return result;
				} else if(javassistClassName.equals("oracle.jdbc.driver.OracleResultSetImpl")) {
					checkLibrary(javassistClassName,classLoader);
					byte[] result=OracleResultSetModifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
					if(result!=null) return result;
				}
			}
		}
//		else if(className.startsWith("java/sql")) {
//			String javassistClassName = className.replace('/', '.');
//			System.out.println("***** Changing "+javassistClassName);
//			byte[] result=AbstractModifier.addBeforeAfterLogics(classPool, javassistClassName);
//			if(result!=null) return result;
//		}
			
		return null;
	}

	private void checkLibrary(String javassistClassName,ClassLoader classLoader) {
		try {
			classPool.get(javassistClassName);
		} catch(NotFoundException nfe) {
//			cnfe.printStackTrace();
			loadClassLoaderLibraries(classLoader);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void loadClassLoaderLibraries(ClassLoader classLoader) {
		if(classLoader instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = (URLClassLoader)classLoader;
			URL[] urlList=urlClassLoader.getURLs();
			for(URL tempURL:urlList) {
				String filePath=tempURL.getFile();
				try {
					classPool.appendClassPath(filePath);
//					log("Loaded "+filePath+" library.");
				} catch(Exception e) {
					
				}
			}
		}
	}
	@SuppressWarnings("unused")
	private static void log(String message) {
		System.out.println("%%%%% "+message);
	}
}
