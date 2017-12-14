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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.resolver.condition.ClassResourceCondition;
import com.navercorp.pinpoint.bootstrap.resolver.condition.MainClassCondition;
import com.navercorp.pinpoint.bootstrap.resolver.condition.PropertyCondition;
import com.navercorp.pinpoint.common.util.SimpleProperty;
import com.navercorp.pinpoint.common.util.SystemPropertyKey;

/**
 * @author HyunGil Jeong
 */
public class ConditionProviderTest {

    private static final String TEST_MAIN_CLASS = "test.main.class";
    private static final String TEST_PROPERTY_KEY = "test.property.key";
    private static final String TEST_PROPERTY_VALUE = "test.property.value";
    
    private ConditionProvider conditionProvider;
    
    @Before
    public void setUp() throws Exception {
        this.conditionProvider = new ConditionProvider(
                new MainClassCondition(PROPERTY_FOR_TEST),
                new PropertyCondition(PROPERTY_FOR_TEST),
                new ClassResourceCondition()
        );
    }
    
    @Test
    public void getMainClassShouldReturnApplicationMainClass() {
        // Given
        final String expectedMainClass = TEST_MAIN_CLASS;
        // When
        String actualMainClass = this.conditionProvider.getMainClass(); 
        // Then
        assertEquals(expectedMainClass, actualMainClass);
    }

    @Test
    public void checkMainClassShouldReturnTrueForMatchingMainClass() {
        // Given
        final String matchingMainClass = TEST_MAIN_CLASS;
        // When
        boolean matches = this.conditionProvider.checkMainClass(matchingMainClass);
        // Then
        assertTrue(matches);
    }

    @Test
    public void checkMainClassShouldReturnTrueForMatchingMainClasses() {
        // Given
        final String matchingMainClass = TEST_MAIN_CLASS;
        final String someOtherMainClass = "some.other.main.class";
        final List<String> mainClassCandidates = Arrays.asList(
                matchingMainClass,
                someOtherMainClass
        );
        // When
        boolean matches = this.conditionProvider.checkMainClass(mainClassCandidates);
        // Then
        assertTrue(matches);
    }

    @Test
    public void checkMainClassShouldReturnFalseForNonMatchingMainClass() {
        // Given
        final String someOtherMainClass = "some.other.main.class";
        // When
        boolean matches = this.conditionProvider.checkMainClass(someOtherMainClass);
        // Then
        assertFalse(matches);
    }

    @Test
    public void checkMainClassShouldReturnFalseForNonMatchingMainClasses() {
        // Given
        final String someOtherMainClass = "some.other.main.class";
        final String someOtherMainClass2 = "some.other.main.class2";
        final List<String> mainClassCandidates = Arrays.asList(
                someOtherMainClass,
                someOtherMainClass2
        );
        // When
        boolean matches = this.conditionProvider.checkMainClass(mainClassCandidates);
        // Then
        assertFalse(matches);
    }

    @Test
    public void checkMainClassShouldReturnFalseForEmptyString() {
        // Given
        // When
        boolean matches = this.conditionProvider.checkMainClass("");
        // Then
        assertFalse(matches);
    }

    @Test
    public void checkMainClassShouldReturnFalseForEmptyList() {
        // Given
        // When
        boolean matches = this.conditionProvider.checkMainClass(Collections.<String>emptyList());
        // Then
        assertFalse(matches);
    }
    
    @Test
    public void getSystemPropertyValueShouldReturnCorrectValue() {
        // Given
        final String expectedValue = TEST_PROPERTY_VALUE;
        // When
        String actualValue = this.conditionProvider.getSystemPropertyValue(TEST_PROPERTY_KEY);
        // Then
        assertEquals(expectedValue, actualValue);
    }
    
    @Test
    public void getSystemPropertyValueShouldReturnEmptyStringForNullKey() {
        // Given
        final String expectedValue = "";
        // When
        String actualValue = this.conditionProvider.getSystemPropertyValue(null);
        // Then
        assertEquals(expectedValue, actualValue);
    }
    
    @Test
    public void getSystemPropertyValueShouldReturnEmptyStringForEmptyKey() {
        // Given
        final String expectedValue = "";
        // When
        String actualValue = this.conditionProvider.getSystemPropertyValue("");
        // Then
        assertEquals(expectedValue, actualValue);
    }
    
    @Test
    public void checkSystemPropertyShouldReturnTrueForExistingKeys() {
        // Given
        // When
        boolean exists = this.conditionProvider.checkSystemProperty(TEST_PROPERTY_KEY);
        // Then
        assertTrue(exists);
    }
    
    @Test
    public void checkSystemPropertyShouldReturnFalseForNonExistingKeys() {
        // Given
        final String nonExistingKey = "some.other.property.key";
        // When
        boolean exists = this.conditionProvider.checkSystemProperty(nonExistingKey);
        // Then
        assertFalse(exists);
    }
    
    @Test
    public void checkSystemPropertyShouldReturnFalseForNullKeys() {
        // Given
        // When
        boolean exists = this.conditionProvider.checkSystemProperty(null);
        // Then
        assertFalse(exists);
    }
    
    @Test
    public void checkSystemPropertyShouldReturnFalseForEmptyKeys() {
        // Given
        // When
        boolean exists = this.conditionProvider.checkSystemProperty("");
        // Then
        assertFalse(exists);
    }
    
    private static final SimpleProperty PROPERTY_FOR_TEST = new SimpleProperty() {
        
        @SuppressWarnings("serial")
        private final Map<String, String> properties = new HashMap<String, String>() {{
            put(SystemPropertyKey.SUN_JAVA_COMMAND.getKey(), TEST_MAIN_CLASS);
            put(TEST_PROPERTY_KEY, TEST_PROPERTY_VALUE);
        }};
                
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
            }
            return defaultValue;
        }
        
    };

}
