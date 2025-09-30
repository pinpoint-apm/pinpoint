/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.event;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;

import java.util.Map;

public class SimpleContextSupplier implements ContextSupplier {

    @Override
    public ContextData applyAsContext(SpanBo spanBo) {
        return new ContextData(spanBo.getApplicationName(), spanBo.getAgentId(), spanBo.getStartTime(), Map.of());
    }

    @Override
    public ContextData applyAsContext(SpanChunkBo spanChunkBo) {
        return new ContextData(spanChunkBo.getApplicationName(), spanChunkBo.getAgentId(), -1, Map.of());
    }

}
