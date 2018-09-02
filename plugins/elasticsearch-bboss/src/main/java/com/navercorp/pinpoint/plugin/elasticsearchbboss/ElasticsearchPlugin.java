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

package com.navercorp.pinpoint.plugin.elasticsearchbboss;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.common.trace.*;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchPlugin implements ProfilerPlugin, TransformTemplateAware {
	public static final AnnotationKey ARGS_ANNOTATION_KEY = AnnotationKeyFactory.of(971, "es.args", AnnotationKeyProperty.VIEW_IN_RECORD_SET);

	public static final ServiceType ELASTICSEARCH = ServiceTypeFactory.of(1971, "ElasticsearchBBoss", RECORD_STATISTICS);

	public static final ServiceType ELASTICSEARCH_EVENT = ServiceTypeFactory.of(1972, "ElasticsearchBBossEvent");
	public static final AnnotationKey ARGS_URL_ANNOTATION_KEY = AnnotationKeyFactory.of(972, "es.url", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final AnnotationKey ARGS_DSL_ANNOTATION_KEY = AnnotationKeyFactory.of(973, "es.dsl", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final AnnotationKey ARGS_ACTION_ANNOTATION_KEY = AnnotationKeyFactory.of(974, "es.action", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final AnnotationKey ARGS_RESPONSEHANDLE_ANNOTATION_KEY = AnnotationKeyFactory.of(975, "es.responseHandle", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
	public static final ServiceType ELASTICSEARCH_EXECUTOR = ServiceTypeFactory.of(9977, "ElasticsearchBBossExecutor");
	private static final String ELASTICSEARCH_SCOPE = "ElasticsearchBBoss_SCOPE";
	private static final String ELASTICSEARCH_EXECUTOR_SCOPE = "ElasticsearchBBossExecutor_SCOPE";
	private static final List<ElasticsearchInterceptorClassInfo> clazzInterceptors = new ArrayList<ElasticsearchInterceptorClassInfo>();

	static {
		init();
	}
	static void init(){
		ElasticsearchInterceptorClassInfo interceptorClassInfo = null;
		List<ElasticsearchMethodInfo> methodInfos = null;
		ElasticsearchMethodInfo methodInfo = null;
		interceptorClassInfo = new ElasticsearchInterceptorClassInfo();
		interceptorClassInfo.setInterceptorClass("org.frameworkset.elasticsearch.client.ConfigRestClientUtil");
		methodInfo = new ElasticsearchMethodInfo();
		methodInfo.setFilterType(1);
		methodInfo.setName("*");
		methodInfo.setPattern(true);
		interceptorClassInfo.setAllAccept(methodInfo);

		methodInfos = null;
		methodInfos = new ArrayList<ElasticsearchMethodInfo>();
		methodInfo = new ElasticsearchMethodInfo();
		methodInfo.setFilterType(1);
		methodInfo.setName("*");
		methodInfo.setPattern(true);
		methodInfos.add(methodInfo);
		methodInfo = new ElasticsearchMethodInfo();
		methodInfo.setFilterType(0);
		methodInfo.setName("discover");
		methodInfo.setPattern(false);
		methodInfos.add(methodInfo);
		interceptorClassInfo.setInterceptorMehtods(methodInfos);
		interceptorClassInfo.setMethodFilter(new ElasticsearchCustomMethodFilter(null, interceptorClassInfo));
		clazzInterceptors.add(interceptorClassInfo);
		interceptorClassInfo = new ElasticsearchInterceptorClassInfo();
		interceptorClassInfo.setInterceptorClass("org.frameworkset.elasticsearch.client.RestClientUtil");
		methodInfo = new ElasticsearchMethodInfo();
		methodInfo.setFilterType(1);
		methodInfo.setName("*");
		methodInfo.setPattern(true);
		interceptorClassInfo.setAllAccept(methodInfo);

		methodInfos = null;
		methodInfos = new ArrayList<ElasticsearchMethodInfo>();
		methodInfo = new ElasticsearchMethodInfo();
		methodInfo.setFilterType(1);
		methodInfo.setName("*");
		methodInfo.setPattern(true);
		methodInfos.add(methodInfo);
		methodInfo = new ElasticsearchMethodInfo();
		methodInfo.setFilterType(0);
		methodInfo.setName("discover");
		methodInfo.setPattern(false);
		methodInfos.add(methodInfo);
		interceptorClassInfo.setInterceptorMehtods(methodInfos);
		interceptorClassInfo.setMethodFilter(new ElasticsearchCustomMethodFilter(null, interceptorClassInfo));
		clazzInterceptors.add(interceptorClassInfo);
	}

	public static List<ElasticsearchInterceptorClassInfo> getClazzInterceptors(){
		return clazzInterceptors;
	}

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private TransformTemplate transformTemplate;
	private RestSeachExecutorMethodFilter restSeachExecutorMethodFilter = new RestSeachExecutorMethodFilter();

	@Override
	public void setup(ProfilerPluginSetupContext context) {
		if (context == null) {
			return;
		}

		ElasticsearchPluginConfig elasticsearchPluginConfig = new ElasticsearchPluginConfig(context.getConfig());
		if (logger.isInfoEnabled()) {
			logger.info("ElasticsearchPlugin config:{}", elasticsearchPluginConfig);
		}

		if (!elasticsearchPluginConfig.isEnabled()) {
			return;
		}
		this.addApplicationTypeDetector(context);
		addElasticsearchInterceptors();
		addElasticsearchExecutorInterceptors();
	}

	/**
	 * Pinpoint profiler agent uses this detector to find out the service type of current application.
	 */
	private void addApplicationTypeDetector(ProfilerPluginSetupContext context) {
		context.addApplicationTypeDetector(new ElasticsearchProviderDetector(clazzInterceptors));
	}

	//  implementations
	private void addElasticsearchInterceptors() {
		for (final ElasticsearchInterceptorClassInfo interceptorClassInfo : clazzInterceptors) {
			transformTemplate.transform(interceptorClassInfo.getInterceptorClass(), new TransformCallback() {

				@Override
				public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
											String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
											byte[] classfileBuffer) throws InstrumentException {

					final InstrumentClass target = instrumentor.getInstrumentClass(loader, interceptorClassInfo.getInterceptorClass(), classfileBuffer);

					final List<InstrumentMethod> methodsToTrace = target.getDeclaredMethods(interceptorClassInfo.getMethodFilter());
					for (InstrumentMethod methodToTrace : methodsToTrace) {
						String operationInterceptor = "com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor.ElasticsearchOperationInterceptor";
						methodToTrace.addScopedInterceptor(operationInterceptor, ELASTICSEARCH_SCOPE, ExecutionPolicy.BOUNDARY);
					}
					return target.toBytecode();
				}
			});

		}
	}

	//  implementations
	private void addElasticsearchExecutorInterceptors() {

		transformTemplate.transform("org.frameworkset.elasticsearch.client.RestSeachExecutor", new TransformCallback() {

			@Override
			public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
										String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
										byte[] classfileBuffer) throws InstrumentException {

				final InstrumentClass target = instrumentor.getInstrumentClass(loader, "org.frameworkset.elasticsearch.client.RestSeachExecutor", classfileBuffer);

				final List<InstrumentMethod> methodsToTrace = target.getDeclaredMethods(restSeachExecutorMethodFilter);
				String operationInterceptor = "com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor.ElasticsearchExecutorOperationInterceptor";
				//logger.info(operationInterceptor+" methodsToTrace",methodsToTrace);
				for (InstrumentMethod methodToTrace : methodsToTrace) {

					methodToTrace.addScopedInterceptor(operationInterceptor, ELASTICSEARCH_EXECUTOR_SCOPE, ExecutionPolicy.ALWAYS);
//                    methodToTrace.addInterceptor(operationInterceptor);
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
