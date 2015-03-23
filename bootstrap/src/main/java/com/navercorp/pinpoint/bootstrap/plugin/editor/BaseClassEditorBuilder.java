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
package com.navercorp.pinpoint.bootstrap.plugin.editor;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;

/**
 * @author Jongho Moon
 *
 */
public interface BaseClassEditorBuilder {

    public abstract void injectFieldAccessor(String fieldName);

    public abstract void injectMetadata(String name);

    public abstract void injectMetadata(String name, String initialValueType);

    public abstract void injectInterceptor(String className, Object... constructorArgs);

    public abstract void weave(String aspectClassName);

    public abstract MethodEditorBuilder editMethods(MethodFilter filter);

    public abstract MethodEditorBuilder editMethod(String name, String... parameterTypeNames);

    public abstract ConstructorEditorBuilder editConstructor(String... parameterTypeNames);

}