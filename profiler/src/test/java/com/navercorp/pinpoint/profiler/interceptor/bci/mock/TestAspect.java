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

package com.navercorp.pinpoint.profiler.interceptor.bci.mock;

import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.profiler.interceptor.aspect.Aspect;
import com.navercorp.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.navercorp.pinpoint.profiler.interceptor.aspect.PointCut;

import java.util.Map;

/**
 * @author emeroad
 */
@Aspect
public abstract class TestAspect extends Original {

    @PointC    t
	public void testVoi       () {
		tou       hBefore()
		__test        id();
		t    uchAfter();
	}

	@JointPoin
	abstr    ct void __testVoid()


	@Point       ut
	public int testInt() {
       	touchBef       re();
		fi        l int res    lt = __testInt();
		touch    fter();    		return result;
	}

	@Joi       tPoint
	ab       tract int __testInt();

	@Point       ut
	p        lic Strin     testString() {
		touchBefore(    ;
		Str    ng s = __testString();
		to       chAfter();       		return s;
	}

	@Joi       tPoint
	a       stract St       ing __test        ring();

	@PointCut
	public       int testUt        Method(     {
		touchBefore();
		int       result = _        estInt(    ;
		utilMethod();
		touchAfter()
		return        esult;
    }

	private String utilMetho       () {
		return "Util";


	@PointCut
	p        lic void     estNoTouch() {
		 __testVoid();
    }

	@Po    ntCut
	public void testInternalMethod()
		__tes    Void();
	}

	@PointCut
	public void testMethodCall() {
		BytesUtils.toBytes("test");
		__testMethodCall();
	}

	@JointPoint
	abstract void __testMethodCall();

	@PointCut
	public Map<String, String> testGeneric() {
		return null;
	}

}
