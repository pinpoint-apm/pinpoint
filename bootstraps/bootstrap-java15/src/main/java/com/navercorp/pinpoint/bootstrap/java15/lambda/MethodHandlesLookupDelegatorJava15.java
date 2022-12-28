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

import java.lang.invoke.MethodHandles;
import java.util.Objects;

public class MethodHandlesLookupDelegatorJava15 {

    private static LambdaBytecodeHandler handler;

    public static boolean register(LambdaBytecodeHandler handler) {
        Objects.requireNonNull(handler, "handler");

        final LambdaBytecodeHandler localCopy = MethodHandlesLookupDelegatorJava15.handler;
        if (localCopy == null) {
            MethodHandlesLookupDelegatorJava15.handler = handler;
            return true;
        } else {
            System.err.println("LambdaBytecodeHandler already registered");
            return false;
        }
    }

    public static MethodHandles.Lookup defineHiddenClass(MethodHandles.Lookup lookup, byte[] bytes, boolean initialize, MethodHandles.Lookup.ClassOption... options) throws IllegalAccessException {
        final LambdaBytecodeHandler localCopy = MethodHandlesLookupDelegatorJava15.handler;
        if (localCopy != null) {
            byte[] data = localCopy.handleLambdaBytecode(lookup.lookupClass(), bytes, null);
            return lookup.defineHiddenClass(data, initialize, options);
        }
        return lookup.defineHiddenClass(bytes, initialize, options);
    }
}
