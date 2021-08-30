/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.test.util;

import java.util.Formatter;
import java.util.Objects;

public class AssertionErrorBuilder {
    private final String message;
    private final Object expected;
    private final Object actual;

    private boolean comparison;
    private Object expectedObject;
    private Object actualObject;

    public AssertionErrorBuilder(String message, Object expected, Object actual) {
        this.message = Objects.requireNonNull(message, "message");
        this.expected = expected;
        this.actual = actual;
    }

    public void setComparison(Object expectedObject, Object actualObject) {
        this.comparison = true;
        this.expectedObject = expectedObject;
        this.actualObject = actualObject;
    }

    public String toString() {
        Formatter format = new Formatter();
        if (!this.comparison) {
            format.format("%s expected:<%s> but was:<%s>", this.message, this.expected, this.actual);
        } else {
            format.format("%s expected:[%s] but was:[%s]", this.message, this.expected, this.actual);
            format.format(" expected:<%s> but was:<%s>", this.expectedObject, this.actualObject);
        }
        return format.toString();
    }

    public void throwAssertionError() throws AssertionError {
        throw new AssertionError(toString());
    }
}
