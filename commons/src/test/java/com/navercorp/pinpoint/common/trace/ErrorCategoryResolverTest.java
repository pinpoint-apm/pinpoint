/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.trace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ErrorCategoryResolverTest {

    @Test
    public void resolveShouldReturnEmptySetForUnknownErrorCode() {
        int unknownErrorCode = ErrorCategory.UNKNOWN.getBitMask();
        ErrorCategorySet result = new ErrorCategoryResolver().resolve(unknownErrorCode);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void resolveShouldReturnSingleCategoryForMatchingErrorCode() {
        int networkErrorCode = ErrorCategory.EXCEPTION.getBitMask();
        ErrorCategorySet result = new ErrorCategoryResolver().resolve(networkErrorCode);
        Assertions.assertEquals(ErrorCategorySet.of(ErrorCategory.EXCEPTION.getBitMask()), result);
    }

    @Test
    public void resolveShouldReturnEmptySetForZeroErrorCode() {
        int zeroErrorCode = 0;
        ErrorCategorySet result = new ErrorCategoryResolver().resolve(zeroErrorCode);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void containsShouldReturnTrueForResolvedCategories() {
        int errorCode = ErrorCategory.EXCEPTION.getBitMask() | ErrorCategory.SQL.getBitMask();
        ErrorCategorySet result = new ErrorCategoryResolver().resolve(errorCode);
        Assertions.assertTrue(result.contains(ErrorCategory.EXCEPTION));
        Assertions.assertTrue(result.contains(ErrorCategory.SQL));
        Assertions.assertFalse(result.contains(ErrorCategory.HTTP_STATUS));
    }

    @Test
    public void containsShouldReturnFalseForEmptySet() {
        ErrorCategorySet result = new ErrorCategoryResolver().resolve(0);
        Assertions.assertFalse(result.contains(ErrorCategory.EXCEPTION));
        Assertions.assertFalse(result.contains(ErrorCategory.HTTP_STATUS));
        Assertions.assertFalse(result.contains(ErrorCategory.SQL));
    }

    @Test
    public void containsShouldReturnFalseForUnknownCategory() {
        int errorCode = ErrorCategory.UNKNOWN.getBitMask() | ErrorCategory.EXCEPTION.getBitMask();
        ErrorCategorySet result = new ErrorCategoryResolver().resolve(errorCode);
        Assertions.assertFalse(result.contains(ErrorCategory.UNKNOWN));
        Assertions.assertTrue(result.contains(ErrorCategory.EXCEPTION));
    }


}