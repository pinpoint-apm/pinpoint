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
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactory;

import com.navercorp.pinpoint.io.request.ServerRequest;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.thrift.dto.TSpan;

import org.springframework.stereotype.Service;

/**
 * @author emeroad
 * @author netspider
 */
@Service
public class ThriftSpanHandler implements SimpleHandler {

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
        if (!(tbase instanceof TSpan)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        try {
            final TSpan tSpan = (TSpan) tbase;
            if (logger.isDebugEnabled()) {
                logger.debug("Received SPAN={}", tSpan);
            }

            final SpanBo spanBo = spanFactory.buildSpanBo(tSpan);
            traceService.insertSpan(spanBo);
        } catch (Exception e) {
            logger.warn("Span handle error. Caused:{}. Span:{}", e.getMessage(), tbase, e);
        }
    }
}