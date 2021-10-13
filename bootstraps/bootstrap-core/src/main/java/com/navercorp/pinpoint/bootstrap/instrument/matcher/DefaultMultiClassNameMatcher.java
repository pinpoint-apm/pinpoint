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
import java.util.Objects;

/**
 * @author emeroad
 */
public class DefaultMultiClassNameMatcher implements MultiClassNameMatcher {
    private final int order;
    private final List<String> classNameList;

    DefaultMultiClassNameMatcher(List<String> classNameMatcherList) {
        this(LOWEST_PRECEDENCE, classNameMatcherList);
    }

    DefaultMultiClassNameMatcher(int order, List<String> classNameMatcherList) {
        Objects.requireNonNull(classNameMatcherList, "classNameMatcherList");
        this.order = order;
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

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public String toString() {
        return "DefaultMultiClassNameMatcher{" +
                "order=" + order +
                ", classNameList=" + classNameList +
                '}';
    }
}
