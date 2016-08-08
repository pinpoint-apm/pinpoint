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

import com.navercorp.pinpoint.common.server.bo.SpanBo;

/**
 *
 * @author netspider
 *
 */
public class FilterChain implements Filter {

    private final List<Filter> filterList = new ArrayList<>();

    public FilterChain() {
    }

    public FilterChain(List<LinkFilter> linkFilterList) {
        this.filterList.addAll(linkFilterList);
    }

    public void addFilter(Filter filter) {
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }
        this.filterList.add(filter);
    }

    @Override
    public boolean include(List<SpanBo> transaction) {
        // FIXME how to improve performance without "for loop"
        for (Filter filter : filterList) {
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
