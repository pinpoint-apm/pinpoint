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

import java.util.Objects;

/**
 * @author emeroad
 */
public class DefaultClassNameMatcher implements ClassNameMatcher {
    private final int order;
    private final String className;

    DefaultClassNameMatcher(String className) {
        this(LOWEST_PRECEDENCE, className);
    }

    DefaultClassNameMatcher(int order, String className) {
        this.order = order;
        this.className = Objects.requireNonNull(className, "className");
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DefaultClassNameMatcher that = (DefaultClassNameMatcher) o;

        return className.equals(that.className);

    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public String toString() {
        return "DefaultClassNameMatcher{" +
                "order=" + order +
                ", className='" + className + '\'' +
                '}';
    }
}
