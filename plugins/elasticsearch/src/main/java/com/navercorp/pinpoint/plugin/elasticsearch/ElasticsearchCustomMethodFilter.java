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


import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;

import java.util.List;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchCustomMethodFilter implements MethodFilter {
	private List<ElasticsearchMethodInfo> interceptorMehtods ;
	private ElasticsearchInterceptorClassInfo interceptorClassInfo;
	public ElasticsearchCustomMethodFilter(int[] rejectModifiers, ElasticsearchInterceptorClassInfo interceptorClassInfo) {
		this.interceptorClassInfo = interceptorClassInfo;
		if(interceptorClassInfo != null)
			this.interceptorMehtods = interceptorClassInfo.getInterceptorMehtods();

	}

	@Override
	public boolean accept(InstrumentMethod method) {
		if (interceptorMehtods == null || interceptorMehtods.size() == 0) {
			return ACCEPT;
		}
		for (ElasticsearchMethodInfo methodInfo : interceptorMehtods) {
			if(methodInfo.isPattern()){
				if(methodInfo.getName().equals("*")){
					continue;
				}
				else if (method.getName().startsWith(methodInfo.getName())) {
					if(methodInfo.getFilterType() == ElasticsearchMethodInfo.FILTER_ACCEPT_TYPE) {
						return ACCEPT;
					}
					else{
						return REJECT;
					}
				}
			}
			else {
				if (methodInfo.getName().equals(method.getName())) {
					if(methodInfo.getFilterType() == ElasticsearchMethodInfo.FILTER_ACCEPT_TYPE) {
						return ACCEPT;
					}
					else {
						return REJECT;
					}
				}
			}
		}
		if(this.interceptorClassInfo.getAllAccept() != null){
			return ACCEPT;
		}
		else {
			return REJECT;
		}
	}

}
