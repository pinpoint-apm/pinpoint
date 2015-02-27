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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.resolver.condition.SystemPropertyCondition;
import com.navercorp.pinpoint.common.util.SystemProperty;

/**
 * @author HyunGil Jeong
 */
public class SystemPropertyConditionTest {
    
    @Test
    public void testMatch() {
        // Given
        final String existingSystemProperty1 = "set.one.key.one";
        final String existingSystemProperty2 = "set.one.key.two";
        SystemProperty property = createTestProperty(existingSystemProperty1, existingSystemProperty2);
        SystemPropertyCondition systemPropertyCondition = new SystemPropertyCondition(property);
        // When
        boolean matches = systemPropertyCondition.check(existingSystemProperty1, existingSystemProperty2);
        // Then
        assertTrue(matches);
    }
    
    @Test
    public void testNoMatch() {
        // Given
        final String existingSystemProperty = "existing.system.property";
        SystemProperty property = createTestProperty(existingSystemProperty);
        SystemPropertyCondition systemPropertyCondition = new SystemPropertyCondition(property);
        // When
        boolean matches = systemPropertyCondition.check("some.other.property");
        // Then
        assertFalse(matches);
    }
    
    @Test
    public void partialMatchShouldResultInNoMatch() {
        // Given
        final String existingSystemProperty = "set.one.key.one";
        SystemProperty property = createTestProperty(existingSystemProperty);
        SystemPropertyCondition systemPropertyCondition = new SystemPropertyCondition(property);
        // When
        boolean matches = systemPropertyCondition.check(existingSystemProperty, "some.other.property");
        // Then
        assertFalse(matches);
    }
    
    @Test
    public void emptyConditionShouldMatch() {
        // Given
        final String existingSystemProperty = "existing.system.property";
        SystemProperty property = createTestProperty(existingSystemProperty);
        SystemPropertyCondition systemPropertyCondition = new SystemPropertyCondition(property);
        // When
        boolean matches = systemPropertyCondition.check(new String[0]);
        // Then
        assertTrue(matches);
    }
    
    @Test
    public void conditionWithNullElementShouldMatch() {
        // Given
        final String existingSystemProperty = "existing.system.property";
        SystemProperty property = createTestProperty(existingSystemProperty);
        SystemPropertyCondition systemPropertyCondition = new SystemPropertyCondition(property);
        final String[] conditionWithNullElement = new String[] {null}; 
        // When
        boolean matches = systemPropertyCondition.check(conditionWithNullElement);
        // Then
        assertFalse(matches);
    }
    
    @Test
    public void nullConditionShouldNotMatch() {
        // Given
        final String existingSystemProperty = "existing.system.property";
        SystemProperty property = createTestProperty(existingSystemProperty);
        SystemPropertyCondition systemPropertyCondition = new SystemPropertyCondition(property);
        final String[] nullCondition = null;
        // When
        boolean matches = systemPropertyCondition.check(nullCondition);
        // Then
        assertFalse(matches);
    }
    
    private static SystemProperty createTestProperty() {
        return new SystemProperty() {
            
            private final Map<String, String> properties = new HashMap<String, String>();
            
            @Override
            public void setProperty(String key, String value) {
                this.properties.put(key, value);
            }
            
            @Override
            public String getProperty(String key) {
                return this.properties.get(key);
            }
            
            @Override
            public String getProperty(String key, String defaultValue) {
                if (this.properties.containsKey(key)) {
                    return this.properties.get(key);
                } else {
                    return defaultValue;
                }
            }
        };
    }
    
    private static SystemProperty createTestProperty(String ... keys) {
        SystemProperty property = createTestProperty();
        if (keys == null) {
            return property;
        }
        for (String key : keys) {
            property.setProperty(key, "");
        }
        return property;
    }
    
}
