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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ErrorCategoryResolver {

    private static final ErrorCategory[] DISPLAY_CANDIDATE = displayCandidate();

    private static ErrorCategory[] displayCandidate() {
        ErrorCategory[] values = ErrorCategory.values();
        List<ErrorCategory> list = new ArrayList<>(Arrays.asList(values));
        list.remove(ErrorCategory.UNKNOWN);
        return list.toArray(new ErrorCategory[0]);
    }

    public Set<ErrorCategory> resolve(int errorCode) {
        Set<ErrorCategory> flagged = EnumSet.noneOf(ErrorCategory.class);
        for (ErrorCategory category : DISPLAY_CANDIDATE) {
            if ((errorCode & category.getBitMask()) != 0) {
                flagged.add(category);
            }
        }
        return flagged;
    }
}
