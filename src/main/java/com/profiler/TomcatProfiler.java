package com.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

import com.profiler.modifier.DefaultModifierRegistry;
import com.profiler.modifier.Modifier;
import com.profiler.modifier.ModifierRegistry;
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

    private final ModifierRegistry modifierRepository;
    private TomcatProfilerConfig tomcatProfilerConfig;

    public static void premain(String agentArgs, Instrumentation inst) {
        TomcatProfilerConfig tomcatProfilerConfig = TomcatProfilerConfig.readConfigFile();
        new TomcatProfiler(agentArgs, inst, tomcatProfilerConfig);
    }

    public TomcatProfiler(String agentArgs, Instrumentation inst, TomcatProfilerConfig tomcatProfilerConfig) {
        this.agentArgString = agentArgs;
        this.instrumentation = inst;
        this.instrumentation.addTransformer(this);
        this.classPool = createClassPool();
        this.modifierRepository = createModifierRegistry(tomcatProfilerConfig);
        this.tomcatProfilerConfig = tomcatProfilerConfig;


    }

    private ModifierRegistry createModifierRegistry(TomcatProfilerConfig tomcatProfilerConfig) {
        DefaultModifierRegistry modifierRepository = new DefaultModifierRegistry();
        modifierRepository.addTomcatModifier();
        if (tomcatProfilerConfig.enableJdbcProfile()) {
            modifierRepository.addJdbcModifier();
        }
        return modifierRepository;
    }

    private ClassPool createClassPool() {
        ClassPool classPool = new ClassPool(null);
        classPool.appendSystemPath();

        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome != null) {
            logger.info("CATALINA_HOME=%s", catalinaHome);
            appendClassPath(classPool, catalinaHome + "/lib/servlet-api.jar");
            appendClassPath(classPool, catalinaHome + "/lib/catalina.jar");
        }
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
        Modifier findModifier = this.modifierRepository.findModifier(className);
        if (findModifier == null) {
            return null;

        }
        String javassistClassName = className.replace('/', '.');
        return findModifier.modify(classPool, classLoader, javassistClassName, classFileBuffer);
    }

}
