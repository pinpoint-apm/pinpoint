/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.handler;

import java.util.List;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanAndSpanChunkList;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;

/**
 * @author Taejin Koo
 */
@Service
public class SpanAndSpanChunkListHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("spanChunkHandler")
    private SimpleHandler spanChunkHandler;

    @Autowired
    @Qualifier("spanHandler")
    private SimpleHandler spanHandler;


    public void handleSimple(TBase<?, ?> tbase) {
        if (!(tbase instanceof TSpanAndSpanChunkList)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        TSpanAndSpanChunkList spanAndSpanChunkList = (TSpanAndSpanChunkList) tbase;
        if (logger.isDebugEnabled()) {
            logger.debug("Received SpanAndSpanChunkList={}", spanAndSpanChunkList);
        }

        if (spanAndSpanChunkList.isSetSpanChunkList()) {
            List<TSpanChunk> spanChunkList = spanAndSpanChunkList.getSpanChunkList();
            for (TSpanChunk spanChunk : spanChunkList) {
                spanChunkHandler.handleSimple(spanChunk);
            }
        }

        if (spanAndSpanChunkList.isSetSpanList()) {
            List<TSpan> spanList = spanAndSpanChunkList.getSpanList();
            for (TSpan span : spanList) {
                spanHandler.handleSimple(span);
            }
        }
    }

}

