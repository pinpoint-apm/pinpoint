/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.filter;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import org.apache.commons.collections.CollectionUtils;


import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventVisitor implements Filter<SpanBo> {

    private final Filter<SpanEventBo> spanEventFilter;

    public SpanEventVisitor(Filter<SpanEventBo> spanEventFilter) {
        this.spanEventFilter = Objects.requireNonNull(spanEventFilter, "spanEventFilter must not be null");
    }

    @Override
    public boolean include(List<SpanBo> spanBoList) {
        if (CollectionUtils.isEmpty(spanBoList)) {
            return REJECT;
        }

        for (SpanBo span : spanBoList) {
            final List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
            if (spanEventFilter.include(spanEventBoList)) {
                return ACCEPT;
            }
            final List<SpanChunkBo> asyncSpanChunkBoList = span.getAsyncSpanChunkBoList();
            if (CollectionUtils.isNotEmpty(asyncSpanChunkBoList)) {
                for (SpanChunkBo asyncSpanChunkBo : asyncSpanChunkBoList) {
                    final List<SpanEventBo> asyncSpanEventBoList = asyncSpanChunkBo.getSpanEventBoList();
                    if (spanEventFilter.include(asyncSpanEventBoList)) {
                        return ACCEPT;
                    }
                }
            }
        }
        return REJECT;
    }

}
