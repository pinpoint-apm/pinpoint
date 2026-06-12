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

import java.util.StringJoiner;

public final class ErrorCategorySet {

    private static final String SEPARATOR = ", ";

    private static final ErrorCategory[] CATEGORIES = ErrorCategory.values();

    private static final ErrorCategorySet EMPTY = new ErrorCategorySet(0);

    public static ErrorCategorySet empty() {
        return EMPTY;
    }

    private final int bitMask;

    public static ErrorCategorySet of(int bitMask) {
        if (bitMask == 0) {
            return EMPTY;
        }
        return new ErrorCategorySet(bitMask);
    }

    private ErrorCategorySet(int bitMask) {
        this.bitMask = bitMask;
    }

    public boolean isEmpty() {
        return bitMask == 0;
    }

    public boolean contains(ErrorCategory category) {
        return (bitMask & category.getBitMask()) != 0;
    }

    public String format() {
        if (bitMask == 0) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(SEPARATOR);
        for (ErrorCategory category : CATEGORIES) {
            if ((bitMask & category.getBitMask()) != 0) {
                joiner.add(category.name());
            }
        }
        return joiner.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ErrorCategorySet)) {
            return false;
        }
        ErrorCategorySet that = (ErrorCategorySet) o;
        return bitMask == that.bitMask;
    }

    @Override
    public int hashCode() {
        return bitMask;
    }

    @Override
    public String toString() {
        return format();
    }
}
