/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.grpc;

import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class StatusErrorsTest {
    private final Logger logger = LogManager.getLogger(getClass());

    @Test
    public void throwable() {
        StatusError statusError = StatusErrors.throwable(new RuntimeException("test"));
        assertEquals("test", statusError.getMessage());
        assertFalse(statusError.isSimpleError());
    }
}