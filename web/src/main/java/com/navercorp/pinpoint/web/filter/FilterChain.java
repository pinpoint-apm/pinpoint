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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author netspider
 *
 */
public class FilterChain<T> implements Filter<T> {

    private final List<Filter<T>> filterList = new ArrayList<>();

    public FilterChain() {
    }

    public FilterChain(List<Filter<T>> linkFilterList) {
        this.filterList.addAll(linkFilterList);
    }

    public void addFilter(Filter<T> filter) {
        if (filter == null) {
            throw new NullPointerException("filter");
        }
        this.filterList.add(filter);
    }

    @Override
    public boolean include(List<T> transaction) {
        // FIXME how to improve performance without "for loop"
        for (Filter<T> filter : filterList) {
            if (!filter.include(transaction)) {
                return REJECT;
            }
        }
        return ACCEPT;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FilterChain{");
        sb.append("filterList=").append(filterList);
        sb.append('}');
        return sb.toString();
    }
}
