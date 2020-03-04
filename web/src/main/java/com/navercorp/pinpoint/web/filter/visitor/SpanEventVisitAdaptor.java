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

package com.navercorp.pinpoint.web.filter.visitor;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventVisitAdaptor implements SpanVisitor {
    private final SpanEventVisitor spanEventVisitor;

    public SpanEventVisitAdaptor(SpanEventVisitor spanEventVisitor) {
        this.spanEventVisitor = Objects.requireNonNull(spanEventVisitor, "spanEventVisitor");
    }

    @Override
    public boolean visit(SpanBo spanBo) {
        return SpanVisitor.REJECT;
    }

    @Override
    public boolean visit(SpanChunkBo spanChunkBo) {
        return SpanVisitor.REJECT;
    }

    @Override
    public boolean visit(SpanEventBo spanEventBo) {
        return spanEventVisitor.visit(spanEventBo);
    }
}
