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

package com.navercorp.pinpoint.bootstrap.resolver.condition;

import com.navercorp.pinpoint.common.util.SystemProperty;

/**
 * Checks if the specified property keys are in the system property.
 * 
 * @author HyunGil Jeong
 */
public class SystemPropertyCondition implements Condition<String[]>, ConditionValue<SystemProperty> {

    private final SystemProperty property;

    public SystemPropertyCondition() {
        this(SystemProperty.INSTANCE);
    }

    SystemPropertyCondition(SystemProperty property) {
        this.property = property;
    }
    
    /**
     * Checks if the specified values are in the system property.
     * 
     * @param requiredKeys the values to check if they exist in the system property
     * @return <tt>true</tt> if all of the specified keys are in the system property; 
     *         <tt>false</tt> if otherwise, or if <tt>null</tt> values are provided
     */
    @Override
    public boolean check(String ... requiredKeys) {
        if (requiredKeys == null) {
            return false;
        }
        for (String requiredKey : requiredKeys) {
            if (this.property.getProperty(requiredKey) == null) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns the system property.
     * 
     * @return the {@link SystemProperty} instance
     */
    @Override
    public SystemProperty getValue() {
        return this.property;
    }
    
}
