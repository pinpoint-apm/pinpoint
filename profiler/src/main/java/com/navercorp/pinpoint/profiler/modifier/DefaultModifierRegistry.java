/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.modifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.ClassNameMatcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.MultiClassNameMatcher;
import com.navercorp.pinpoint.profiler.modifier.connector.asynchttpclient.AsyncHttpClientModifier;
import com.navercorp.pinpoint.profiler.modifier.db.cubrid.CubridConnectionModifier;
import com.navercorp.pinpoint.profiler.modifier.db.cubrid.CubridDriverModifier;
import com.navercorp.pinpoint.profiler.modifier.db.cubrid.CubridPreparedStatementModifier;
import com.navercorp.pinpoint.profiler.modifier.db.cubrid.CubridResultSetModifier;
import com.navercorp.pinpoint.profiler.modifier.db.cubrid.CubridStatementModifier;
import com.navercorp.pinpoint.profiler.modifier.db.dbcp.DBCPBasicDataSourceModifier;
import com.navercorp.pinpoint.profiler.modifier.db.dbcp.DBCPPoolGuardConnectionWrapperModifier;
import com.navercorp.pinpoint.profiler.modifier.db.jtds.Jdbc2ConnectionModifier;
import com.navercorp.pinpoint.profiler.modifier.db.jtds.Jdbc4_1ConnectionModifier;
import com.navercorp.pinpoint.profiler.modifier.db.jtds.JtdsDriverModifier;
import com.navercorp.pinpoint.profiler.modifier.db.jtds.JtdsPreparedStatementModifier;
import com.navercorp.pinpoint.profiler.modifier.db.jtds.JtdsResultSetModifier;
import com.navercorp.pinpoint.profiler.modifier.db.jtds.JtdsStatementModifier;
import com.navercorp.pinpoint.profiler.modifier.db.mysql.MySQLConnectionImplModifier;
import com.navercorp.pinpoint.profiler.modifier.db.mysql.MySQLConnectionModifier;
import com.navercorp.pinpoint.profiler.modifier.db.mysql.MySQLNonRegisteringDriverModifier;
import com.navercorp.pinpoint.profiler.modifier.db.mysql.MySQLPreparedStatementJDBC4Modifier;
import com.navercorp.pinpoint.profiler.modifier.db.mysql.MySQLPreparedStatementModifier;
import com.navercorp.pinpoint.profiler.modifier.db.mysql.MySQLStatementModifier;
import com.navercorp.pinpoint.profiler.modifier.db.oracle.OracleDriverModifier;
import com.navercorp.pinpoint.profiler.modifier.db.oracle.OraclePreparedStatementModifier;
import com.navercorp.pinpoint.profiler.modifier.db.oracle.OracleStatementModifier;
import com.navercorp.pinpoint.profiler.modifier.db.oracle.PhysicalConnectionModifier;
import com.navercorp.pinpoint.profiler.modifier.log.log4j.LoggingEventOfLog4jModifier;
import com.navercorp.pinpoint.profiler.modifier.log.logback.LoggingEventOfLogbackModifier;
import com.navercorp.pinpoint.profiler.modifier.method.MethodModifier;
import com.navercorp.pinpoint.profiler.modifier.servlet.SpringFrameworkServletModifier;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author emeroad
 * @author netspider
 * @author hyungil.jeong
 * @author Minwoo Jung
 * @author jaehong.kim
 */
public class DefaultModifierRegistry implements ModifierRegistry {

    // No concurrent issue because only one thread put entries to the map and get operations are started AFTER the map is completely build.
    // Set the map size big intentionally to keep hash collision low.
    private final Map<String, AbstractModifier> registry = new HashMap<String, AbstractModifier>(512);

    private final ByteCodeInstrumentor byteCodeInstrumentor;
    private final ProfilerConfig profilerConfig;
    private final Agent agent;

    public DefaultModifierRegistry(Agent agent, ByteCodeInstrumentor byteCodeInstrumentor) {
        this.agent = agent;
        this.byteCodeInstrumentor = byteCodeInstrumentor;
        this.profilerConfig = agent.getProfilerConfig();
    }

    @Override
    public AbstractModifier findModifier(String className) {
        return registry.get(className);
    }

    public void addModifier(AbstractModifier modifier) {
        final Matcher matcher = modifier.getMatcher();
        // TODO extract matcher process
        if (matcher instanceof ClassNameMatcher) {
            final ClassNameMatcher classNameMatcher = (ClassNameMatcher)matcher;
            String className = classNameMatcher.getClassName();
            addModifier0(modifier, className);
        } else if (matcher instanceof MultiClassNameMatcher) {
            final MultiClassNameMatcher classNameMatcher = (MultiClassNameMatcher)matcher;
            List<String> classNameList = classNameMatcher.getClassNames();
            for (String className : classNameList) {
                addModifier0(modifier, className);
            }
        } else {
            throw new IllegalArgumentException("unsupported matcher :" + matcher);
        }
    }

    private void addModifier0(AbstractModifier modifier, String className) {
        // check jvmClassName
        final String checkJvmClassName = JavaAssistUtils.javaNameToJvmName(className);
        AbstractModifier old = registry.put(checkJvmClassName, modifier);
        if (old != null) {
            throw new IllegalStateException("Modifier already exist. className:" + checkJvmClassName + " new:" + modifier.getClass() + " old:" + old.getClass());
        }
    }

    public void addMethodModifier() {
        MethodModifier methodModifier = new MethodModifier(byteCodeInstrumentor, agent);
        addModifier(methodModifier);
    }

    public void addConnectorModifier() {
        // ning async http client
        addModifier(new AsyncHttpClientModifier(byteCodeInstrumentor, agent));
    }

    public void addTomcatModifier() {
        SpringFrameworkServletModifier springServletModifier = new SpringFrameworkServletModifier(byteCodeInstrumentor, agent);
        addModifier(springServletModifier);
    }

    public void addJdbcModifier() {
        // TODO Can we check if JDBC driver exists here?

        if (!profilerConfig.isJdbcProfile()) {
            return;
        }

        if (profilerConfig.isJdbcProfileOracle()) {
            addOracleDriver();
        }

        if (profilerConfig.isJdbcProfileDbcp()) {
            addDbcpDriver();
        }
    }

    private void addOracleDriver() {
        AbstractModifier oracleDriverModifier = new OracleDriverModifier(byteCodeInstrumentor, agent);
        addModifier(oracleDriverModifier);

        // TODO Intercepting PhysicalConnection makes view ugly.
        // We'd better intercept top-level classes T4C, T2C and OCI each to makes view more readable.
        AbstractModifier oracleConnectionModifier = new PhysicalConnectionModifier(byteCodeInstrumentor, agent);
        addModifier(oracleConnectionModifier);

        AbstractModifier oraclePreparedStatementModifier = new OraclePreparedStatementModifier(byteCodeInstrumentor, agent);
        addModifier(oraclePreparedStatementModifier);

        AbstractModifier oracleStatementModifier = new OracleStatementModifier(byteCodeInstrumentor, agent);
        addModifier(oracleStatementModifier);
        //
        // Modifier oracleResultSetModifier = new OracleResultSetModifier(byteCodeInstrumentor, agent);
        // addModifier(oracleResultSetModifier);
    }

    private void addDbcpDriver() {

        // TODO Cubrid doesn't have connection impl too. Check it out.
        AbstractModifier dbcpBasicDataSourceModifier = new DBCPBasicDataSourceModifier(byteCodeInstrumentor, agent);
        addModifier(dbcpBasicDataSourceModifier);

        if (profilerConfig.isJdbcProfileDbcpConnectionClose()) {
            AbstractModifier dbcpPoolModifier = new DBCPPoolGuardConnectionWrapperModifier(byteCodeInstrumentor, agent);
            addModifier(dbcpPoolModifier);
        }
    }

    public void addLog4jModifier() {
        if (profilerConfig.isLog4jLoggingTransactionInfo()) {
            addModifier(new LoggingEventOfLog4jModifier(byteCodeInstrumentor, agent));
//            addModifier(new Nelo2AsyncAppenderModifier(byteCodeInstrumentor, agent));
//            addModifier(new NeloAppenderModifier(byteCodeInstrumentor, agent));
        }
    }

    public void addLogbackModifier() {
        if (profilerConfig.isLogbackLoggingTransactionInfo()) {
            addModifier(new LoggingEventOfLogbackModifier(byteCodeInstrumentor, agent));
        }
    }
}
