/*
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
package com.navercorp.pinpoint.profiler.instrument.interceptor;

import java.lang.reflect.Method;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;

/**
 * @author Jongho Moon
 *
 */
public class InvokeAfterCodeGenerator extends InvokeCodeGenerator {

    private static final int THIS_RETURN_EXCEPTION_SIZE = 3;

    private final int interceptorId;
    private final InterceptorDefinition interceptorDefinition;
    private final InstrumentClass targetClass;
    private final boolean localVarsInitialized;
    private final boolean catchClause;

    public InvokeAfterCodeGenerator(int interceptorId, InterceptorDefinition interceptorDefinition, InstrumentClass targetClass, InstrumentMethod targetMethod, ApiMetaDataService apiMetaDataService, boolean localVarsInitialized, boolean catchClause) {
        super(interceptorId, interceptorDefinition, targetMethod, apiMetaDataService);
        this.interceptorDefinition = interceptorDefinition;
        this.interceptorId = interceptorId;
        this.targetClass = targetClass;
        this.localVarsInitialized = localVarsInitialized;
        this.catchClause = catchClause;
    }

    public String generate() {
        final CodeBuilder builder = new CodeBuilder();

        builder.begin();

        // try {
        //    (($INTERCEPTOR_TYPE)_$PINPOINT$_holder13.getInterceptor.before($ARGUMENTS);
        // } catch (Throwable t) {
        //     InterceptorInvokerHelper.handleException(t);
        // }
        //
        // throw e;

        builder.append("try { ");

        if (!localVarsInitialized) {
            builder.format("%1$s = %2$s.getInterceptor(%3$d); ", getInterceptorVar(), getInterceptorRegistryClassName(), interceptorId);
        }
        final Method afterMethod = interceptorDefinition.getAfterMethod();
        if (afterMethod != null) {
            builder.format("((%1$s)%2$s).after(", getInterceptorType(), getInterceptorVar());
            appendArguments(builder);
            builder.format(");");
        }

        builder.format("} catch (java.lang.Throwable _$PINPOINT_EXCEPTION$_) { %1$s.handleException(_$PINPOINT_EXCEPTION$_); }", getInterceptorInvokerHelperClassName());

        if (catchClause) {
            builder.append(" throw $e;");
        }

        builder.end();
        
        return builder.toString();
        
    }

    private String getReturnValue() {
        if (catchClause) {
            return "null";
        }
        
        if (!targetMethod.isConstructor()) {
            if ("void".equals(targetMethod.getReturnType())) {
                return "null";
            }
        }

        return "($w)$_";
    }
        
    private String getException() {
        if (catchClause) {
            return "$e";
        }
        
        return "null";
    }

    private void appendArguments(CodeBuilder builder) {
        final InterceptorType type = interceptorDefinition.getInterceptorType();
        switch (type) {
        case ARRAY_ARGS:
            appendSimpleAfterArguments(builder);
            break;
        case STATIC:
            appendStaticAfterArguments(builder);
            break;
        case API_ID_AWARE:
            appendApiIdAwareAfterArguments(builder);
            break;
        case BASIC:
            appendCustomAfterArguments(builder);
            break;
        }
    }

    private void appendSimpleAfterArguments(CodeBuilder builder) {
        builder.format("%1$s, %2$s, %3$s, %4$s", getTarget(), getArguments(), getReturnValue(), getException());
    }

    private void appendStaticAfterArguments(CodeBuilder builder) {
        builder.format("%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, %6$s, %7$s", getTarget(), targetClass.getName(), targetMethod.getName(), getParameterTypes(), getArguments(), getReturnValue(), getException());
    }

    private void appendApiIdAwareAfterArguments(CodeBuilder builder) {
        builder.format("%1$s, %2$d, %3$s, %4$s, %5$s", getTarget(), getApiId(), getArguments(), getReturnValue(), getException());
    }

    private void appendCustomAfterArguments(CodeBuilder builder) {
        final Method interceptorMethod = interceptorDefinition.getAfterMethod();
        final Class<?>[] interceptorParamTypes = interceptorMethod.getParameterTypes();
        
        if (interceptorParamTypes.length == 0) {
            return;
        }

        builder.append(getTarget());

        final int parameterSize = parameterBind(builder, interceptorParamTypes);
        final int bindSize = parameterSize + THIS_RETURN_EXCEPTION_SIZE;
        if (bindSize != interceptorParamTypes.length) {
            throw new IllegalStateException("interceptor arguments not matched. interceptorSize:" + interceptorParamTypes.length + " bindSize:" + bindSize);
        }
//        if (interceptorParamTypes.length >= 2) {
        builder.append(", ");
        builder.append(getReturnValue());
//        }

//        if (interceptorParamTypes.length >= 3) {
        builder.append(", ");
        builder.append(getException());
//        }
    }

    private int parameterBind(CodeBuilder builder, Class<?>[] interceptorParamTypes) {

        final int methodArgNum = targetMethod.getParameterTypes().length;

        final int interceptorArgNum = interceptorParamTypes.length - THIS_RETURN_EXCEPTION_SIZE;

        final int matchNum = Math.min(methodArgNum, interceptorArgNum);

        int parameterIndex = 0;
        for (; parameterIndex < matchNum; parameterIndex++) {
            builder.append(", ($w)$" + (parameterIndex + 1));
        }

        for (; parameterIndex < interceptorArgNum; parameterIndex++) {
            builder.append(", null");
        }
        return parameterIndex;
    }
}
