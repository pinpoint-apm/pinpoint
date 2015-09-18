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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;

public class ConstructorTransformer implements MethodTransformer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String[] targetParameterTypes;
    private final List<MethodRecipe> recipes;
    private final MethodTransformerExceptionHandler exceptionHandler;
    private final boolean ignoreIfNotExist;

    public ConstructorTransformer(String[] targetParameterTypes, List<MethodRecipe> recipes, MethodTransformerExceptionHandler exceptionHandler, boolean ignoreIfNotExist) {
        this.targetParameterTypes = targetParameterTypes;
        this.recipes = recipes;
        this.exceptionHandler = exceptionHandler;
        this.ignoreIfNotExist = ignoreIfNotExist;
    }

    @Override
    public void edit(ClassLoader classLoader, InstrumentClass target) throws Throwable {
        InstrumentMethod targetConstructor = target.getConstructor(targetParameterTypes);
        
        if (targetConstructor == null) {
            if (ignoreIfNotExist) {
                return;
            } else {
                Exception e = new NoSuchMethodException("No such constructor: " + "(" + Arrays.deepToString(targetParameterTypes) + ")");
                
                if (exceptionHandler != null) {
                    exceptionHandler.handle(target.getName(), "init", targetParameterTypes, e);
                    logger.info("Cannot find target constructor with parameter types (" + Arrays.deepToString(targetParameterTypes) + ") but MethodTransformerExceptionHandler handled it.");
                    return;
                } else {
                    throw new InstrumentException("Fail to edit constructor", e);
                }
            }
        }
        
        for (MethodRecipe recipe : recipes) {
            try {
                recipe.edit(classLoader, target, targetConstructor);
            } catch (Throwable t) {
                logger.info("Exception thrown while editing " + targetConstructor.getDescriptor().getApiDescriptor(), t);
                
                if (exceptionHandler != null) {
                    exceptionHandler.handle(target.getName(), "init", targetParameterTypes, t);
                    logger.info("Exception thrown while editing" + targetConstructor.getDescriptor().getApiDescriptor() + " but MethodTransformerExceptionHandler handled it.", t);
                } else {
                    throw new InstrumentException("Fail to edit constructor " + targetConstructor.getDescriptor().getApiDescriptor(), t);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConstructorTransformer[paramTypes=");
        builder.append(Arrays.toString(targetParameterTypes));
        builder.append(", recipes=");
        builder.append(recipes);
        builder.append(", ignoreIfNotExist=");
        builder.append(ignoreIfNotExist);
        
        if (exceptionHandler != null) {
            builder.append(", exceptionHandler=");
            builder.append(exceptionHandler);
        }
        
        builder.append(']');
        return builder.toString();
    }
}
