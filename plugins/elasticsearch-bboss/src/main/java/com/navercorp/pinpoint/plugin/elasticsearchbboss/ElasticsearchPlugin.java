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
import com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor.*;

import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchPlugin implements ProfilerPlugin, TransformTemplateAware {




	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
	private TransformTemplate transformTemplate;
	private static RestSeachExecutorMethodFilter restSeachExecutorMethodFilter = new RestSeachExecutorMethodFilter();
	static final ElasticsearchCustomMethodFilter elasticsearchCustomMethodFilter = new ElasticsearchCustomMethodFilter();
	@Override
	public void setup(ProfilerPluginSetupContext context) {
		if (context == null) {
			return;
		}

		final ElasticsearchPluginConfig elasticsearchPluginConfig = new ElasticsearchPluginConfig(context.getConfig());
		if (logger.isInfoEnabled()) {
			logger.info("ElasticsearchPlugin config:{}", elasticsearchPluginConfig);
		}

		if (!elasticsearchPluginConfig.isEnabled()) {
			return;
		}
		addElasticsearchInterceptors();
		addElasticsearchExecutorInterceptors();
		this.addParallelElasticsearchInterceptors();
	}


	//  implementations
	private void addElasticsearchInterceptors() {
		transformTemplate.transform("org.frameworkset.elasticsearch.client.ConfigRestClientUtil",ConfigRestClientTransformCallback.class);
		transformTemplate.transform("org.frameworkset.elasticsearch.client.RestClientUtil",RestClientTransformCallback.class);

	}

	//  implementations
	private void addElasticsearchExecutorInterceptors() {

		transformTemplate.transform("org.frameworkset.elasticsearch.client.RestSearchExecutor", RestSearchExecutorTransformCallback.class);


	}

	//  implementations
	private void addParallelElasticsearchInterceptors() {

		transformTemplate.transform("org.frameworkset.elasticsearch.SliceRunTask", ParallelRunTaskTransformCallback.class);
		transformTemplate.transform("org.frameworkset.elasticsearch.scroll.thread.ScrollTask", ParallelRunTaskTransformCallback.class);
	}

	@Override
	public void setTransformTemplate(TransformTemplate transformTemplate) {
		this.transformTemplate = transformTemplate;
	}

	public static class RestSearchExecutorTransformCallback implements TransformCallback{
		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
									String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
									byte[] classfileBuffer) throws InstrumentException {

			final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
			target.addField(ClusterVersionAccessor.class);
			final List<InstrumentMethod> methodsToTrace = target.getDeclaredMethods(restSeachExecutorMethodFilter);
			for (InstrumentMethod methodToTrace : methodsToTrace) {

				methodToTrace.addScopedInterceptor(ElasticsearchExecutorOperationInterceptor.class, ElasticsearchConstants.ELASTICSEARCH_EXECUTOR_SCOPE, ExecutionPolicy.ALWAYS);
			}

			return target.toBytecode();
		}

	}

	public static class ParallelRunTaskTransformCallback implements TransformCallback{
		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
									String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
									byte[] classfileBuffer) throws InstrumentException {

			final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
			InterceptorScope scope = instrumentor.getInterceptorScope(ElasticsearchConstants.ELASTICSEARCH_Parallel_SCOPE);

			target.addField(AsyncContextAccessor.class);

			InstrumentMethod constructor = target.getConstructor("org.frameworkset.elasticsearch.client.RestClientUtil","int",
					"java.lang.String","java.lang.String",  "java.lang.String",  "java.lang.Class",
					"org.frameworkset.elasticsearch.scroll.ParallelSliceScrollResult","org.frameworkset.elasticsearch.serial.SerialContext" );
			if(constructor != null)
				constructor.addScopedInterceptor(ParallelWorkerConstructorInterceptor.class, scope, ExecutionPolicy.INTERNAL);

			constructor = target.getConstructor("org.frameworkset.elasticsearch.scroll.ScrollHandler","org.frameworkset.elasticsearch.entity.ESDatas",
					"org.frameworkset.elasticsearch.scroll.HandlerInfo");
			if(constructor != null)
				constructor.addScopedInterceptor(ParallelWorkerConstructorInterceptor.class, scope, ExecutionPolicy.INTERNAL);

			constructor = target.getConstructor("org.frameworkset.elasticsearch.scroll.ScrollHandler","org.frameworkset.elasticsearch.entity.ESDatas",
					"org.frameworkset.elasticsearch.scroll.HandlerInfo","org.frameworkset.elasticsearch.scroll.SliceScrollResultInf");
			if(constructor != null)
				constructor.addScopedInterceptor(ParallelWorkerConstructorInterceptor.class, scope, ExecutionPolicy.INTERNAL);
			InstrumentMethod run = target.getDeclaredMethod("run");
			run.addInterceptor(ParallelWorkerRunInterceptor.class);

			return target.toBytecode();
		}
	}

	public static abstract class BaseClientTransformCallback implements TransformCallback{
		protected byte[] toBytecode(InstrumentClass target)  throws InstrumentException{
			List<InstrumentMethod> methodsToTrace = target.getDeclaredMethods(elasticsearchCustomMethodFilter);
			for (InstrumentMethod methodToTrace : methodsToTrace) {
				methodToTrace.addScopedInterceptor(ElasticsearchOperationInterceptor.class, ElasticsearchConstants.ELASTICSEARCH_SCOPE, ExecutionPolicy.BOUNDARY);
			}

			InstrumentMethod runSliceTaskMethod = target.getDeclaredMethod("runSliceTask",
													"int","java.lang.String","java.lang.String","java.lang.String","java.lang.Class"
													,"org.frameworkset.elasticsearch.scroll.ParallelSliceScrollResult"
													,"java.util.concurrent.ExecutorService"
													,"java.util.List"
													,"org.frameworkset.elasticsearch.serial.SerialContext"	);

			if(runSliceTaskMethod != null) {
				runSliceTaskMethod.addScopedInterceptor(ElasticsearchOperationAsyncInitiatorInterceptor.class, ElasticsearchConstants.ELASTICSEARCH_Parallel_SCOPE);
			}

			InstrumentMethod runScrollTask = target.getDeclaredMethod("runScrollTask",
					"java.util.List","org.frameworkset.elasticsearch.scroll.ScrollHandler",
					"org.frameworkset.elasticsearch.entity.ESDatas","org.frameworkset.elasticsearch.scroll.HandlerInfo"
					,"java.util.concurrent.ExecutorService" 	);

			if(runScrollTask != null) {
				runScrollTask.addScopedInterceptor(ElasticsearchOperationAsyncInitiatorInterceptor.class, ElasticsearchConstants.ELASTICSEARCH_Parallel_SCOPE);
			}

			InstrumentMethod runSliceScrollTask = target.getDeclaredMethod("runSliceScrollTask",
					"java.util.List","org.frameworkset.elasticsearch.scroll.ScrollHandler",
					"org.frameworkset.elasticsearch.entity.ESDatas","org.frameworkset.elasticsearch.scroll.HandlerInfo"
					,"org.frameworkset.elasticsearch.scroll.SliceScrollResultInf"
					,"java.util.concurrent.ExecutorService" );

			if(runSliceScrollTask != null) {
				runSliceScrollTask.addScopedInterceptor(ElasticsearchOperationAsyncInitiatorInterceptor.class, ElasticsearchConstants.ELASTICSEARCH_Parallel_SCOPE);
			}

			return target.toBytecode();
		}
	}
	public static class RestClientTransformCallback extends BaseClientTransformCallback{
		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
									String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
									byte[] classfileBuffer) throws InstrumentException {

			final InstrumentClass target = instrumentor.getInstrumentClass(loader, "org.frameworkset.elasticsearch.client.RestClientUtil", classfileBuffer);
			return toBytecode(target);
		}
	}

	public static class ConfigRestClientTransformCallback extends BaseClientTransformCallback{
		@Override
		public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader,
									String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
									byte[] classfileBuffer) throws InstrumentException {

			final InstrumentClass target = instrumentor.getInstrumentClass(loader, "org.frameworkset.elasticsearch.client.ConfigRestClientUtil", classfileBuffer);
			return toBytecode(target);
		}
	}
}
