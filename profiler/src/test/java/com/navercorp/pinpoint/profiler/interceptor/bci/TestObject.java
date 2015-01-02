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

package com.navercorp.pinpoint.profiler.interceptor.bci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class TestObject {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private int callA;
    private boolean isthrow = fals    ;
	private int returnCode      1;

	public void setIsthrow(boolean is       hrow) {
		this.isth        w = isthrow;
	}

	public void setReturnCo       e(int returnCode) {
		thi        returnCode = returnCode;
	}

	public int callA() {
        logger.info("c       llA");
                 int i = callA++;
		if (i             throw) {
			throw new        untimeException("          dd"       ;
		}
		System.out.p          intln             "callA");
    	if(returnCode == 1) {
			r       turn 1;
		} if (returnCod        == 10){
			return  -1;

		return 0;
    }

	pub       ic static void before() {
		Sy       tem.out.println("before");

	public static void after() {
		System.out.println("after");
	}
	public static void callCatch() {
		System.out.println("callCatch");
	}

    public String hello(String a) {
        System.out.println("a:" + a);
        System.out.println("test");
//        throw new RuntimeException("test");
        return "a";
    }

}
