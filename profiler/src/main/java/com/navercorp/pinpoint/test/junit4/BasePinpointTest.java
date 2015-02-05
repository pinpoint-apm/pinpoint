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

package com.navercorp.pinpoint.test.junit4;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TBase;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.test.PeekableDataSender;

/**
 * @author hyungil.jeong
 */
@RunWith(value = PinpointJUnit4ClassRunner.class)
public abstract class BasePinpointTest {
//    private ThreadLocal<PeekableDataSender<? extends TBase<?, ?>>> traceHolder = new ThreadLocal<PeekableDataSender<? extends TBase<?, ?>>>();
    private PeekableDataSender<? extends TBase<?, ?>> traceHolder;
    private ThreadLocal<ServerMetaDataHolder> serverMetaDataHolder = new ThreadLocal<ServerMetaDataHolder>();

    protected final List<SpanEventBo> getCurrentSpanEvents() {
        List<SpanEventBo> spanEvents = new ArrayList<SpanEventBo>();
        for (TBase<?, ?> span : this.traceHolder) {
            if (span instanceof SpanEvent) {
                SpanEvent spanEvent = (SpanEvent)span;
                spanEvents.add(new SpanEventBo(spanEvent.getSpan(), spanEvent));
            }
        }
        return spanEvents;
    }

    protected final List<SpanBo> getCurrentRootSpans() {
        List<SpanBo> rootSpans = new ArrayList<SpanBo>();
        for (TBase<?, ?> span : this.traceHolder) {
            if (span instanceof Span) {
                rootSpans.add(new SpanBo((Span)span));
            }
        }
        return rootSpans;
    }
    
    protected final ServerMetaData getServerMetaData() {
        return this.serverMetaDataHolder.get().getServerMetaData();
    }

    final void setCurrentHolder(PeekableDataSender<? extends TBase<?, ?>> dataSender) {
        traceHolder = dataSender;
    }
    
    final void setServerMetaDataHolder(ServerMetaDataHolder metaDataHolder) {
        this.serverMetaDataHolder.set(metaDataHolder);
    }
}
