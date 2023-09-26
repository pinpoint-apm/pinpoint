/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.server.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

class CallerUtilsTest {
    private final Logger logger = LogManager.getLogger(getClass());

    @Test
    void getCallerMethodName() {
        String methodName = CallerUtils.getCallerMethodName();
        Assertions.assertEquals("getCallerMethodName", methodName);
    }

    @Test
    void getCallerMethodName_inner() {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return CallerUtils.getCallerMethodName();
            }
        };
        logger.debug("getCallerMethodName_inner: {}", callable);
//        System.out.println(callable);
    }

    @Test
    void getCallerMethodName_lambda() {
        Callable<String> callable = () -> CallerUtils.getCallerMethodName();
        logger.debug("getCallerMethodName_lambda: {}", callable);
//        System.out.println(callable);
    }

    @Test
    void getCallerMethodName_methodRef() {
        Callable<String> callable = CallerUtils::getCallerMethodName;;
        logger.debug("getCallerMethodName_methodRef: {}", callable);
//        System.out.println(callable);
    }

}