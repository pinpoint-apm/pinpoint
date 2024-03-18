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

package com.navercorp.pinpoint.profiler.instrument.lambda;

import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LambdaClassJava9 implements LambdaClass {

    public static final String DELEGATE_CLASS = "com/navercorp/pinpoint/bootstrap/java9/lambda/UnsafeDelegatorJava9";

    private final MethodInsn methodInsn;

    public LambdaClassJava9() {
        this.methodInsn = new MethodInsn("spinInnerClass",
                "jdk/internal/misc/Unsafe",
                "defineAnonymousClass",
                DELEGATE_CLASS,
                "defineAnonymousClass",
                null);
    }

    @Override
    public List<MethodInsn> getMethodInsnList() {
        return Collections.singletonList(methodInsn);
    }
}
