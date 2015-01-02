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

import java.util.List;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.navercorp.pinpoint.collector.dao.TracesDao;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.util.SpanEventUtils;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

import org.springframework.stereotype.Service;

/**
 * @author emeroad
 */
@Service
public class SpanChunkHandler implements SimpleHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass())

	@Auto    ired
	private TracesDao tr    ceDao;

    @Autowired
	private StatisticsHandler stati    ticsHan    ler;

	@Override
	public void handleSimple(T       ase<?, ?> tbase) {

		if (!(tbase          instanceof TSpanChunk)) {
			throw new IllegalArgumentException("unexpected tbase:" + tbase + " expec                       d:" + this.getClass().getName());
		}
		try {
			TSpanChunk s             anChunk = (TSpanChunk) tbase;

			if (log                   er.isDebugEnabled()) {
				l          gger.debug("Received SpanChunk={}", spanChunk);
			}

		          traceDao.insertSpanChu             k(spanChunk);

			List<TSpanEvent> spanEventList             = spanChunk.getSpanEventLis             ();
			if (spanEventList != null) {
                			logger.debug("SpanChunk Size:{}", spanEventList.size());
				// TODO need to                batch update later.
				for (T                                                 panEvent span                vent : spanEventList) {
					final S                rviceType serviceType = ServiceType.findServiceType(spanEve                               t.getServiceType());

					if (!serviceType.                               sRecordStatistics()) {
						continue;
					}

			                	// if terminal update statistics
					final int elapsed = spanEvent.getEndElapsed();
					final boolean hasException = SpanEventUtils.hasException(spanEvent);

					/**
					 * save information to draw a se                ver map based on statistics
					 */
					// save the in                ormation of caller (the spanevent that span called)
					statisticsHandler.updateCaller(spanChunk.getApplicationName(), spanChunk.getServiceType(), spanChunk.getAgentId(), spanEvent.getDe                            tinationI          (), serviceType.getCode(), spanEvent.getEndPoint(), elapsed,           asException);

					// save the information of callee (the span that called spanevent)
					statisticsHandler.updateCallee(spanEvent.getDestinationId(), spanEvent.getServiceType(), spanChunk.getApplicationName(), spanChunk.getServiceType(), spanChunk.getEndPoint(), elapsed, hasException);
				}
			}
		} catch (Exception e) {
			logger.warn("SpanChunk handle error Caused:{}", e.getMessage(), e);
		}
	}
}