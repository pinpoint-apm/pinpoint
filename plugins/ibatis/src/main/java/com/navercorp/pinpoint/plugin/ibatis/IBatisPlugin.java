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

import static com.navercorp.pinpoint.common.util.VarArgs.va;

import java.security.ProtectionDomain;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author HyunGil Jeong
 */
public class IBatisPlugin implements ProfilerPlugin, TransformTemplateAware {

    public static final ServiceType IBATIS = ServiceTypeFactory.of(5500, "IBATIS");
    public static final ServiceType IBATIS_SPRING = ServiceTypeFactory.of(5501, "IBATIS_SPRING", "IBATIS");

    private static final String IBATIS_SCOPE = "IBATIS_SCOPE";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        IBatisPluginConfig iBatisPluginConfig = new IBatisPluginConfig(context.getConfig());
        if (logger.isInfoEnabled()) {
            logger.info("IBatisPlugin config:{}", iBatisPluginConfig);
        }
        if (iBatisPluginConfig.isIBatisEnabled()) {
            addInterceptorsForSqlMapExecutors();
            addInterceptorsForSqlMapClientTemplate();
        }
    }

    // SqlMapClient / SqlMapSession
    private void addInterceptorsForSqlMapExecutors() {
        final ServiceType serviceType = IBATIS;
        final String[] sqlMapExecutorImplClasses = { "com.ibatis.sqlmap.engine.impl.SqlMapClientImpl",
                "com.ibatis.sqlmap.engine.impl.SqlMapSessionImpl" };
        addInterceptorsForClasses(serviceType, sqlMapExecutorImplClasses);
    }

    // SqlMapClientTemplate
    private void addInterceptorsForSqlMapClientTemplate() {
        final ServiceType serviceType = IBATIS_SPRING;
        final String[] sqlMapClientTemplateClasses = { "org.springframework.orm.ibatis.SqlMapClientTemplate" };
        addInterceptorsForClasses(serviceType, sqlMapClientTemplateClasses);
    }

    private void addInterceptorsForClasses(ServiceType serviceType, String... targetClassNames) {

        final MethodFilter methodFilter = MethodFilters.name("insert", "delete", "update", "queryForList",
                "queryForMap", "queryForObject", "queryForPaginatedList");
        for (String targetClassName : targetClassNames) {
            addInterceptorsForClass(targetClassName, serviceType, methodFilter);
        }
    }

    private void addInterceptorsForClass(final String targetClassName,
            final ServiceType serviceType, final MethodFilter methodFilter) {

        transformTemplate.transform(targetClassName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
                                        String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                                        byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                final List<InstrumentMethod> methodsToTrace = target.getDeclaredMethods(methodFilter);
                for (InstrumentMethod methodToTrace : methodsToTrace) {
                    String sqlMapOperationInterceptor = "com.navercorp.pinpoint.plugin.ibatis.interceptor.SqlMapOperationInterceptor";
                    methodToTrace.addScopedInterceptor(sqlMapOperationInterceptor, va(serviceType), IBATIS_SCOPE, ExecutionPolicy.BOUNDARY
                    );
                }

                return target.toBytecode();
            }

        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
