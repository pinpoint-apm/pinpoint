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

package com.navercorp.pinpoint.profiler.plugin.xml.transformer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;

public class FilteringMethodTransformer implements MethodTransformer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final MethodFilter filter;
    private final List<MethodRecipe> recipes;
    private final MethodTransformerExceptionHandler exceptionHandler; 

    public FilteringMethodTransformer(MethodFilter[] filters, List<MethodRecipe> recipes, MethodTransformerExceptionHandler handler) {
        this.filter = filters.length == 1 ? filters[0] : new AndFilter(filters);
        this.recipes = recipes;
        this.exceptionHandler = handler;
    }
    
    @Override
    public void edit(ClassLoader classLoader, InstrumentClass target) throws Throwable {
        for (InstrumentMethod targetMethod : target.getDeclaredMethods(filter)) {
            for (MethodRecipe recipe : recipes) {
                try {
                    recipe.edit(classLoader, target, targetMethod);
                } catch (Throwable t) {
                    if (exceptionHandler != null) {
                        exceptionHandler.handle(target.getName(), targetMethod.getName(), targetMethod.getParameterTypes(), t);
                        logger.info("Exception thrown while editing" + targetMethod.getDescriptor().getApiDescriptor() + " but MethodTransformerExceptionHandler handled it.", t);
                    } else {
                        throw new InstrumentException("Fail to edit method " + targetMethod.getDescriptor().getApiDescriptor(), t);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FilteringMethodTransformer [filter=");
        builder.append(filter);
        builder.append(", recipes=");
        builder.append(recipes);
        
        if (exceptionHandler != null) {
            builder.append(", exceptionHandler=");
            builder.append(exceptionHandler);
        }
        
        builder.append(']');
        return builder.toString();
    }
    
    private static final class AndFilter implements MethodFilter {
        private final MethodFilter[] filters;

        public AndFilter(MethodFilter[] filters) {
            this.filters = filters;
        }

        @Override
        public boolean accept(InstrumentMethod method) {
            for (MethodFilter filter : filters) {
                if (!filter.accept(method)) {
                    return REJECT;
                }
            }
            
            return ACCEPT;
        }
    }
}
