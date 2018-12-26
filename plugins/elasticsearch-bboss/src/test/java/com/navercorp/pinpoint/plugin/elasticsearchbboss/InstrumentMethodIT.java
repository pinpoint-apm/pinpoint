package com.navercorp.pinpoint.plugin.elasticsearchbboss;
/**
 * Copyright 2008 biaoping.yin
 * <p>
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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/12/26 14:23
 * @author biaoping.yin
 * @version 1.0
 */
public class InstrumentMethodIT implements InstrumentMethod {
	private String name;
	public InstrumentMethodIT(String name){
		this.name = name;
	}
	@Override
	public String getName() {
		return name;
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
}
