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
 */

package com.navercorp.pinpoint.profiler.instrument.lambda;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MethodInsnTest {

    @Test
    public void constructor() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            new MethodInsn(null, "targetClassName", "targetMethodName", "delegateClassName", "delegateMethodName", "delegateDescriptor");
        });

        assertThrows(NullPointerException.class, () -> {
            new MethodInsn("methodName", null, "targetMethodName", "delegateClassName", "delegateMethodName", "delegateDescriptor");
        });

        assertThrows(NullPointerException.class, () -> {
            new MethodInsn("methodName", "targetClassName", null, "delegateClassName", "delegateMethodName", "delegateDescriptor");
        });

        assertThrows(NullPointerException.class, () -> {
            new MethodInsn("methodName", "targetClassName", "targetMethodName", null, "delegateMethodName", "delegateDescriptor");
        });

        assertThrows(NullPointerException.class, () -> {
            new MethodInsn("methodName", "targetClassName", "targetMethodName", "delegateClassName", null, "delegateDescriptor");
        });

        MethodInsn methodInsn = new MethodInsn("methodName", "targetClassName", "targetMethodName", "delegateClassName", "delegateMethodName", null);
        assertNull(methodInsn.getDelegateDescriptor());
    }
}