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

package com.navercorp.pinpoint.bootstrap.java9.lambda;

import com.navercorp.pinpoint.bootstrap.instrument.lambda.LambdaBytecodeHandler;
import jdk.internal.misc.Unsafe;

import java.util.Objects;


/**
 * @author Woonduk Kang(emeroad)
 */
public class UnsafeDelegatorJava9 {
    private static final Unsafe UNSAFE = Unsafe.getUnsafe();
    private static LambdaBytecodeHandler handler;

    public static boolean register(LambdaBytecodeHandler handler) {
        Objects.requireNonNull(handler, "handler");

        final LambdaBytecodeHandler localCopy = UnsafeDelegatorJava9.handler;
        if (localCopy == null) {
            UnsafeDelegatorJava9.handler = handler;
            return true;
        } else {
            System.err.println("LambdaBytecodeHandler already registered");
            return false;
        }
    }

    public static Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches) {
        Objects.requireNonNull(hostClass, "hostClass");
        Objects.requireNonNull(data, "data");

        if (hostClass.isArray() || hostClass.isPrimitive()) {
            throw new IllegalArgumentException();
        }

        final LambdaBytecodeHandler localCopy = UnsafeDelegatorJava9.handler;
        if (localCopy != null) {
            data = localCopy.handleLambdaBytecode(hostClass, data, cpPatches);
        }

        return UNSAFE.defineAnonymousClass(hostClass, data, cpPatches);
    }

}
