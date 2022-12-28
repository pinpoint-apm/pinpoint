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

package com.navercorp.pinpoint.profiler.instrument.lambda;

import java.util.Arrays;
import java.util.List;

public class LambdaClassJava16 implements LambdaClass {

    public static final String DELEGATE_CLASS = "com/navercorp/pinpoint/bootstrap/java16/lambda/MethodHandlesLookupDelegatorJava16";

    private final List<MethodInsn> methodInsnList;

    public LambdaClassJava16() {
        this.methodInsnList = Arrays.asList(
                new MethodInsn("generateInnerClass", "java/lang/invoke/MethodHandles$Lookup", "defineHiddenClass", DELEGATE_CLASS, "defineHiddenClass", "(Ljava/lang/invoke/MethodHandles$Lookup;[BZ[Ljava/lang/invoke/MethodHandles$Lookup$ClassOption;)Ljava/lang/invoke/MethodHandles$Lookup;"),
                new MethodInsn("generateInnerClass", "java/lang/invoke/MethodHandles$Lookup", "defineHiddenClassWithClassData", DELEGATE_CLASS, "defineHiddenClassWithClassData", "(Ljava/lang/invoke/MethodHandles$Lookup;[BLjava/lang/Object;Z[Ljava/lang/invoke/MethodHandles$Lookup$ClassOption;)Ljava/lang/invoke/MethodHandles$Lookup;")
        );
    }

    @Override
    public List<MethodInsn> getMethodInsnList() {
        return methodInsnList;
    }

    @Override
    public String toString() {
        return "LambdaClassJava16{" +
                "methodInsnList=" + methodInsnList +
                '}';
    }
}
