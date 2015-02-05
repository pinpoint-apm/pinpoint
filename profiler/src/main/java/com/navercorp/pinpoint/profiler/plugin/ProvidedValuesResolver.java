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
package com.navercorp.pinpoint.profiler.plugin;

import java.lang.annotation.Annotation;

/**
 * @author Jongho Moon
 *
 */
public class ProvidedValuesResolver implements JudgingParameterResolver {
    private final Object[] values;
    private int index = 0;

    public ProvidedValuesResolver(Object[] values) {
        this.values = values;
    }

    @Override
    public void prepare() {
        index = 0;
    }

    @Override
    public Option<Object> resolve(int index, Class<?> type, Annotation[] annotations) {
        if (this.index >= values.length) {
            return Option.<Object>empty();
        }
        
        Object candidate = values[this.index];
        
        if (type.isPrimitive()) {
            if (candidate == null) {
                return Option.<Object>empty();
            }
            
            if (TypeUtils.getWrapperOf(type) == candidate.getClass()) {
                this.index++;
                return Option.withValue(candidate); 
            }
        } else {
            if (type.isInstance(candidate)) {
                this.index++;
                return Option.withValue(candidate);
            }
        }
        
        return Option.<Object>empty();
    }

    @Override
    public boolean isAcceptable() {
        return index == values.length;
    }
}
