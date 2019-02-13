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

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.LocalAsyncIdBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

import java.util.Objects;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class AsyncSpanEventAlign extends SpanEventAlign {

    private final LocalAsyncIdBo localAsyncIdBo;


    public AsyncSpanEventAlign(SpanBo spanBo, SpanEventBo spanEventBo, LocalAsyncIdBo localAsyncIdBo) {
        super(spanBo, spanEventBo);
        this.localAsyncIdBo = Objects.requireNonNull(localAsyncIdBo, "localAsyncIdBo must not be null");
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public boolean isAsyncFirst() {
        return getSpanEventBo().getSequence() == 0;
    }

    @Override
    public int getAsyncId() {
        return localAsyncIdBo.getAsyncId();
    }


}