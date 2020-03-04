/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.plugin.ibatis.interceptor.SqlMapOperationInterceptor;

/**
 * @author HyunGil Jeong
 */
public class IBatisPlugin implements ProfilerPlugin, TransformTemplateAware {

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
        final ServiceType serviceType = IBatisConstants.IBATIS;
        final String[] sqlMapExecutorImplClasses = { "com.ibatis.sqlmap.engine.impl.SqlMapClientImpl",
                "com.ibatis.sqlmap.engine.impl.SqlMapSessionImpl" };
        addInterceptorsForClasses(serviceType, sqlMapExecutorImplClasses);
    }

    // SqlMapClientTemplate
    private void addInterceptorsForSqlMapClientTemplate() {
        final ServiceType serviceType = IBatisConstants.IBATIS_SPRING;
        final String[] sqlMapClientTemplateClasses = { "org.springframework.orm.ibatis.SqlMapClientTemplate" };
        addInterceptorsForClasses(serviceType, sqlMapClientTemplateClasses);
    }

    private void addInterceptorsForClasses(ServiceType serviceType, String... targetClassNames) {

        final MethodFilter methodFilter = MethodFilters.name("insert", "delete", "update", "queryForList",
                "queryForMap", "queryForObject", "queryForPaginatedList");
        for (String targetClassName : targetClassNames) {
            addInterceptorsForClass(targetClassName, serviceType);
        }
    }

    private void addInterceptorsForClass(final String targetClassName, final ServiceType serviceType) {
        Class<? extends TransformCallback> transformer = getTransformer(serviceType);
        transformTemplate.transform(targetClassName, transformer);
    }

    private Class<? extends TransformCallback> getTransformer(ServiceType serviceType) {
        if (serviceType == IBatisConstants.IBATIS) {
            return IBatisApiTransform.class;
        }
        if (serviceType == IBatisConstants.IBATIS_SPRING) {
            return SpringApiTransform.class;
        }
        throw new IllegalStateException("Unknown ServiceType:" + serviceType);
    }

    public static class SpringApiTransform extends ApiTransform {
        public SpringApiTransform() {
            super(IBatisConstants.IBATIS_SPRING);
        }
    }

    public static class IBatisApiTransform extends ApiTransform {
        public IBatisApiTransform() {
            super(IBatisConstants.IBATIS);
        }
    }


    public static class ApiTransform implements TransformCallback {
        private final ServiceType serviceType;

        public ApiTransform(ServiceType serviceType) {
            this.serviceType = Assert.requireNonNull(serviceType, "serviceType");
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
                String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final MethodFilter methodFilter = MethodFilters.name("insert", "delete", "update", "queryForList",
                    "queryForMap", "queryForObject", "queryForPaginatedList");
            final List<InstrumentMethod> methodsToTrace = target.getDeclaredMethods(methodFilter);
            for (InstrumentMethod methodToTrace : methodsToTrace) {
                methodToTrace.addScopedInterceptor(SqlMapOperationInterceptor.class, va(serviceType), IBATIS_SCOPE, ExecutionPolicy.BOUNDARY
                );
            }

            return target.toBytecode();
        }

    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
