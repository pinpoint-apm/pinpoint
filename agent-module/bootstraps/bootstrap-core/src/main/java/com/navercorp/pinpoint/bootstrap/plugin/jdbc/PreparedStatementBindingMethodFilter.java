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
package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jongho Moon
 *
 */
public class PreparedStatementBindingMethodFilter implements MethodFilter {
    private static final Map<String, List<String[]>> BIND_METHODS = getBindVariableDesc();


    private static Map<String, List<String[]>> getBindVariableDesc() {
        List<Method> methods = PreparedStatementUtils.findBindVariableSetMethod();
        Map<String, List<String[]>> bindMethod = new HashMap<>();

        for (Method method : methods) {
            List<String[]> parameterTypeList = bindMethod.computeIfAbsent(method.getName(), k -> new ArrayList<>());
            String[] paramTypeNames = getParameterTypeNames(method);

            parameterTypeList.add(paramTypeNames);
        }
        return bindMethod;
    }

    private static String[] getParameterTypeNames(Method method) {
        if (method.getParameterCount() == 0) {
            return new String[0];
        }
        return getParameterTypeNames(method.getParameterTypes());
    }

    private static String[] getParameterTypeNames(Class<?>[] paramTypes) {
        int len = paramTypes.length;
        String[] paramTypeNames = new String[len];
        for (int i = 0; i < len; i++) {
            Class<?> paramType = paramTypes[i];
            paramTypeNames[i] = ReflectionUtils.getParameterTypeName(paramType);
        }
        return paramTypeNames;
    }


    public static PreparedStatementBindingMethodFilter includes(String... names) {
        Map<String, List<String[]>> targets = new HashMap<>(names.length);
        
        for (String name : names) {
            List<String[]> paramTypes = BIND_METHODS.get(name);
            
            if (paramTypes != null) {
                targets.put(name, paramTypes);
            }
        }
        
        return new PreparedStatementBindingMethodFilter(targets);
    }

    public static PreparedStatementBindingMethodFilter excludes(String... names) {
        Map<String, List<String[]>> targets = new HashMap<>(BIND_METHODS);
        
        for (String name : names) {
            targets.remove(name);
        }
        
        return new PreparedStatementBindingMethodFilter(targets);
    }

    
    private final Map<String, List<String[]>> methods;
    
    public PreparedStatementBindingMethodFilter() {
        this(BIND_METHODS);
    }
    
    public PreparedStatementBindingMethodFilter(Map<String, List<String[]>> methods) {
        this.methods = Objects.requireNonNull(methods, "methods");
    }

    @Override
    public boolean accept(InstrumentMethod method) {
        List<String[]> paramTypes = methods.get(method.getName());
        
        if (paramTypes == null) {
            return REJECT;
        }
        
        for (String[] types : paramTypes) {
            if (Arrays.equals(types, method.getParameterTypes())) {
                return ACCEPT;
            }
        }

        return REJECT;
    }

}
