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

package com.navercorp.pinpoint.profiler.plugin.editor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorExceptionHandler;

public class FilteringMethodEditor implements MethodEditor {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final MethodFilter filter;
    private final List<MethodRecipe> recipes;
    private final MethodEditorExceptionHandler exceptionHandler; 

    public FilteringMethodEditor(MethodFilter filter, List<MethodRecipe> recipes, MethodEditorExceptionHandler handler) {
        this.filter = filter;
        this.recipes = recipes;
        this.exceptionHandler = handler;
    }
    
    @Override
    public void edit(ClassLoader classLoader, InstrumentClass target) throws Throwable {
        for (MethodInfo targetMethod : target.getDeclaredMethods(filter)) {
            for (MethodRecipe recipe : recipes) {
                try {
                    recipe.edit(classLoader, target, targetMethod);
                } catch (Throwable t) {
                    if (exceptionHandler != null) {
                        exceptionHandler.handle(target.getName(), targetMethod.getName(), targetMethod.getParameterTypes(), t);
                        logger.info("Exception thrown while editing" + targetMethod.getDescriptor().getApiDescriptor() + " but MethodEditorExceptionHandler handled it.", t);
                    } else {
                        throw new InstrumentException("Fail to edit method " + targetMethod.getDescriptor().getApiDescriptor(), t);
                    }
                }
            }
        }
    }
}
