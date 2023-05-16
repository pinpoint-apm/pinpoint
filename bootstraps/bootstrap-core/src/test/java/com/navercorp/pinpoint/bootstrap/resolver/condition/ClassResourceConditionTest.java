/*
 * Copyright 2018 NAVER Corp.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author HyunGil Jeong
 */
public class ClassResourceConditionTest {

    @Test
    public void testMatch() {
        // Given
        String resource = String.class.getName();
        // When
        boolean match = ClassResourceCondition.INSTANCE.check(resource);
        // Then
        Assertions.assertTrue(match);
    }

    @Test
    public void testNoMatch() {
        // Given
        String nonExistingResource = "nonExistingClassNameForPinpointTest";
        // When
        boolean match = ClassResourceCondition.INSTANCE.check(nonExistingResource);
        // Then
        Assertions.assertFalse(match);
    }

    @Test
    public void testNullParameter() {
        boolean match = ClassResourceCondition.INSTANCE.check(null);
        Assertions.assertFalse(match);
    }

    @Test
    public void testEmptyResource() {
        boolean match = ClassResourceCondition.INSTANCE.check("");
        Assertions.assertFalse(match);
    }
}
