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

import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class DefaultMultiClassNameMatcher implements MultiClassNameMatcher {

    private final List<String> classNameList;

    DefaultMultiClassNameMatcher(List<String> classNameMatcherList) {
        if (classNameMatcherList == null) {
            throw new NullPointerException("classNameMatcherList must not be null");
        }
        this.classNameList = Collections.unmodifiableList(classNameMatcherList);
    }

    @Override
    public List<String> getClassNames() {
        return classNameList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultMultiClassNameMatcher that = (DefaultMultiClassNameMatcher) o;

        return classNameList.equals(that.classNameList);

    }

    @Override
    public int hashCode() {
        return classNameList.hashCode();
    }
}
