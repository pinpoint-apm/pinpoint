/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpStatusCodeErrorsTest {
    @Test
    void isErrorCode() {
        // default 5xx
        HttpStatusCodeErrors defaultHttpStatusCodeErrors = new HttpStatusCodeErrors();
        assertTrue(defaultHttpStatusCodeErrors.isErrorCode(500));
        assertTrue(defaultHttpStatusCodeErrors.isErrorCode(501));
        assertFalse(defaultHttpStatusCodeErrors.isErrorCode(200));
        assertFalse(defaultHttpStatusCodeErrors.isErrorCode(999));
        assertFalse(defaultHttpStatusCodeErrors.isErrorCode(0));
        assertFalse(defaultHttpStatusCodeErrors.isErrorCode(-1));

        HttpStatusCodeErrors customHttpStatusCodeErrors = new HttpStatusCodeErrors(Arrays.asList("5xx", "401", "402"));
        assertTrue(customHttpStatusCodeErrors.isErrorCode(500));
        assertTrue(customHttpStatusCodeErrors.isErrorCode(501));
        assertTrue(customHttpStatusCodeErrors.isErrorCode(401));
        assertTrue(customHttpStatusCodeErrors.isErrorCode(402));

        assertFalse(customHttpStatusCodeErrors.isErrorCode(100));
        assertFalse(customHttpStatusCodeErrors.isErrorCode(200));
        assertFalse(customHttpStatusCodeErrors.isErrorCode(201));
        assertFalse(customHttpStatusCodeErrors.isErrorCode(300));
        assertFalse(customHttpStatusCodeErrors.isErrorCode(400));
        assertFalse(customHttpStatusCodeErrors.isErrorCode(404));
    }

    @Test
    void isHttpStatusCode() {
        HttpStatusCodeErrors httpStatusCodeErrors = new HttpStatusCodeErrors();
        assertTrue(httpStatusCodeErrors.isHttpStatusCode(200));
        assertTrue(httpStatusCodeErrors.isHttpStatusCode(300));
        assertTrue(httpStatusCodeErrors.isHttpStatusCode(500));

        assertFalse(httpStatusCodeErrors.isHttpStatusCode(0));
        assertFalse(httpStatusCodeErrors.isHttpStatusCode(600));
    }

    @Test
    void isHttpStatusCode_properties() {
        Properties properties = new Properties();
        properties.setProperty("profiler.http.status.code.errors", "400");

        HttpStatusCodeErrors httpStatusCodeErrors = HttpStatusCodeErrors.of(properties::getProperty);
        assertTrue(httpStatusCodeErrors.isErrorCode(400));


        assertFalse(httpStatusCodeErrors.isErrorCode(0));
        assertFalse(httpStatusCodeErrors.isErrorCode(100));
    }
}