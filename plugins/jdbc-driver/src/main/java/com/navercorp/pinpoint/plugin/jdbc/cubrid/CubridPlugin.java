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
package com.navercorp.pinpoint.plugin.jdbc.cubrid;

import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectRecipe;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.plugin.jdbc.common.JdbcDriverConstants;

/**
 * @author Jongho Moon
 *
 */
public class CubridPlugin implements ProfilerPlugin, JdbcDriverConstants, CubridConstants {

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        CubridConfig config = new CubridConfig(context.getConfig());
        
        if (!config.isProfileCubrid()) {
            return;
        }

        addCUBRIDConnectionTransformer(context, config);
        addCUBRIDDriverTransformer(context);
        addCUBRIDPreparedStatementTransformer(context, config);
        addCUBRIDStatementTransformer(context);
    }

    
    private void addCUBRIDConnectionTransformer(ProfilerPluginSetupContext context, CubridConfig config) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("cubrid.jdbc.driver.CUBRIDConnection");
        
        builder.injectMetadata(DATABASE_INFO);
        
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdbc.common.interceptor.ConnectionCloseInterceptor").group(GROUP_CUBRID);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdbc.common.interceptor.StatementCreateInterceptor").group(GROUP_CUBRID);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdbc.common.interceptor.PreparedStatementCreateInterceptor").group(GROUP_CUBRID);
        
        if (config.isProfileSetAutoCommit()) {
            builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdbc.common.interceptor.TransactionSetAutoCommitInterceptor").group(GROUP_CUBRID);
        }
        
        if (config.isProfileCommit()) {
            builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdbc.common.interceptor.TransactionCommitInterceptor").group(GROUP_CUBRID);
        }
        
        if (config.isProfileRollback()) {
            builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdbc.common.interceptor.TransactionRollbackInterceptor").group(GROUP_CUBRID);
        }
        
        context.addClassFileTransformer(builder.build());
    }
    
    private void addCUBRIDDriverTransformer(ProfilerPluginSetupContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("cubrid.jdbc.driver.CUBRIDDriver");
        
        ObjectRecipe jdbcUrlParser = ObjectRecipe.byConstructor("com.navercorp.pinpoint.plugin.jdbc.cubrid.CubridJdbcUrlParser");
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdbc.common.interceptor.DriverConnectInterceptor", jdbcUrlParser).group(GROUP_CUBRID, ExecutionPolicy.ALWAYS);
                
        context.addClassFileTransformer(builder.build());
    }
    
    private void addCUBRIDPreparedStatementTransformer(ProfilerPluginSetupContext context, CubridConfig config) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("cubrid.jdbc.driver.CUBRIDPreparedStatement");

        builder.injectMetadata(DATABASE_INFO);
        builder.injectMetadata(PARSING_RESULT);
        builder.injectMetadata(BIND_VALUE, BIND_VALUE_INITIAL_VALUE_TYPE);
        
        int maxBindValueSize = config.getMaxSqlBindValueSize();
        
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdbc.common.interceptor.PreparedStatementExecuteQueryInterceptor", maxBindValueSize).group(GROUP_CUBRID);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdbc.common.interceptor.PreparedStatementBindVariableInterceptor").group(GROUP_CUBRID);
        
        context.addClassFileTransformer(builder.build());
    }
    
    private void addCUBRIDStatementTransformer(ProfilerPluginSetupContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("cubrid.jdbc.driver.CUBRIDStatement");
        
        builder.injectMetadata(DATABASE_INFO);
        
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdbc.common.interceptor.StatementExecuteQueryInterceptor").group(GROUP_CUBRID);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.jdbc.common.interceptor.StatementExecuteUpdateInterceptor").group(GROUP_CUBRID);
        
        context.addClassFileTransformer(builder.build());
    }
}
