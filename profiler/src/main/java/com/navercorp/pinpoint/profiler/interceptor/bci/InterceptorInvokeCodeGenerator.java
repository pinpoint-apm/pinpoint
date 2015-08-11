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
import java.lang.reflect.Modifier;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptorExceptionHandler;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author Jongho Moon
 *
 */
public class InterceptorInvokeCodeGenerator {
    private final int interceptorId;
    private final Class<?> interceptorClass;
    private final Method interceptorMethod;
    private final InstrumentClass targetClass;
    private final InstrumentMethod targetMethod;
    private final boolean inCatch;
    private final Type type;
    
    private enum Type {
        SIMPLE,
        STATIC,
        CUSTOM
    }

    public InterceptorInvokeCodeGenerator(int interceptorId, Class<?> interceptorClass, Method interceptorMethod, InstrumentClass targetClass, InstrumentMethod targetMethod, boolean inCatch) {
        this.interceptorId = interceptorId;
        this.interceptorClass = interceptorClass;
        this.interceptorMethod = interceptorMethod;
        this.targetClass = targetClass;
        this.targetMethod = targetMethod;
        this.inCatch = inCatch;
        
        if (SimpleAroundInterceptor.class.isAssignableFrom(interceptorClass)) {
            type = Type.SIMPLE;
        } else if (StaticAroundInterceptor.class.isAssignableFrom(interceptorClass)) {
            type = Type.STATIC;
        } else {
            type = Type.CUSTOM;
        }
    }

    public String generate() {
        CodeBuilder builder = new CodeBuilder();
        
        builder.begin();

        // try {
        //     (($INTERCEPTOR_TYPE)InterceptorRegistry.findInterceptor($INTERCEPTOR_ID)).$INTERCEPTOR_METHOD_NAME($ARGUMENTS);
        // } catch (Throwable t) {
        //     InterceptorExceptionHandler.handleException(t);
        // }

        builder.append("try { ");
        
        appendInterceptorRetrieval(builder);
        builder.format(".%1$s(", interceptorMethod.getName());

        appendArguments(builder);

        builder.format("); } catch (java.lang.Throwable _$PINPOINT_EXCEPTION$_) { %1$s.handleException(_$PINPOINT_EXCEPTION$_); }", InterceptorExceptionHandler.class.getName());
        
        if (inCatch) {
            builder.append(" throw $e;");
        }
        
        builder.end();
        
        return builder.toString();
    }

    private void appendInterceptorRetrieval(CodeBuilder builder) {
        switch (type) {
        case SIMPLE:
            builder.format("%1$s.getSimpleInterceptor(%2$d)", InterceptorRegistry.class.getName(), interceptorId);
            break;
            
        case STATIC:
            builder.format("%1$s.getStaticInterceptor(%2$d)", InterceptorRegistry.class.getName(), interceptorId);
            break;
            
        case CUSTOM:
            builder.format("((%1$s)%2$s.getStaticInterceptor(%2$d))", interceptorClass.getName(), InterceptorRegistry.class.getName(), interceptorId);
            break;
        }
    }

    private String getParameterTypes() {
        String[] parameterTypes = targetMethod.getParameterTypes();
        return JavaAssistUtils.getParameterDescription(parameterTypes);
    }

    private String getTarget() {
        return Modifier.isStatic(targetMethod.getModifiers()) ? "null" : "this";
    }

    private String getReturnValue() {
        if (inCatch) {
            return "null";
        }
        
        if (!targetMethod.isConstructor()) {
            if ("void".equals(targetMethod.getReturnType())) {
                return "null";
            }
        }

        return "($w)$_";
    }
    
    private String getArguments() {
        if (targetMethod.getParameterTypes().length == 0) {
            return "null";
        }
        
        return "$args";
    }
    
    private String getException() {
        if (inCatch) {
            return "$e";
        }
        
        return "null";
    }

    private void appendArguments(CodeBuilder builder) {
        boolean after = "after".equals(interceptorMethod.getName());
        
        if (after) {
            switch (type) {
            case SIMPLE:
                appendSimpleAfterArguments(builder);
                break;
            case STATIC:
                appendStaticAfterArguments(builder);
                break;
            case CUSTOM:
                appendCustomAfterArguments(builder);
                break;
            }
        } else {
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
    }

    private void appendSimpleAfterArguments(CodeBuilder builder) {
        builder.format("%1$s, %2$s, %3$s, %4$s", getTarget(), getArguments(), getReturnValue(), getException());
    }

    private void appendSimpleBeforeArguments(CodeBuilder builder) {
        builder.format("%1$s, %2$s", getTarget(), getArguments());
    }
    
    private void appendStaticBeforeArguments(CodeBuilder builder) {
        builder.format("%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s", getTarget(), targetClass.getName(), targetMethod.getName(), getParameterTypes(), getArguments());
    }

    private void appendStaticAfterArguments(CodeBuilder builder) {
        builder.format("%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, %6$s, %7$s", getTarget(), targetClass.getName(), targetMethod.getName(), getParameterTypes(), getArguments(), getReturnValue(), getException());
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

    private void appendCustomAfterArguments(CodeBuilder builder) {
        Class<?>[] paramTypes = interceptorMethod.getParameterTypes();
        
        if (paramTypes.length == 0) {
            return;
        }
        
        builder.append(getTarget());
        
        if (paramTypes.length >= 2) {
            builder.append(", ");
            builder.append(getReturnValue());
        }
        
        if (paramTypes.length >= 3) {
            builder.append(", ");
            builder.append(getException());
        }
        
        int i = 0;
        int argNum = targetMethod.getParameterTypes().length;
        int interceptorArgNum = paramTypes.length - 3;
        int matchNum = Math.min(argNum, interceptorArgNum);
        
        for (; i < matchNum; i++) {
            builder.append(", $" + (i + 1));
        }
        
        for (; i < interceptorArgNum; i++) {
            builder.append(", null");
        }
    }
}
