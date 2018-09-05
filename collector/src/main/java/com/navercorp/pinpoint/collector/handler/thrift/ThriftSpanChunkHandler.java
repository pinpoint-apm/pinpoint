/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.handler.thrift;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactory;

import com.navercorp.pinpoint.io.request.ServerRequest;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.thrift.dto.TSpanChunk;

import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public class ThriftSpanChunkHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TraceService traceService;

    @Autowired
    private SpanFactory spanFactory;

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (data instanceof TBase<?, ?>) {
            handleSimple((TBase<?, ?>) data);
        } else {
            throw new UnsupportedOperationException("data is not support type : " + data);
        }
    }

    private void handleSimple(TBase<?, ?> tbase) {

        try {
            final SpanChunkBo spanChunkBo = newSpanChunkBo(tbase);
            this.traceService.insertSpanChunk(spanChunkBo);
        } catch (Exception e) {
            logger.warn("SpanChunk handle error Caused:{}", e.getMessage(), e);
        }
    }

    private SpanChunkBo newSpanChunkBo(TBase<?, ?> tbase) {
        if (!(tbase instanceof TSpanChunk)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        final TSpanChunk tSpanChunk = (TSpanChunk) tbase;
        if (logger.isDebugEnabled()) {
            logger.debug("Received SpanChunk={}", tbase);
        }

        return this.spanFactory.buildSpanChunkBo(tSpanChunk);
    }
}