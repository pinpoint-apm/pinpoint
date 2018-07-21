/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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
package com.navercorp.pinpoint.plugin.elasticsearch;


import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchCustomMethodFilterTest{

	@Test
	public void testAccept() {
		ElasticsearchCustomMethodFilter elasticsearchCustomMethodFilter = new ElasticsearchCustomMethodFilter(null,ElasticsearchPlugin.getClazzInterceptors().get(0));
		InstrumentMethod instrumentMethod = new InstrumentMethod(){

			@Override
			public String getName() {
				return null;
			}

			@Override
			public String[] getParameterTypes() {
				return new String[0];
			}

			@Override
			public String getReturnType() {
				return null;
			}

			@Override
			public int getModifiers() {
				return 0;
			}

			@Override
			public boolean isConstructor() {
				return false;
			}

			@Override
			public MethodDescriptor getDescriptor() {
				return null;
			}

			@Override
			public int addInterceptor(String interceptorClassName) throws InstrumentException {
				return 0;
			}

			@Override
			public int addInterceptor(String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
				return 0;
			}

			@Override
			public int addScopedInterceptor(String interceptorClassName, String scopeName) throws InstrumentException {
				return 0;
			}

			@Override
			public int addScopedInterceptor(String interceptorClassName, InterceptorScope scope) throws InstrumentException {
				return 0;
			}

			@Override
			public int addScopedInterceptor(String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
				return 0;
			}

			@Override
			public int addScopedInterceptor(String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
				return 0;
			}

			@Override
			public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName) throws InstrumentException {
				return 0;
			}

			@Override
			public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope) throws InstrumentException {
				return 0;
			}

			@Override
			public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
				return 0;
			}

			@Override
			public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
				return 0;
			}

			@Override
			public void addInterceptor(int interceptorId) throws InstrumentException {

			}
		};
		Assert.assertTrue(elasticsearchCustomMethodFilter.accept(instrumentMethod));
	}

}
