/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.common.server.util;

import java.util.EnumSet;
import java.util.function.Function;

/**
 * @author intr3p1d
 */
public class EnumGetter<E extends Enum<E>> {
    private final EnumSet<E> set;

    public EnumGetter(Class<E> eClass) {
        this.set = EnumSet.allOf(eClass);
    }

    public E fromValueWithFallBack(
            Function<E, String> getter,
            String value,
            E defaultEnum
    ) {
        E ele = fromValue(getter, value);
        if (ele == null) {
            return defaultEnum;
        }
        return ele;
    }

    public E fromValue(
            Function<E, String> getter,
            String value
    ) {
        for (E ele : set) {
            if (getter.apply(ele).equalsIgnoreCase(value)) {
                return ele;
            }
        }
        return null;
    }
}
