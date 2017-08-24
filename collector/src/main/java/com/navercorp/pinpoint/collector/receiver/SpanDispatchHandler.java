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

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import org.apache.thrift.TBase;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class SpanDispatchHandler extends AbstractDispatchHandler {

    @Autowired()
    @Qualifier("spanHandler")
    private SimpleHandler spanDataHandler;

    @Autowired()
    @Qualifier("spanChunkHandler")
    private SimpleHandler spanChunkHandler;

    public SpanDispatchHandler() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }


    @Override
    protected List<SimpleHandler> getSimpleHandler(TBase<?, ?> tBase) {
        List<SimpleHandler> simpleHandlerList = new ArrayList<>();

        if (tBase instanceof TSpan) {
            simpleHandlerList.add(spanDataHandler);
        }
        if (tBase instanceof TSpanChunk) {
            simpleHandlerList.add(spanChunkHandler);
        }

        return simpleHandlerList;
    }

}
