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
package com.navercorp.pinpoint.plugin.jdbc.jtds;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jongho Moon
 *
 */
public class JtdsPlugin implements ProfilerPlugin, TransformTemplateAware {

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        JtdsConfig config = new JtdsConfig(context.getConfig());
        
        addConnectionTransformer(config);
        addDriverTransformer();
        addPreparedStatementTransformer(config);
        addStatementTransformer();
    }

    
    private void addConnectionTransformer(final JtdsConfig config) {
        TransformCallback transformer = new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.ConnectionCloseInterceptor", JtdsConstants.GROUP_JTDS);
                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementCreateInterceptor", JtdsConstants.GROUP_JTDS);
                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementCreateInterceptor", JtdsConstants.GROUP_JTDS);
                
                if (config.isProfileSetAutoCommit()) {
                    target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionSetAutoCommitInterceptor", JtdsConstants.GROUP_JTDS);
                }
                
                if (config.isProfileCommit()) {
                    target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionCommitInterceptor", JtdsConstants.GROUP_JTDS);
                }
                
                if (config.isProfileRollback()) {
                    target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.TransactionRollbackInterceptor", JtdsConstants.GROUP_JTDS);
                }
                
                return target.toBytecode();
            }
        };
        
        transformTemplate.transform("net.sourceforge.jtds.jdbc.ConnectionJDBC2", transformer);
        transformTemplate.transform("net.sourceforge.jtds.jdbc.JtdsConnection", transformer);
    }
    
    private void addDriverTransformer() {
        transformTemplate.transform("net.sourceforge.jtds.jdbc.Driver", new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);

                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.DriverConnectInterceptor", va(new JtdsJdbcUrlParser()), JtdsConstants.GROUP_JTDS, ExecutionPolicy.ALWAYS);
                
                return target.toBytecode();
            }
        });
    }
    
    private void addPreparedStatementTransformer(final JtdsConfig config) {
        transformTemplate.transform("net.sourceforge.jtds.jdbc.JtdsPreparedStatement", new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.ParsingResultAccessor");
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.BindValueAccessor");
                
                int maxBindValueSize = config.getMaxSqlBindValueSize();

                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementExecuteQueryInterceptor", va(maxBindValueSize), JtdsConstants.GROUP_JTDS);
                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.PreparedStatementBindVariableInterceptor", JtdsConstants.GROUP_JTDS);
                
                return target.toBytecode();
            }
        });
    }
    
    private void addStatementTransformer() {
        transformTemplate.transform("net.sourceforge.jtds.jdbc.JtdsStatement", new TransformCallback() {
            
            @Override
            public byte[] doInTransform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                
                target.addField("com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor");

                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteQueryInterceptor", JtdsConstants.GROUP_JTDS);
                target.addGroupedInterceptor("com.navercorp.pinpoint.bootstrap.plugin.jdbc.interceptor.StatementExecuteUpdateInterceptor", JtdsConstants.GROUP_JTDS);
                
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
