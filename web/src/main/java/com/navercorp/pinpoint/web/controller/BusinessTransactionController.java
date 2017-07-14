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

package com.navercorp.pinpoint.web.controller;

import java.util.List;

import com.navercorp.pinpoint.common.util.DefaultSqlParser;
import com.navercorp.pinpoint.common.util.OutputParameterParser;
import com.navercorp.pinpoint.common.util.SqlParser;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.web.view.TransactionInfoViewModel;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.service.FilteredMapService;
import com.navercorp.pinpoint.web.service.SpanResult;
import com.navercorp.pinpoint.web.service.SpanService;
import com.navercorp.pinpoint.web.service.TransactionInfoService;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;

/**
 * @author emeroad
 * @author jaehong.kim
 */
@Controller
public class BusinessTransactionController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SpanService spanService;

    @Autowired
    private TransactionInfoService transactionInfoService;

    @Autowired
    private FilteredMapService filteredMapService;

    @Value("#{pinpointWebProps['log.enable'] ?: false}")
    private boolean logLinkEnable;

    @Value("#{pinpointWebProps['log.button.name'] ?: ''}")
    private String logButtonName;

    @Value("#{pinpointWebProps['log.page.url'] ?: ''}")
    private String logPageUrl;

    @Value("#{pinpointWebProps['log.button.disable.message'] ?: ''}")
    private String disableButtonMessage;

    private SqlParser sqlParser = new DefaultSqlParser();
    private OutputParameterParser parameterParser = new OutputParameterParser();

    /**
     * info lookup for a selected transaction
     *
     * @param traceIdParam
     * @param focusTimestamp
     * @return
     */
    @RequestMapping(value = "/transactionInfo", method = RequestMethod.GET)
    @ResponseBody
    public TransactionInfoViewModel transactionInfo(@RequestParam("traceId") String traceIdParam,
                                                    @RequestParam(value = "focusTimestamp", required = false, defaultValue = "0") long focusTimestamp,
                                                    @RequestParam(value = "agentId", required = false) String agentId,
                                                    @RequestParam(value = "spanId", required = false, defaultValue = "-1") long spanId,
                                                    @RequestParam(value = "v", required = false, defaultValue = "0") int viewVersion) {
        logger.debug("GET /transactionInfo params {traceId={}, focusTimestamp={}, agentId={}, spanId={}, v={}}", traceIdParam, focusTimestamp, agentId, spanId, viewVersion);

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(traceIdParam);

        // select spans
        final SpanResult spanResult = this.spanService.selectSpan(transactionId, focusTimestamp);
        final CallTreeIterator callTreeIterator = spanResult.getCallTree();

        // application map
        ApplicationMap map = filteredMapService.selectApplicationMap(transactionId, viewVersion);
        RecordSet recordSet = this.transactionInfoService.createRecordSet(callTreeIterator, focusTimestamp, agentId, spanId);

        TransactionInfoViewModel result = new TransactionInfoViewModel(transactionId, map.getNodes(), map.getLinks(), recordSet, spanResult.getCompleteTypeString(), logLinkEnable, logButtonName, logPageUrl, disableButtonMessage);
        return result;
    }

    @RequestMapping(value = "/sqlBind", method = RequestMethod.POST)
    @ResponseBody
    public String sqlBind(@RequestParam("sql") String sql,
                          @RequestParam("bind") String bind) {
        if (logger.isDebugEnabled()) {
            logger.debug("GET /sqlBind params {sql={}, bind={}}", sql, bind);
        }

        if (sql == null) {
            return "";
        }

        final List<String> bindValues = parameterParser.parseOutputParameter(bind);
        final String combineSql = sqlParser.combineBindValues(sql, bindValues);
        if(logger.isDebugEnabled()) {
            logger.debug("Combine SQL. sql={}", combineSql);
        }

        return StringEscapeUtils.escapeHtml4(combineSql);
    }
}