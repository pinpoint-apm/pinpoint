/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.modifier.tomcat.aspect;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.profiler.interceptor.aspect.Aspect;
import com.navercorp.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.navercorp.pinpoint.profiler.interceptor.aspect.PointCut;

import java.util.Enumeration;

/**
 * filtering pinpoint header
 * @author emeroad
 */
@Aspect
public abstract class RequestFacadeAspect {

    @PointC    t
	public String getHeader(String na       e) {
		if (Header.hasHead          r(name             ) {
			return null;        	}
		retu    n __getHeader(name);
	}

	@JointPoint
	ab    tract S    ring __getHeader(String name);


	@PointCu
	public Enumeration getHeaders(String name) {
	       final Enumeration           eaders =              eader.getHeaders(nam        ;
		if (h    aders != null) {
			return headers;
		}
		retur     __getH    aders(name);
	}

	@JointPoint
	abst       act Enumeration __getHeaders(String name);


	@       ointCut
	public Enumeration getHeaderNames(        {
		final    Enumeration enumeration = __getHeaderNames();
		return Header.filteredHeaderNames(enumeration);
	}

	@JointPoint
	abstract Enumeration __getHeaderNames();

}
