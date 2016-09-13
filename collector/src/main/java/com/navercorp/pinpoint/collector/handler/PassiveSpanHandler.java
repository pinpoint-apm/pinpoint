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

package com.navercorp.pinpoint.collector.handler;

import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.common.server.bo.PassiveSpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanFactory;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.thrift.dto.TPassiveSpan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author Peter Chen
 */
@Service
public class PassiveSpanHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("hbaseTraceDaoFactory")
    private TraceDao traceDao;

    @Autowired
    private SpanFactory spanFactory;

    public void handleSimple(TBase<?, ?> tbase) {

        if (!(tbase instanceof TPassiveSpan)) {
            throw new IllegalArgumentException("unexpected tbase:" + tbase + " expected:" + this.getClass().getName());
        }

        try {
            final TPassiveSpan passiveSpan = (TPassiveSpan) tbase;
            if (logger.isDebugEnabled()) {
                logger.debug("Received PassiveSPAN={}", passiveSpan);
            }

            final PassiveSpanBo passiveSpanBo = spanFactory.buildPassiveSpanBo(passiveSpan);

            traceDao.insert(passiveSpanBo);

        } catch (Exception e) {
            logger.warn("Span handle error. Caused:{}. Span:{}",e.getMessage(), tbase, e);
        }
    }
}
