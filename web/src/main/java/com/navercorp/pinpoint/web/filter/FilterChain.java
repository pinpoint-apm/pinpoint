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

import com.navercorp.pinpoint.common.bo.SpanBo;

/**
 * 
 * @author netspider
 * 
 */
public class FilterChain implements Filter {

    private final List<Filter> filterList;

    public FilterChain() {
        this.filterList = new ArrayList<Filter>();
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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filterList.size(); i++, sb.append("<br/>")) {
            sb.append(filterList.get(i).toString());
        }
        return sb.toString();
    }

    public void addAllFilter(List<LinkFilter> linkFilter) {
        if (linkFilter == null) {
            throw new NullPointerException("linkFilter must not be null");
        }
        this.filterList.addAll(linkFilter);
    }
}
