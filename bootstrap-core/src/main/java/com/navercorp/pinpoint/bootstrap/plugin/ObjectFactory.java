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
package com.navercorp.pinpoint.bootstrap.plugin;

/**
 * @author Jongho Moon
 *
 */

public abstract class ObjectFactory {
    private final String className;
    private final Object[] arguments;
    
    private ObjectFactory(String className, Object[] arguments) {
        this.className = className;
        this.arguments = arguments;
    }

    public String getClassName() {
        return className;
    }

    public Object[] getArguments() {
        return arguments;
    }

    
    public static ObjectFactory byConstructor(String className, Object... args) {
        return new ByConstructor(className, args);
    }
    
    public static ObjectFactory byStaticFactory(String className, String factoryMethodName, Object... args) {
        return new ByStaticFactoryMethod(className, factoryMethodName, args);
    }

    
    public static class ByConstructor extends ObjectFactory {
        public ByConstructor(String className, Object[] arguments) {
            super(className, arguments);
        }
    }
    
    public static class ByStaticFactoryMethod extends ObjectFactory {
        private final String factoryMethodName;
        
        public ByStaticFactoryMethod(String className, String factoryMethodName, Object[] arguments) {
            super(className, arguments);
            this.factoryMethodName = factoryMethodName;
        }

        public String getFactoryMethodName() {
            return factoryMethodName;
        }
    }
    
    public static class ByFactoryObject extends ObjectFactory {
        private final ObjectFactory recipe;
        private final String factoryMethod;
        
        public ByFactoryObject(String className, ObjectFactory recipe, String factoryMethod, Object[] arguments) {
            super(className, arguments);
            this.recipe = recipe;
            this.factoryMethod = factoryMethod;
        }

        public ObjectFactory getRecipe() {
            return recipe;
        }

        public String getFactoryMethod() {
            return factoryMethod;
        }
    }
}
