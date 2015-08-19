/**
 * Copyright 2014 NAVER Corp.
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

import java.lang.reflect.Method;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;

/**
 * @author Jongho Moon
 *
 */
public class InvokeBeforeCodeGenerator extends InvokeCodeGenerator {
    private final int interceptorId;
    private final Method interceptorMethod;
    private final InstrumentClass targetClass;
    private final ExecutionPolicy policy;
    
    public InvokeBeforeCodeGenerator(int interceptorId, Class<?> interceptorClass, Method interceptorMethod, InstrumentClass targetClass, InstrumentMethod targetMethod, ExecutionPolicy policy) {
        super(interceptorId, interceptorClass, targetMethod, policy);
        
        this.interceptorId = interceptorId;
        this.interceptorMethod = interceptorMethod;
        this.targetClass = targetClass;
        this.policy = policy;
    }

    public String generate() {
        CodeBuilder builder = new CodeBuilder();
        
        builder.begin();

        // try {
        //     _$PINPOINT$_holder13 = InterceptorRegistry.findInterceptor(13);
        //     _$PINPOINT$_groupInvocation13 = _$PINPOINT$_holder13.getGroup().getCurrentInvocation();
        //     
        //     if (_$PINPOINT$_groupInvocation13.tryEnter(ExecutionPolicy.POLICY) {
        //         (($INTERCEPTOR_TYPE)_$PINPOINT$_holder13.getInterceptor.before($ARGUMENTS);
        //     } else {
        //         _$PINPOINT$_groupInvocation13 = null;
        //         InterceptorInvokerHelper.logSkipBeforeByExecutionPolicy(_$PINPOINT$_holder13, _$PINPOINT$_groupInvocation13, ExecutionPolicy.POLICY);
        //     }
        // } catch (Throwable t) {
        //     InterceptorInvokerHelper.handleException(t);
        // }
        
        builder.append("try { ");
        builder.format("%1$s = %2$s.findInterceptor(%3$d); ", getInterceptorInstanceVar(), getInterceptorRegistryClassName(), interceptorId);
        
        if (policy != null) {
            builder.format("%1$s = %2$s.getGroup().getCurrentInvocation();", getInterceptorGroupInvocationVar(), getInterceptorInstanceVar());
            builder.format("if (%1$s.tryEnter(%2$s)) {", getInterceptorGroupInvocationVar(), getExecutionPolicy());
        }
        
        if (interceptorMethod != null) {
            builder.format("((%1$s)%2$s.getInterceptor()).before(", getInterceptorType(), getInterceptorInstanceVar());
            appendArguments(builder);
            builder.format(");");
        }
        
        if (policy != null) {
            builder.format(" } else { %1$s.logSkipBeforeByExecutionPolicy(%2$s, %3$s, %4$s); }", getInterceptorInvokerHelperClassName(), getInterceptorInstanceVar(), getInterceptorGroupInvocationVar(), getExecutionPolicy());
        }
        
        builder.format("} catch (java.lang.Throwable _$PINPOINT_EXCEPTION$_) { %1$s.handleException(_$PINPOINT_EXCEPTION$_); }", getInterceptorInvokerHelperClassName());
        
        builder.end();
        
        return builder.toString();
    }

    private void appendArguments(CodeBuilder builder) {
        switch (type) {
        case SIMPLE:
            appendSimpleBeforeArguments(builder);
            break;
        case STATIC:
            appendStaticBeforeArguments(builder);
            break;
        case CUSTOM:
            appendCustomBeforeArguments(builder);
            break;
        }
    }

    private void appendSimpleBeforeArguments(CodeBuilder builder) {
        builder.format("%1$s, %2$s", getTarget(), getArguments());
    }
    
    private void appendStaticBeforeArguments(CodeBuilder builder) {
        builder.format("%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s", getTarget(), targetClass.getName(), targetMethod.getName(), getParameterTypes(), getArguments());
    }

    private void appendCustomBeforeArguments(CodeBuilder builder) {
        Class<?>[] paramTypes = interceptorMethod.getParameterTypes();
        
        if (paramTypes.length == 0) {
            return;
        }
        
        builder.append(getTarget());
        
        int i = 0;
        int argNum = targetMethod.getParameterTypes().length;
        int interceptorArgNum = paramTypes.length - 1;
        int matchNum = Math.min(argNum, interceptorArgNum);
        
        for (; i < matchNum; i++) {
            builder.append(", $" + (i + 1));
        }
        
        for (; i < interceptorArgNum; i++) {
            builder.append(", null");
        }
    }
}
