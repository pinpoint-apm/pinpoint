/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GetTraceInfoParser {

    public static final String APPLICATION_NAME = "ApplicationName";

    public static final String PREFIX_TRANSACTION_ID = "I";
    public static final String PREFIX_TIME = "T";
    public static final String PREFIX_RESPONSE_TIME = "R";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<GetTraceInfo> parse(Map<String, String> requestParam) {
        final String applicationName = requestParam.get(APPLICATION_NAME);

        List<GetTraceInfo> getTraceInfoList = new ArrayList<>();
        int index = 0;
        while (true) {
            final String transactionId = requestParam.get(PREFIX_TRANSACTION_ID + index);
            final String time = requestParam.get(PREFIX_TIME + index);
            final String responseTime = requestParam.get(PREFIX_RESPONSE_TIME + index);

            if (transactionId == null || time == null || responseTime == null) {
                break;
            }

            TransactionId traceId = TransactionIdUtils.parseTransactionId(transactionId);
            SpanHint spanHint = new SpanHint(Long.parseLong(time), Integer.parseInt(responseTime), applicationName);

            final GetTraceInfo getTraceInfo = new GetTraceInfo(traceId, spanHint);
            getTraceInfoList.add(getTraceInfo);
            index++;
        }
        logger.debug("query:{}", getTraceInfoList);
        return getTraceInfoList;
    }
}
