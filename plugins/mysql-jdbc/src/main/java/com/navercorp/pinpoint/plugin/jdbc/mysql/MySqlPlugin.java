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
package com.navercorp.pinpoint.plugin.jdbc.mysql;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.PreparedStatementBindingMethodFilter;

/**
 * @author Jongho Moon
 *
 */
public class MySqlPlugin implements ProfilerPlugin, MySqlConstants {

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        MySqlConfig config = new MySqlConfig(context.getConfig());
        
        addConnectionTransformer(context, config);
        addDriverTransformer(context);
        addStatementTransformer(context);
        addPreparedStatementTransformer(context, config);
        
        // From MySQL driver 5.1.x, backward compatibility is broken.
        // Driver returns not com.mysql.jdbc.Connection but com.mysql.jdbc.JDBC4Connection which extends com.mysql.jdbc.ConnectionImpl from 5.1.x
        addJDBC4PreparedStatementTransformer(context);
    }
    
    private void addConnectionTransformer(ProfilerPluginSetupContext setupContext, final MySqlConfig config) {
        PinpointClassFileTransformer transformer = new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                
                if (!target.isInterceptable()) {
                    return null;
                }
                
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                InterceptorGroup group = instrumentContext.getInterceptorGroup(GROUP_NAME);

                target.addInterceptor("com.navercorp.pinpoint.plugin.jdbc.mysql.interceptor.MySQLConnectionCreateInterceptor");
                target.addInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor", group);
                target.addInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor", group);
                target.addInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor", group);
                
                if (config.isProfileSetAutoCommit()) {
                    target.addInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor", group);
                }
                
                if (config.isProfileCommit()) {
                    target.addInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor", group);
                }
                
                if (config.isProfileRollback()) {
                    target.addInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor", group);
                }
                
                return target.toBytecode();
            }
        };
        
        setupContext.addClassFileTransformer("com.mysql.jdbc.Connection", transformer);
        setupContext.addClassFileTransformer("com.mysql.jdbc.ConnectionImpl", transformer);
    }
    
    private void addDriverTransformer(ProfilerPluginSetupContext setupContext) {
        setupContext.addClassFileTransformer("com.mysql.jdbc.NonRegisteringDriver", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                InterceptorGroup group = instrumentContext.getInterceptorGroup(GROUP_NAME);
                
                target.addInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptor", group, ExecutionPolicy.ALWAYS, new MySqlJdbcUrlParser(), false);
                
                return target.toBytecode();
            }
        });
    }
    
    private void addPreparedStatementTransformer(ProfilerPluginSetupContext setupContext, final MySqlConfig config) {
        setupContext.addClassFileTransformer("com.mysql.jdbc.PreparedStatement", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor", "new java.util.HashMap()");
                
                int maxBindValueSize = config.getMaxSqlBindValueSize();
                InterceptorGroup group = instrumentContext.getInterceptorGroup(GROUP_NAME);
                
                target.addInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor", group, maxBindValueSize);
                target.addInterceptor(PreparedStatementBindingMethodFilter.excludes("setRowId", "setNClob", "setSQLXML"), "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", group, ExecutionPolicy.BOUNDARY);
                
                return target.toBytecode();
            }
        });
    }

    private void addJDBC4PreparedStatementTransformer(ProfilerPluginSetupContext setupContext) {
        setupContext.addClassFileTransformer("com.mysql.jdbc.JDBC4PreparedStatement", new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                InterceptorGroup group = instrumentContext.getInterceptorGroup(GROUP_NAME);
                
                target.addInterceptor(PreparedStatementBindingMethodFilter.includes("setRowId", "setNClob", "setSQLXML"), "com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", group, ExecutionPolicy.BOUNDARY);
                
                return target.toBytecode();
            }
        });
    }

    
    private void addStatementTransformer(ProfilerPluginSetupContext setupContext) {
        PinpointClassFileTransformer transformer = new PinpointClassFileTransformer() {
            
            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                
                if (!target.isInterceptable()) {
                    return null;
                }
                
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                
                InterceptorGroup group = instrumentContext.getInterceptorGroup(GROUP_NAME);

                target.addInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor", group);
                target.addInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor", group);
                
                return target.toBytecode();
            }
        };
        
        setupContext.addClassFileTransformer("com.mysql.jdbc.Statement", transformer);
        setupContext.addClassFileTransformer("com.mysql.jdbc.StatementImpl", transformer);
    }
}
