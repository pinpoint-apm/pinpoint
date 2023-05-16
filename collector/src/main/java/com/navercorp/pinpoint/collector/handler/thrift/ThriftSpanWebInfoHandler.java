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
import com.navercorp.pinpoint.common.server.bo.SpanWebInfoBo;
import com.navercorp.pinpoint.common.server.bo.thrift.SpanFactory;
import com.navercorp.pinpoint.io.request.ServerRequest;

import com.navercorp.pinpoint.thrift.dto.TSpanWebInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author emeroad
 */
@Service
public class ThriftSpanWebInfoHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TraceService traceService;

    private final SpanFactory spanFactory;


    public ThriftSpanWebInfoHandler(TraceService traceService, SpanFactory spanFactory) {
        this.traceService = Objects.requireNonNull(traceService, "traceService");
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");
    }

    @Override
    public void handleSimple(ServerRequest serverRequest) {
        final Object data = serverRequest.getData();
        if (logger.isDebugEnabled()) {
            logger.debug("Handle simple data={}", data);
        }
        if (data instanceof TSpanWebInfo) {
            handleSpanWebInfo((TSpanWebInfo) data);
        } else {
            throw new UnsupportedOperationException("data is not support type : " + data);
        }
    }

    private void handleSpanWebInfo(TSpanWebInfo tbase) {
        try {
            final SpanWebInfoBo spanWebInfoBo = this.spanFactory.buildSpanWebInfoBo(tbase);

            logger.info("开始处理spanWebInfoBo数据，可写入hbase或发kafka做处理。此处暂不处理。");

        } catch (Exception e) {
            logger.warn("Failed to handle SpanWebInfo={}, Caused={}", tbase, e.getMessage(), e);
        }
    }
}