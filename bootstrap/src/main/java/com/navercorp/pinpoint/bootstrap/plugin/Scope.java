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
package com.navercorp.pinpoint.bootstrap.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated {@link Interceptor} should in a scope.
 * 
 * Scope is used to prevent handling same task twice.
 * 
 * Once a transaction entered a scope S by entering a method m() which is intercepted by an interceptor I scoped by S,
 * the interceptor I's before() is executed normally but all the other interceptors encounterd afterward within the same scope S are skipped
 * until the interceptor I's after() is executed.
 * 
 * For example, if an interceptor I intecept method a() which invokes itself recusively,
 * interceptor I will be executed every time a() is invoked.
 * To prevent this, you can put I in a scope S.
 * Then I will be invoked the first time a() is invoked only.
 * 
 * @author Jongho Moon
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {
    /**
     * scope name
     */
    public String value();
}
