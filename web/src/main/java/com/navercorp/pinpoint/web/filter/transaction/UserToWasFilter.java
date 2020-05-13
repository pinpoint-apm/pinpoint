/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.filter.transaction;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.filter.URLPatternFilter;
import com.navercorp.pinpoint.web.filter.visitor.SpanVisitor;

import java.util.List;
import java.util.Objects;

/**
 * USER -> WAS
 */
public class UserToWasFilter implements Filter<LinkContext> {
    private final Filter<SpanBo> spanResponseConditionFilter;
    private final URLPatternFilter acceptURLFilter;

    public UserToWasFilter(Filter<SpanBo> spanResponseConditionFilter, URLPatternFilter acceptURLFilter) {
        this.spanResponseConditionFilter = Objects.requireNonNull(spanResponseConditionFilter, "spanResponseConditionFilter");
        this.acceptURLFilter = Objects.requireNonNull(acceptURLFilter, "acceptURLFilter");
    }

    public boolean include(LinkContext spanContainer) {
        final List<SpanBo> toNode = spanContainer.findToNode(acceptURLFilter);
        for (SpanBo span : toNode) {
            if (span.isRoot()) {
                if (spanResponseConditionFilter.include(span)) {
                    return SpanVisitor.ACCEPT;
                }
            }
        }
        return SpanVisitor.REJECT;
    }
}
