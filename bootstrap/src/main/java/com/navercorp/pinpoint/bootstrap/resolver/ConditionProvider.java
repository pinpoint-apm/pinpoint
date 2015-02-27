/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.resolver;

import java.util.HashMap;
import java.util.Map;

import com.navercorp.pinpoint.bootstrap.resolver.condition.Condition;
import com.navercorp.pinpoint.bootstrap.resolver.condition.LibraryClassCondition;
import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;
import com.navercorp.pinpoint.bootstrap.resolver.condition.SystemPropertyCondition;

/**
 * @author HyunGil Jeong
 */
public class ConditionProvider {

    public static final ConditionProvider DEFAULT_CONDITION_PROVIDER = new ConditionProvider();

    private final Map<Class<? extends Condition<?>>, Condition<?>> conditions;

    public ConditionProvider() {
        this(new HashMap<Class<? extends Condition<?>>, Condition<?>>());
    }

    public ConditionProvider(Map<Class<? extends Condition<?>>, Condition<?>> conditions) {
        this.conditions = conditions;
        addDefaultConditions();
    }

    private void addDefaultConditions() {
        this.conditions.put(MainClassCondition.class, new MainClassCondition());
        this.conditions.put(SystemPropertyCondition.class, new SystemPropertyCondition());
        this.conditions.put(LibraryClassCondition.class, new LibraryClassCondition());
    }

    public <T extends Condition<?>> T getCondition(Class<T> conditionClass) {
        @SuppressWarnings("unchecked")
        T condition = (T)this.conditions.get(conditionClass);
        return condition;
    }

    /**
     * Returns {@link MainClassCondition} that allows plugins to check for the application's bootstrap main class.
     * 
     * @return the {@link MainClassCondition}
     */
    public MainClassCondition getMainClassCondition() {
        return (MainClassCondition)this.conditions.get(MainClassCondition.class);
    }

    /**
     * Returns {@link SystemPropertyCondition} that allows plugins to check for system property keys.
     * 
     * @return the {@link SystemPropertyCondition}
     */
    public SystemPropertyCondition getSystemPropertyCondition() {
        return (SystemPropertyCondition)this.conditions.get(SystemPropertyCondition.class);
    }

    /**
     * Returns {@link LibraryClassCondition} that allows plugins to check for classes accessible by
     * the system class loader.
     * 
     * @return the {@link LibraryClassCondition}
     */
    public LibraryClassCondition getLibraryClassCondition() {
        return (LibraryClassCondition)this.conditions.get(LibraryClassCondition.class);
    }
}
