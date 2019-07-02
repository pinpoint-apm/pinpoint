/*
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.spring.beans;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.spring.beans.interceptor.ClassPathDefinitionScannerDoScanInterceptor;
import com.navercorp.pinpoint.plugin.spring.beans.interceptor.CreateBeanInstanceInterceptor;
import com.navercorp.pinpoint.plugin.spring.beans.interceptor.PostProcessorInterceptor;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class SpringBeansPlugin implements ProfilerPlugin, TransformTemplateAware {

    public static final String ENABLE = "profiler.spring.beans";

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final boolean enable = context.getConfig().readBoolean(ENABLE, true);
        if (!enable) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }

        final SpringBeansConfig config = new SpringBeansConfig(context.getConfig());
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        if (logger.isInfoEnabled()) {
            logger.info("SpringBeans targets=" + config.getTargets());
        }

        if (config.hasTarget(SpringBeansTargetScope.COMPONENT_SCAN)) {
            // since spring-context 2.5
            addClassPathDefinitionScannerTransformer();
        }

        if (config.hasTarget(SpringBeansTargetScope.POST_PROCESSOR)) {
            addAbstractAutowireCapableBeanFactoryTransformer();
        }
    }

    private void addAbstractAutowireCapableBeanFactoryTransformer() {
        transformTemplate.transform("org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory", AbstractAutowireCapableBeanFactoryTransform.class);
    }

    public static class AbstractAutowireCapableBeanFactoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final ProfilerConfig config = instrumentor.getProfilerConfig();
            final boolean errorMark = SpringBeansConfig.getMarkError(config);
            final BeanMethodTransformer beanTransformer = new BeanMethodTransformer(errorMark);
            final ObjectFactory beanFilterFactory = ObjectFactory.byStaticFactory("com.navercorp.pinpoint.plugin.spring.beans.interceptor.TargetBeanFilter", "of", config);

            final InstrumentMethod createBeanInstance = target.getDeclaredMethod("createBeanInstance", "java.lang.String", "org.springframework.beans.factory.support.RootBeanDefinition", "java.lang.Object[]");
            createBeanInstance.addInterceptor(CreateBeanInstanceInterceptor.class, va(beanTransformer, beanFilterFactory));

            final InstrumentMethod postProcessor = target.getDeclaredMethod("applyBeanPostProcessorsBeforeInstantiation", "java.lang.Class", "java.lang.String");
            postProcessor.addInterceptor(PostProcessorInterceptor.class, va(beanTransformer, beanFilterFactory));

            return target.toBytecode();
        }
    }

    private void addClassPathDefinitionScannerTransformer() {

        transformTemplate.transform("org.springframework.context.annotation.ClassPathBeanDefinitionScanner", ClassPathBeanDefinitionScannerTransform.class);
    }

    public static class ClassPathBeanDefinitionScannerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            final ProfilerConfig config = instrumentor.getProfilerConfig();
            final boolean errorMark = SpringBeansConfig.getMarkError(config);
            final BeanMethodTransformer beanTransformer = new BeanMethodTransformer(errorMark);

            final ObjectFactory beanFilterFactory = ObjectFactory.byStaticFactory("com.navercorp.pinpoint.plugin.spring.beans.interceptor.TargetBeanFilter", "of", config);

            final InstrumentMethod method = target.getDeclaredMethod("doScan", "java.lang.String[]");
            method.addInterceptor(ClassPathDefinitionScannerDoScanInterceptor.class, va(loader, beanTransformer, beanFilterFactory));

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}