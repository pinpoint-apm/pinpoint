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
package com.navercorp.pinpoint.plugin.elasticsearchbboss;


import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;

import java.util.List;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchInterceptorClassInfo {
	private List<ElasticsearchMethodInfo> interceptorMehtods ;
	private MethodFilter methodFilter;
	private String interceptorClass;
    private ElasticsearchMethodInfo allAccept ;
    private ElasticsearchMethodInfo allReject ;

	public List<ElasticsearchMethodInfo> getInterceptorMehtods() {
		return interceptorMehtods;
	}

	public void setInterceptorMehtods(List<ElasticsearchMethodInfo> interceptorMehtods) {
		this.interceptorMehtods = interceptorMehtods;
	}

	public String getInterceptorClass() {
		return interceptorClass;
	}

	public void setInterceptorClass(String interceptorClass) {
		this.interceptorClass = interceptorClass;
	}

	public MethodFilter getMethodFilter() {
		return methodFilter;
	}

	public void setMethodFilter(MethodFilter methodFilter) {
		this.methodFilter = methodFilter;
	}

	public ElasticsearchMethodInfo getAllAccept() {
		return allAccept;
	}

	public void setAllAccept(ElasticsearchMethodInfo allAccept) {
		this.allAccept = allAccept;
	}

	public ElasticsearchMethodInfo getAllReject() {
		return allReject;
	}

	public void setAllReject(ElasticsearchMethodInfo allReject) {
		this.allReject = allReject;
	}
}
