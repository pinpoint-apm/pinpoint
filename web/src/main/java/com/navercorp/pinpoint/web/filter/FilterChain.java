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

package com.navercorp.pinpoint.web.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author netspider
 *
 */
public class FilterChain<T> implements Filter<T> {

    private final Filter<T>[] filters;


    public FilterChain(List<Filter<T>> linkFilterList) {
        Objects.requireNonNull(linkFilterList, "linkFilterList");
        this.filters = linkFilterList.toArray(new Filter[0]);
    }


    @Override
    public boolean include(T transaction) {
        // FIXME how to improve performance without "for loop"
        for (Filter<T> filter : filters) {
            if (!filter.include(transaction)) {
                return REJECT;
            }
        }
        return ACCEPT;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FilterChain{");
        sb.append("filters=").append(Arrays.toString(filters));
        sb.append('}');
        return sb.toString();
    }
}
