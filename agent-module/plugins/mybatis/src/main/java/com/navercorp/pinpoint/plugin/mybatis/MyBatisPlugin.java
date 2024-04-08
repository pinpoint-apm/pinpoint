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

package com.navercorp.pinpoint.plugin.mybatis;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.mybatis.interceptor.SqlSessionOperationInterceptor;

import java.security.ProtectionDomain;
import java.util.List;


/**
 * @author HyunGil Jeong
 */
public class MyBatisPlugin implements ProfilerPlugin, TransformTemplateAware {

    private static final String MYBATIS_SCOPE = "MYBATIS_SCOPE";

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {

        MyBatisPluginConfig config = new MyBatisPluginConfig(context.getConfig());
        if (!config .isMyBatisEnabled()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
         addInterceptorsForSqlSession();
    }

    // SqlSession implementations
    private void addInterceptorsForSqlSession() {

        final String[] sqlSessionImpls = { "org.apache.ibatis.session.defaults.DefaultSqlSession",
                "org.mybatis.spring.SqlSessionTemplate" };

        for (final String sqlSession : sqlSessionImpls) {
            transformTemplate.transform(sqlSession, SqlSessionTransform.class);

        }
    }

    public static class SqlSessionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
                String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
        byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final MethodFilter methodFilter = MethodFilters.name("selectOne", "selectList", "selectMap", "select",
                    "insert", "update", "delete");
            final List<InstrumentMethod> methodsToTrace = target.getDeclaredMethods(methodFilter);
            for (InstrumentMethod methodToTrace : methodsToTrace) {
                Class<? extends Interceptor> sqlSessionOperationInterceptor = SqlSessionOperationInterceptor.class;
                methodToTrace.addScopedInterceptor(sqlSessionOperationInterceptor, MYBATIS_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
