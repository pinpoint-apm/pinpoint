/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.interceptor.bci;

import javassist.CtBehavior;
import javassist.CtConstructor;

import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.profiler.interceptor.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

public class JavassistMethodInfo implements MethodInfo {
    private final CtBehavior behavior;
    private final MethodDescriptor descriptor;
    
    public JavassistMethodInfo(CtBehavior behavior) {
        this.behavior = behavior;
        
        String[] parameterVariableNames = JavaAssistUtils.getParameterVariableName(behavior);
        this.descriptor = new DefaultMethodDescriptor(behavior.getDeclaringClass().getName(), behavior.getName(), getParameterTypes(), parameterVariableNames);
    }

    @Override
    public String getName() {
        return behavior.getName();
    }

    @Override
    public String[] getParameterTypes() {
        return JavaAssistUtils.parseParameterSignature(behavior.getSignature());
    }

    @Override
    public int getModifiers() {
        return behavior.getModifiers();
    }
    
    @Override
    public boolean isConstructor() {
        return behavior instanceof CtConstructor;
    }

    @Override
    public MethodDescriptor getDescriptor() {
        return descriptor;
    }

}
