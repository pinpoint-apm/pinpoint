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

import com.navercorp.pinpoint.bootstrap.resolver.condition.ClassResourceCondition;
import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;
import com.navercorp.pinpoint.bootstrap.resolver.condition.PropertyCondition;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.List;

/**
 * 
 * @author HyunGil Jeong
 */
public class ConditionProvider {
    
    public static final ConditionProvider DEFAULT_CONDITION_PROVIDER = new ConditionProvider();
    
    private final MainClassCondition mainClassCondition;
    
    private final PropertyCondition systemPropertyCondition;
    
    private final ClassResourceCondition classResourceCondition;
    
    private ConditionProvider() {
        this(new MainClassCondition(), new PropertyCondition(), new ClassResourceCondition());
    }
    
    ConditionProvider(MainClassCondition mainClassCondition, PropertyCondition systemPropertyCondition, ClassResourceCondition classResourceCondition) {
        this.mainClassCondition = mainClassCondition;
        this.systemPropertyCondition = systemPropertyCondition;
        this.classResourceCondition = classResourceCondition;
    }
    
    /**
     * Returns the fully qualified class name of the application's main class.
     * 
     * @return the fully qualified class name of the main class, or an empty string if the main class cannot be resolved
     * @see MainClassCondition#getValue()
     */
    public String getMainClass() {
        return this.mainClassCondition.getValue();
    }

    /**
     * Checks if candidate matches the fully qualified class name of the application's main class.
     * If the main class cannot be resolved, the method return <tt>false</tt>.
     *
     * @param candidate the value to check against the application's main class name
     * @return <tt>true</tt> if candidate matches the name of the main class;
     *         <tt>false</tt> if otherwise, or if the main class cannot be resolved
     * @see MainClassCondition#check(String)
     */
    public boolean checkMainClass(String candidate) {
        if (candidate == null) {
            return false;
        } else {
            String trimmedCandidate = candidate.trim();
            return this.mainClassCondition.check(trimmedCandidate);
        }
    }

    /**
     * Checks if any of the candidates match the fully qualified class name of the application's main class.
     * If the main class cannot be resolved, the method returns <tt>false</tt>.
     *
     * @param candidates the values to check against the application's main class name
     * @return <tt>true</tt> if any of the candidates match the name of the main class;
     *         <tt>false</tt> if otherwise, or if the main class cannot be resolved
     * @see MainClassCondition#check(String)
     */
    public boolean checkMainClass(List<String> candidates) {
        if (CollectionUtils.isEmpty(candidates)) {
            return false;
        }
        for (String candidate : candidates) {
            if (this.checkMainClass(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the system property value for the specified key.
     * 
     * @return the system property value, or an empty string if the key is null or empty 
     */
    public String getSystemPropertyValue(String systemPropertyKey) {
        if (StringUtils.isEmpty(systemPropertyKey)) {
            return "";
        }
        return this.systemPropertyCondition.getValue().getProperty(systemPropertyKey);
    }
    
    /**
     * Checks if the specified value is in the system property.
     * 
     * @param systemPropertyKey the values to check if they exist in the system property
     * @return <tt>true</tt> if the specified key is in the system property; 
     *         <tt>false</tt> if otherwise, or if <tt>null</tt> or empty key is provided
     */
    public boolean checkSystemProperty(String systemPropertyKey) {
        return this.systemPropertyCondition.check(systemPropertyKey);
    }
    
    /**
     * Checks if the specified class can be found in the current System ClassLoader's search path.
     * 
     * @param requiredClass the fully qualified class name of the class to check
     * @return <tt>true</tt> if the specified class can be found in the system class loader's search path, 
     *         <tt>false</tt> if otherwise
     * @see ClassResourceCondition#check(String)
     */
    public boolean checkForClass(String requiredClass) {
        return this.classResourceCondition.check(requiredClass);
    }
    
}
