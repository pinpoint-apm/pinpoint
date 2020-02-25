/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.test.pinpoint.plugin.hystrix.repository;

import com.navercorp.pinpoint.plugin.hystrix.HystrixTestHelper;

/**
 * @author HyunGil Jeong
 */
public class HelloRepository {

    public String hello(String name) {
        System.out.println("name : " + name);
        return HystrixTestHelper.sayHello(name);
    }

    public String hello(String name, Exception exception) throws Exception {
        if (exception == null) {
            throw new NullPointerException("exception");
        }
        System.out.println("name : " + name + ", with exception : " + exception);
        throw exception;
    }

    public String hello(String name, long delayMs) {
        System.out.println("name : " + name + ", with delay : " + delayMs + "ms");
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw HystrixTestHelper.INTERRUPTED_EXCEPTION_DUE_TO_TIMEOUT;
        }
        return HystrixTestHelper.sayHello(name);
    }
}
