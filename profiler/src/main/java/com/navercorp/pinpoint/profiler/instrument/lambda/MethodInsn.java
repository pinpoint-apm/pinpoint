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

import java.util.Objects;

public class MethodInsn {
    private final String methodName;
    private final String targetClassName;
    private final String targetMethodName;
    private final String delegateClassName;
    private final String delegateMethodName;
    private final String delegateDescriptor;

    public MethodInsn(String methodName, String targetClassName, String targetMethodName, String delegateClassName, String delegateMethodName, String delegateDescriptor) {
        this.methodName = Objects.requireNonNull(methodName);
        this.targetClassName = Objects.requireNonNull(targetClassName);
        this.targetMethodName = Objects.requireNonNull(targetMethodName);
        this.delegateClassName = Objects.requireNonNull(delegateClassName);
        this.delegateMethodName = Objects.requireNonNull(delegateMethodName);
        this.delegateDescriptor = delegateDescriptor;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getTargetClassName() {
        return targetClassName;
    }

    public String getTargetMethodName() {
        return targetMethodName;
    }

    public String getDelegateClassName() {
        return delegateClassName;
    }

    public String getDelegateMethodName() {
        return delegateMethodName;
    }

    public String getDelegateDescriptor() {
        return delegateDescriptor;
    }

    @Override
    public String toString() {
        return "MethodInsn{" +
                "methodName='" + methodName + '\'' +
                ", targetClassName='" + targetClassName + '\'' +
                ", targetMethodName='" + targetMethodName + '\'' +
                ", delegateClassName='" + delegateClassName + '\'' +
                ", delegateMethodName='" + delegateMethodName + '\'' +
                ", delegateDescriptor='" + delegateDescriptor + '\'' +
                '}';
    }
}
