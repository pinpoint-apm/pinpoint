/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.instrument.matcher;

import java.util.Arrays;
import java.util.List;

/**
 * Matcher Utils
 * @author emeroad
 */
public final class Matchers {

    private Matchers() {
    }

    public static Matcher newClassNameMatcher(String classInternalName) {
        return new DefaultClassNameMatcher(classInternalName);
    }

    public static Matcher newMultiClassNameMatcher(List<String> classNameList) {
        if (classNameList == null) {
            throw new NullPointerException("classNameList must not be null");
        }
        return new DefaultMultiClassNameMatcher(classNameList);
    }

    public static Matcher newMultiClassNameMatcher(String... classNameList) {
        if (classNameList == null) {
            throw new NullPointerException("classNameList must not be null");
        }
        return new DefaultMultiClassNameMatcher(Arrays.asList(classNameList));
    }

}
