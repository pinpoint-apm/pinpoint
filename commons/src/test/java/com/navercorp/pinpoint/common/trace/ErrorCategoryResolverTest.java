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

import java.util.EnumSet;

class ErrorCategoryResolverTest {

    @Test
    public void resolveShouldReturnEmptySetForUnknownErrorCode() {
        int unknownErrorCode = ErrorCategory.UNKNOWN.getBitMask();
        EnumSet<ErrorCategory> result = new ErrorCategoryResolver().resolve(unknownErrorCode);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void resolveShouldReturnSingleCategoryForMatchingErrorCode() {
        int networkErrorCode = ErrorCategory.EXCEPTION.getBitMask();
        EnumSet<ErrorCategory> result = new ErrorCategoryResolver().resolve(networkErrorCode);
        Assertions.assertEquals(EnumSet.of(ErrorCategory.EXCEPTION), result);
    }

    @Test
    public void resolveShouldReturnEmptySetForZeroErrorCode() {
        int zeroErrorCode = 0;
        EnumSet<ErrorCategory> result = new ErrorCategoryResolver().resolve(zeroErrorCode);
        Assertions.assertTrue(result.isEmpty());
    }


}