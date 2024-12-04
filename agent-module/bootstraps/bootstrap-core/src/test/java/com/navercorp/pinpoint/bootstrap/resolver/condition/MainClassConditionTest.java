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

import com.navercorp.pinpoint.common.util.SystemPropertyKey;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author HyunGil Jeong
 */
public class MainClassConditionTest {

    private static final String TEST_MAIN_CLASS = "main.class.for.Test";

    @Test
    public void getValueShouldReturnBootstrapMainClass() {
        // Given
        Properties property = createTestProperty(TEST_MAIN_CLASS);
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        String expectedMainClass = mainClassCondition.getValue();
        // Then
        assertEquals(TEST_MAIN_CLASS, expectedMainClass);
    }

    @Test
    public void getValueShouldReturnEmptyStringWhenMainClassCannotBeResolved() {
        // Given
        Properties property = new Properties();
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        String expectedMainClass = mainClassCondition.getValue();
        // Then
        assertEquals("", expectedMainClass);
    }

    @Test
    public void testMatch() {
        // Given
        Properties property = createTestProperty(TEST_MAIN_CLASS);
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
        Properties property = createTestProperty(givenBootstrapMainClass);
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        boolean matches = mainClassCondition.check(TEST_MAIN_CLASS);
        // Then
        assertFalse(matches);
    }

    @Test
    public void nullConditionShouldNotMatch() {
        // Given
        Properties property = createTestProperty(TEST_MAIN_CLASS);
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        boolean matches = mainClassCondition.check(null);
        // Then
        assertFalse(matches);
    }

    @Test
    public void shouldNotMatchWhenMainClassCannotBeResolved() {
        // Given
        Properties property = new Properties();
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        boolean matches = mainClassCondition.check(null);
        // Then
        assertFalse(matches);
    }

    @Test
    public void shouldNotMatchWhenWhenJarFileCannotBeFound() {
        // Given
        Properties property = createTestProperty("non-existent-test-jar.jar");
        MainClassCondition mainClassCondition = new MainClassCondition(property);
        // When
        boolean matches = mainClassCondition.check(null);
        // Then
        assertFalse(matches);
    }


    private static Properties createTestProperty(String testMainClass) {
        Properties testProperty = new Properties();
        testProperty.setProperty(SystemPropertyKey.SUN_JAVA_COMMAND.getKey(), testMainClass);
        return testProperty;
    }

}
