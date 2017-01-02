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
package com.navercorp.pinpoint.profiler.objectfactory;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author Jongho Moon
 *
 */
public class ArgumentsResolver {

    private final List<ArgumentProvider> parameterResolvers;

    public ArgumentsResolver(List<ArgumentProvider> parameterResolvers) {
        this.parameterResolvers = parameterResolvers;
    }

    public Object[] resolve(Class<?>[] types, Annotation[][] annotations) {
        int length = types.length;
        Object[] arguments = new Object[length];
        
        for (ArgumentProvider resolver : parameterResolvers) {
            if (resolver instanceof JudgingParameterResolver) {
                ((JudgingParameterResolver)resolver).prepare();
            }
        }
        
        outer:
        for (int i = 0; i < length; i++) {
            for (ArgumentProvider resolver : parameterResolvers) {
                Option resolved = resolver.get(i, types[i], annotations[i]);
                
                if (resolved.hasValue()) {
                    arguments[i] = resolved.getValue();
                    continue outer;
                }
            }
            
            return null;
        }
        
        for (ArgumentProvider resolver : parameterResolvers) {
            if (resolver instanceof JudgingParameterResolver) {
                if (!((JudgingParameterResolver)resolver).isAcceptable()) {
                    return null;
                }
            }
        }
        
        return arguments;
    }
}