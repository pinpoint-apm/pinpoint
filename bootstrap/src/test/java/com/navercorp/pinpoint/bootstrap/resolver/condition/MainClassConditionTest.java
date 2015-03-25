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

import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;
import com.navercorp.pinpoint.common.util.SimpleProperty;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;

/**
 * @author HyunGil Jeong
 */
public class MainClassConditionTest {
    
    private static final String TEST_MAIN_CLASS = "main.class.for.Test";
    
    @Test
    public void getValueShouldReturnBootstrapMainClass() {
        // Given
        SimpleProperty property = createTestProperty(TEST_MAIN_CLASS);
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        String expectedMainClass = mainClassCondition.getValue();
        // Then
        assertEquals(TEST_MAIN_CLASS, expectedMainClass);
    }
    
    @Test
    public void getValueShouldReturnEmptyStringWhenMainClassCannotBeResolved() {
        // Given
        SimpleProperty property = createTestProperty();
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        String expectedMainClass = mainClassCondition.getValue();
        // Then
        assertEquals("", expectedMainClass);
    }
    
    @Test
    public void testMatch() {
        // Given
        SimpleProperty property = createTestProperty(TEST_MAIN_CLASS);
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        boolean matches = mainClassCondition.check(TEST_MAIN_CLASS);
        // Then
        assertTrue(matches);
    }
    
    @Test
    public void testNoMatch() {
        // Given
        String givenBootstrapMainClass = "some.other.main.class";
        SimpleProperty property = createTestProperty(givenBootstrapMainClass);
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        boolean matches = mainClassCondition.check(TEST_MAIN_CLASS);
        // Then
        assertFalse(matches);
    }
    
    @Test
    public void nullConditionShouldNotMatch() {
        // Given
        SimpleProperty property = createTestProperty(TEST_MAIN_CLASS);
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        boolean matches = mainClassCondition.check(null);
        // Then
        assertFalse(matches);
    }
    
    @Test
    public void shouldNotMatchWhenMainClassCannotBeResolved() {
        // Given
        SimpleProperty property = createTestProperty();
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        boolean matches = mainClassCondition.check(null);
        // Then
        assertFalse(matches);
    }
    
    @Test
    public void shouldNotMatchWhenWhenJarFileCannotBeFound() {
        // Given
        SimpleProperty property = createTestProperty("non-existent-test-jar.jar");
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        boolean matches = mainClassCondition.check(null);
        // Then
        assertFalse(matches);
    }
    
    private static SimpleProperty createTestProperty() {
        return new SimpleProperty() {
            
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
    
    private static SimpleProperty createTestProperty(String testMainClass) {
        SimpleProperty testProperty = createTestProperty();
        testProperty.setProperty(SystemPropertyKey.SUN_JAVA_COMMAND.getKey(), testMainClass);
        return testProperty;
    }
    
}
