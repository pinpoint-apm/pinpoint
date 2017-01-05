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
package com.navercorp.pinpoint.profiler.plugin.xml.transformer;


/**
 * @author Jongho Moon
 *
 */
public interface MethodTransformerExceptionHandler {
    /**
     * Handles exception thrown wile editing method.
     * 
     * If you want to continue editing class, return normally.
     * If you want to stop editing class, throw an exception.
     * 
     * @param targetClassName
     * @param targetMethodName
     * @param targetMethodParameterTypes
     * @param exception
     */
    void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Throwable;
    
    
    MethodTransformerExceptionHandler IGNORE = new MethodTransformerExceptionHandler() {
        
        @Override
        public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Throwable {
            // do nothing
        }

        @Override
        public String toString() {
            return "MethodTransformerExceptionHandler.IGNORE";
        }
    };
}
