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

package com.navercorp.pinpoint.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.navercorp.pinpoint.common.bo.AnnotationBo;
import com.navercorp.pinpoint.common.bo.Span;
import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.common.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.calltree.span.CallTreeNode;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.vo.BusinessTransactions;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordFactory;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author jaehong.kim
 */
@Service
public class TransactionInfoServiceImpl implements TransactionInfoService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TraceDao traceDao;

    @Autowired
    private AnnotationKeyMatcherService annotationKeyMatcherService;

    @Autowired
    private ServiceTypeRegistryService registry;

    @Autowired
    private AnnotationKeyRegistryService annotationKeyRegistryService;

    // Temporarily disabled Because We need to solve authentication problem inter system.
    // @Value("#{pinpointWebProps['log.enable'] ?: false}")
    // private boolean logLinkEnable;

    // @Value("#{pinpointWebProps['log.button.name'] ?: ''}")
    // private String logButtonName;

    // @Value("#{pinpointWebProps['log.page.url'] ?: ''}")
    // private String logPageUrl;

    @Override
    public BusinessTransactions selectBusinessTransactions(List<TransactionId> transactionIdList, String applicationName, Range range, Filter filter) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }
        if (range == null) {
            // TODO range is not used - check the logic again
            throw new NullPointerException("range must not be null");
        }

        List<List<SpanBo>> traceList;

        if (filter == Filter.NONE) {
            traceList = this.traceDao.selectSpans(transactionIdList);
        } else {
            traceList = this.traceDao.selectAllSpans(transactionIdList);
        }

        BusinessTransactions businessTransactions = new BusinessTransactions();
        for (List<SpanBo> trace : traceList) {
            if (!filter.include(trace)) {
                continue;
            }

            for (SpanBo spanBo : trace) {
                // show application's incoming requests
                if (applicationName.equals(spanBo.getApplicationId())) {
                    businessTransactions.add(spanBo);
                }
            }
        }

        return businessTransactions;
    }

    @Override
    public RecordSet createRecordSet(CallTreeIterator callTreeIterator, long focusTimestamp) {
        if (callTreeIterator == null) {
            throw new NullPointerException("callTreeIterator must not be null");
        }

        RecordSet recordSet = new RecordSet();
        final List<SpanAlign> spanAlignList = callTreeIterator.values();

        // finds and marks the focusTimestamp.
        // focusTimestamp is needed to determine which span to use as reference when there are more than 2 spans making up a transaction.
        // for cases where focus cannot be found due to an error, a separate marker is needed.
        // TODO potential error - because server time is used, there may be more than 2 focusTime due to differences in server times.
        SpanBo focusTimeSpanBo = findFocusTimeSpanBo(spanAlignList, focusTimestamp);
        // FIXME patched temporarily for cases where focusTimeSpanBo is not found. Need a more complete solution.
        if (focusTimeSpanBo != null) {
            recordSet.setAgentId(focusTimeSpanBo.getAgentId());
            recordSet.setApplicationId(focusTimeSpanBo.getApplicationId());

            final String applicationName = getRpcArgument(focusTimeSpanBo);
            recordSet.setApplicationName(applicationName);
        }

        // find the startTime to use as reference
        long startTime = getStartTime(spanAlignList);
        recordSet.setStartTime(startTime);

        // find the endTime to use as reference
        long endTime = getEndTime(spanAlignList);
        recordSet.setEndTime(endTime);
        
        recordSet.setLoggingTransactionInfo(findIsLoggingTransactionInfo(spanAlignList));

        final SpanAlignPopulate spanAlignPopulate = new SpanAlignPopulate();
        List<Record> recordList = spanAlignPopulate.populateSpanRecord(callTreeIterator);
        logger.debug("RecordList:{}", recordList);

        if (focusTimeSpanBo != null) {
            // mark the record to be used as focus
            long beginTimeStamp = focusTimeSpanBo.getStartTime();
            markFocusRecord(recordList, beginTimeStamp);
            recordSet.setBeginTimestamp(beginTimeStamp);
        }

        recordSet.setRecordList(recordList);

        return recordSet;
    }

    private boolean findIsLoggingTransactionInfo(List<SpanAlign> spanAlignList) {
        for (SpanAlign spanAlign : spanAlignList) {
            if (spanAlign.isSpan()) {
                if (spanAlign.getSpanBo().getLoggingTransactionInfo() == LoggingInfo.LOGGED.getCode()) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private void markFocusRecord(List<Record> recordList, long beginTimeStamp) {
        for (Record record : recordList) {
            if (record.getBegin() == beginTimeStamp) {
                record.setFocused(true);
                break;
            }
        }
    }

    // private void addlogLink(RecordSet recordSet) {
    // List<Record> records = recordSet.getRecordList();
    // List<TransactionInfo> transactionInfoes = new LinkedList<TransactionInfo>();
    //
    // for (Iterator<Record> iterator = records.iterator(); iterator.hasNext();) {
    // Record record = (Record) iterator.next();
    //
    // if(record.getTransactionId() == null) {
    // continue;
    // }
    //
    // TransactionInfo transactionInfo = new TransactionInfo(record.getTransactionId(), record.getSpanId());
    //
    // if (transactionInfoes.contains(transactionInfo)) {
    // continue;
    // };
    //
    // record.setLogPageUrl(logPageUrl);
    // record.setLogButtonName(logButtonName);
    //
    // transactionInfoes.add(transactionInfo);
    // }
    // }

    private class TransactionInfo {

        private final String transactionId;
        private final long spanId;

        public TransactionInfo(String transactionId, long spanId) {
            this.transactionId = transactionId;
            this.spanId = spanId;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public long getSpanId() {
            return spanId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TransactionInfo == false) {
                return false;
            }

            TransactionInfo transactionInfo = (TransactionInfo) obj;

            if (!transactionId.equals(transactionInfo.getTransactionId())) {
                return false;
            }
            if (spanId != transactionInfo.getSpanId()) {
                return false;
            }

            return true;
        }
    }

    private long getStartTime(List<SpanAlign> spanAlignList) {
        if (spanAlignList == null || spanAlignList.size() == 0) {
            return 0;
        }
        SpanAlign spanAlign = spanAlignList.get(0);
        if (spanAlign.isSpan()) {
            SpanBo spanBo = spanAlign.getSpanBo();
            return spanBo.getStartTime();
        } else {
            SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
            return spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
        }
    }

    private long getEndTime(List<SpanAlign> spanAlignList) {
        if (spanAlignList == null || spanAlignList.size() == 0) {
            return 0;
        }
        SpanAlign spanAlign = spanAlignList.get(0);
        if (spanAlign.isSpan()) {
            SpanBo spanBo = spanAlign.getSpanBo();
            return spanBo.getElapsed();
        } else {
            SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
            long begin = spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
            long elapsed = spanEventBo.getEndElapsed();
            return begin + elapsed;
        }
    }

    private SpanBo findFocusTimeSpanBo(List<SpanAlign> spanAlignList, long focusTimestamp) {
        SpanBo firstSpan = null;
        for (SpanAlign spanAlign : spanAlignList) {
            if (spanAlign.isSpan()) {
                SpanBo spanBo = spanAlign.getSpanBo();
                if (spanBo.getCollectorAcceptTime() == focusTimestamp) {
                    return spanBo;
                }
                if (firstSpan == null) {
                    firstSpan = spanBo;
                }
            }
        }
        // return firstSpan when focus Span could not be found.
        return firstSpan;
    }
    
    private String getArgument(final SpanAlign align) {
        if(align.isSpan()) {
            return getRpcArgument(align.getSpanBo());
        }
        
        return getDisplayArgument(align.getSpanEventBo());
    }

    private String getRpcArgument(SpanBo spanBo) {
        String rpc = spanBo.getRpc();
        if (rpc != null) {
            return rpc;
        }
        return getDisplayArgument(spanBo);
    }
    
    private String getDisplayArgument(Span span) {
        AnnotationBo displayArgument = getDisplayArgument0(span);
        if (displayArgument == null) {
            return "";
        }
        return Objects.toString(displayArgument.getValue(), "");
    }

    private AnnotationBo getDisplayArgument0(Span span) {
        // TODO needs a more generalized implementation for Arcus
        List<AnnotationBo> list = span.getAnnotationBoList();
        if (list == null) {
            return null;
        }

        final AnnotationKeyMatcher matcher = annotationKeyMatcherService.findAnnotationKeyMatcher(span.getServiceType());
        if (matcher == null) {
            return null;
        }

        for (AnnotationBo annotation : list) {
            int key = annotation.getKey();

            if (matcher.matches(key)) {
                return annotation;
            }
        }
        return null;
    }

    private class SpanAlignPopulate {
        private List<Record> populateSpanRecord(CallTreeIterator callTreeIterator) {
            if (callTreeIterator == null) {
                throw new NullPointerException("callTreeIterator must not be null");
            }

            final List<Record> recordList = new ArrayList<Record>(callTreeIterator.size() * 2);
            final RecordFactory factory = new RecordFactory(registry, annotationKeyRegistryService);

            // annotation id has nothing to do with spanAlign's seq and thus may be incremented as long as they don't overlap.
            while (callTreeIterator.hasNext()) {
                final CallTreeNode node = callTreeIterator.next();
                if (node == null) {
                    logger.warn("Corrupt CallTree found : {}", callTreeIterator.toString());
                    throw new IllegalStateException("CallTree corrupted");
                }
                final SpanAlign align = node.getValue();
                final String argument = getArgument(align);
                final Record record = factory.get(node, argument);
                recordList.add(record);

                // add exception record.
                if(align.hasException()) {
                    final Record exceptionRecord = factory.getException(record.getTab() + 1, record.getId(), align);
                    if(exceptionRecord != null) {
                        recordList.add(exceptionRecord);
                    }
                }
                
                // add annotation record.
                if(align.getAnnotationBoList().size() > 0) {
                    final List<Record> annotations = factory.getAnnotations(record.getTab() + 1, record.getId(), align);
                    recordList.addAll(annotations);
                }
                
                // add remote record.(span only)
                if (align.getRemoteAddr() != null) {
                    final Record remoteAddressRecord = factory.getParameter(record.getTab() + 1, record.getId(), "REMOTE_ADDRESS", align.getRemoteAddr());
                    recordList.add(remoteAddressRecord);
                }
            }

            return recordList;
        }
    }
}
