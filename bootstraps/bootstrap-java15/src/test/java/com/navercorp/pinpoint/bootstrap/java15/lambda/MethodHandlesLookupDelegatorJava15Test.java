/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.java15.lambda;


import com.navercorp.pinpoint.bootstrap.instrument.lambda.LambdaBytecodeHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MethodHandlesLookupDelegatorJava15Test {

    @Test
    public void defineHiddenClass() throws Exception {
        // handler is null
        Assertions.assertThrows(NullPointerException.class, () ->
                MethodHandlesLookupDelegatorJava15.register(null));

        // register
        LambdaBytecodeHandler handlerMock = mock(LambdaBytecodeHandler.class);
        Assertions.assertTrue(MethodHandlesLookupDelegatorJava15.register(handlerMock));

        // already registered
        Assertions.assertFalse(MethodHandlesLookupDelegatorJava15.register(handlerMock));
    }
}