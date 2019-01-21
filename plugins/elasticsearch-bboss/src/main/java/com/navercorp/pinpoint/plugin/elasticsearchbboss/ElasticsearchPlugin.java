/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.elasticsearchbboss;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchPlugin implements ProfilerPlugin, TransformTemplateAware {


	public static String[] getClazzInterceptors(){
		return ElasticsearchConstants.clazzInterceptors;
	}

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private TransformTemplate transformTemplate;
	private RestSeachExecutorMethodFilter restSeachExecutorMethodFilter = new RestSeachExecutorMethodFilter();
	final ElasticsearchParallelMethodFilter elasticsearchParallelMethodFilter = new ElasticsearchParallelMethodFilter();

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
		addElasticsearchInterceptors();
		addElasticsearchExecutorInterceptors();
		this.addSliceElasticsearchInterceptors();
	}


	//  implementations
	private void addElasticsearchInterceptors() {
		final ElasticsearchCustomMethodFilter elasticsearchCustomMethodFilter = new ElasticsearchCustomMethodFilter();
		for (final String interceptorClass: ElasticsearchConstants.clazzInterceptors) {
			transformTemplate.transform(interceptorClass, new TransformCallback() {

				@Override
				public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
											String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
											byte[] classfileBuffer) throws InstrumentException {

					final InstrumentClass target = instrumentor.getInstrumentClass(loader, interceptorClass, classfileBuffer);

					final List<InstrumentMethod> methodsToTrace = target.getDeclaredMethods(elasticsearchCustomMethodFilter);
					for (InstrumentMethod methodToTrace : methodsToTrace) {
						String operationInterceptor = "com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor.ElasticsearchOperationInterceptor";
						methodToTrace.addScopedInterceptor(operationInterceptor, ElasticsearchConstants.ELASTICSEARCH_SCOPE, ExecutionPolicy.BOUNDARY);
					}
					final List<InstrumentMethod> sliceMethodsToTrace = target.getDeclaredMethods(elasticsearchParallelMethodFilter);
					for (InstrumentMethod methodToTrace : sliceMethodsToTrace) {
						methodToTrace.addScopedInterceptor("com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor.ElasticsearchOperationAsyncInitiatorInterceptor", ElasticsearchConstants.ELASTICSEARCH_SLICE_SCOPE);
					}
					return target.toBytecode();
				}
			});

		}
	}

	//  implementations
	private void addElasticsearchExecutorInterceptors() {

		transformTemplate.transform("org.frameworkset.elasticsearch.client.RestSearchExecutor", new TransformCallback() {

			@Override
			public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
										String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
										byte[] classfileBuffer) throws InstrumentException {

				final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

				final List<InstrumentMethod> methodsToTrace = target.getDeclaredMethods(restSeachExecutorMethodFilter);
				String operationInterceptor = "com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor.ElasticsearchExecutorOperationInterceptor";
				//logger.info(operationInterceptor+" methodsToTrace",methodsToTrace);
				for (InstrumentMethod methodToTrace : methodsToTrace) {

					methodToTrace.addScopedInterceptor(operationInterceptor, ElasticsearchConstants.ELASTICSEARCH_EXECUTOR_SCOPE, ExecutionPolicy.ALWAYS);
//                    methodToTrace.addInterceptor(operationInterceptor);
				}

				return target.toBytecode();
			}
		});


	}

	//  implementations
	private void addSliceElasticsearchInterceptors() {

		transformTemplate.transform("org.frameworkset.elasticsearch.SliceRunTask", new TransformCallback() {

			@Override
			public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
										String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
										byte[] classfileBuffer) throws InstrumentException {

				final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
				InterceptorScope scope = instrumentor.getInterceptorScope(ElasticsearchConstants.ELASTICSEARCH_SLICE_SCOPE);

				target.addField(AsyncContextAccessor.class.getName());

				InstrumentMethod constructor = target.getConstructor("org.frameworkset.elasticsearch.client.RestClientUtil","int",
																	"java.lang.String","java.lang.String",  "java.lang.String",  "java.lang.Class",
																	"org.frameworkset.elasticsearch.scroll.ParallelSliceScrollResult" );
				constructor.addScopedInterceptor("com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor.SliceWorkerConstructorInterceptor", scope, ExecutionPolicy.INTERNAL);

				InstrumentMethod run = target.getDeclaredMethod("run");
				run.addInterceptor("com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor.SliceWorkerRunInterceptor");

				return target.toBytecode();
			}
		});
	}

	@Override
	public void setTransformTemplate(TransformTemplate transformTemplate) {
		this.transformTemplate = transformTemplate;
	}
}
