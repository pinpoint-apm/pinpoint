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

package com.navercorp.pinpoint.test.javasssit.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class TestObject {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private int callA;
    private boolean isthrow = false;
    private int returnCode = 1;

    public void setIsthrow(boolean isthrow) {
        this.isthrow = isthrow;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public int callA() {
        logger.info("callA");
        int i = callA++;
        if (isthrow) {
            throw new RuntimeException("ddd");
        }
        if(returnCode == 1) {
            return 1;
        } if (returnCode == 10){
            return  -1;
        }
        return 0;
    }

    public static void before() {

    }
    public static void after() {
    }
    public static void callCatch() {

    }

    public String hello(String a) {
        return "a";
    }

}
