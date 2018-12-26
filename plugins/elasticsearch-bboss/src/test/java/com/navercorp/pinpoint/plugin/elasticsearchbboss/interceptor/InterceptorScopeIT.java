package com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor;
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

import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/12/26 14:36
 * @author biaoping.yin
 * @version 1.0
 */
public class InterceptorScopeIT implements InterceptorScope {
	private String name;
	public InterceptorScopeIT(String name){
		this.name = name;
	}
	@Override
	public String getName() {
		return name;
	}

	@Override
	public InterceptorScopeInvocation getCurrentInvocation() {
		return null;
	}
}
