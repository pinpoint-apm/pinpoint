/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.oracle;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jongho Moon
 *
 */
public class OraclePlugin implements ProfilerPlugin {

    private static final String CLASS_STATEMENT_WRAPPER = "oracle.jdbc.driver.OracleStatementWrapper";
    private static final String CLASS_STATEMENT = "oracle.jdbc.driver.OracleStatement";
    private static final String CLASS_PREPARED_STATEMENT_WRAPPER = "oracle.jdbc.driver.OraclePreparedStatementWrapper";
    private static final String CLASS_PREPARED_STATEMENT = "oracle.jdbc.driver.OraclePreparedStatement";

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        OracleConfig config = new OracleConfig(context.getConfig());
        
        addConnectionTransformer(context, config);
        addDriverTransformer(context);
        addPreparedStatementTransformer(context, config);
        addStatementTransformer(context);
    }

    
    private void addConnectionTransformer(ProfilerPluginSetupContext setupContext, final OracleConfig config) {
        setupContext.addClassFileTransformer("oracle.jdbc.driver.PhysicalConnection", new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");


                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor", OracleConstants.GROUP_ORACLE);
                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor", OracleConstants.GROUP_ORACLE);
                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor", OracleConstants.GROUP_ORACLE);
                
                if (config.isProfileSetAutoCommit()) {
                    target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor", OracleConstants.GROUP_ORACLE);
                }
                
                if (config.isProfileCommit()) {
                    target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor", OracleConstants.GROUP_ORACLE);
                }
                
                if (config.isProfileRollback()) {
                    target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor", OracleConstants.GROUP_ORACLE);
                }
                
                return target.toBytecode();
            }
        });
    }
    
    private void addDriverTransformer(ProfilerPluginSetupContext setupContext) {
        setupContext.addClassFileTransformer("oracle.jdbc.driver.OracleDriver", new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);

                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptor", va(new OracleJdbcUrlParser()), OracleConstants.GROUP_ORACLE, ExecutionPolicy.ALWAYS);
                
                return target.toBytecode();
            }
        });
    }
    
    private void addPreparedStatementTransformer(ProfilerPluginSetupContext setupContext, final OracleConfig config) {
        TransformCallback transformer = new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                if (className.equals(CLASS_PREPARED_STATEMENT)) {
                    if (instrumentContext.exist(loader, CLASS_PREPARED_STATEMENT_WRAPPER)) {
                        return null;
                    }
                }
                
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor", "new java.util.HashMap()");
                
                int maxBindValueSize = config.getMaxSqlBindValueSize();

                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor", va(maxBindValueSize), OracleConstants.GROUP_ORACLE);
                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", OracleConstants.GROUP_ORACLE);
                
                return target.toBytecode();
            }
        };
        
        setupContext.addClassFileTransformer(CLASS_PREPARED_STATEMENT, transformer);
        setupContext.addClassFileTransformer(CLASS_PREPARED_STATEMENT_WRAPPER, transformer);
    }
    
    private void addStatementTransformer(ProfilerPluginSetupContext setupContext) {
        TransformCallback transformer = new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                if (className.equals(CLASS_STATEMENT)) {
                    if (instrumentContext.exist(loader, CLASS_STATEMENT_WRAPPER)) {
                        return null;
                    }
                }
                
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                
                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor", OracleConstants.GROUP_ORACLE);
                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor", OracleConstants.GROUP_ORACLE);
                
                return target.toBytecode();
            }
        };
        
        setupContext.addClassFileTransformer(CLASS_STATEMENT, transformer);
        setupContext.addClassFileTransformer(CLASS_STATEMENT_WRAPPER, transformer);
    }
}
