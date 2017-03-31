/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.common.util;

/**
 * @author Jongho Moon
 * @deprecated Since 1.7.0. Use {@link com.navercorp.pinpoint.common.util.Assert}
 */
@Deprecated
public final class Asserts {
    private Asserts() {}

    public static void notNull(Object value, String name) {
        if (value == null) {
            // String concat is not recommend
            throw new NullPointerException(name + " can not be null");
        }
    }
}