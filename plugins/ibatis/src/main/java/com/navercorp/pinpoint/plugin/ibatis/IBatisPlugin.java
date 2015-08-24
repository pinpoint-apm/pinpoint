/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ibatis;

import static com.navercorp.pinpoint.common.trace.HistogramSchema.NORMAL_SCHEMA;

import java.security.ProtectionDomain;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginInstrumentContext;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author HyunGil Jeong
 */
public class IBatisPlugin implements ProfilerPlugin {

    public static final ServiceType IBATIS = ServiceType.of(5500, "IBATIS", NORMAL_SCHEMA);
    public static final ServiceType IBATIS_SPRING = ServiceType.of(5501, "IBATIS_SPRING", "IBATIS", NORMAL_SCHEMA);

    private static final String IBATIS_SCOPE = "IBATIS_SCOPE";

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        ProfilerConfig profilerConfig = context.getConfig();
        if (profilerConfig.isIBatisEnabled()) {
            addInterceptorsForSqlMapExecutors(context);
            addInterceptorsForSqlMapClientTemplate(context);
        }
    }

    // SqlMapClient / SqlMapSession
    private void addInterceptorsForSqlMapExecutors(ProfilerPluginSetupContext context) {
        final ServiceType serviceType = IBATIS;
        final String[] sqlMapExecutorImplClasses = { "com.ibatis.sqlmap.engine.impl.SqlMapClientImpl",
                "com.ibatis.sqlmap.engine.impl.SqlMapSessionImpl" };
        addInterceptorsForClasses(context, serviceType, sqlMapExecutorImplClasses);
    }

    // SqlMapClientTemplate
    private void addInterceptorsForSqlMapClientTemplate(ProfilerPluginSetupContext context) {
        final ServiceType serviceType = IBATIS_SPRING;
        final String[] sqlMapClientTemplateClasses = { "org.springframework.orm.ibatis.SqlMapClientTemplate" };
        addInterceptorsForClasses(context, serviceType, sqlMapClientTemplateClasses);
    }

    private void addInterceptorsForClasses(ProfilerPluginSetupContext context, ServiceType serviceType,
            String... targetClassNames) {
        final MethodFilter methodFilter = MethodFilters.name("insert", "delete", "update", "queryForList",
                "queryForMap", "queryForObject", "queryForPaginatedList");
        for (String targetClassName : targetClassNames) {
            addInterceptorsForClass(context, targetClassName, serviceType, methodFilter);
        }
    }

    private void addInterceptorsForClass(ProfilerPluginSetupContext context, final String targetClassName,
            final ServiceType serviceType, final MethodFilter methodFilter) {

        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(ProfilerPluginInstrumentContext instrumentContext, ClassLoader loader,
                    String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                    byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                final InterceptorGroup group = instrumentContext.getInterceptorGroup(IBATIS_SCOPE);

                final List<InstrumentMethod> methodsToTrace = target.getDeclaredMethods(methodFilter);
                for (InstrumentMethod methodToTrace : methodsToTrace) {
                    String sqlMapOperationInterceptor = "com.navercorp.pinpoint.plugin.ibatis.interceptor.SqlMapOperationInterceptor";
                    methodToTrace.addInterceptor(sqlMapOperationInterceptor, group, ExecutionPolicy.BOUNDARY,
                            serviceType);
                }

                return target.toBytecode();
            }

        });
    }
}
