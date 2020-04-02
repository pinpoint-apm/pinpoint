/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.Event;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.calltree.span.Align;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.calltree.span.CallTreeNode;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.security.MetaDataFilter;
import com.navercorp.pinpoint.web.security.MetaDataFilter.MetaData;
import com.navercorp.pinpoint.web.vo.BusinessTransactions;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordFactory;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author jaehong.kim
 */
@Service
public class TransactionInfoServiceImpl implements TransactionInfoService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TraceDao traceDao;

    private final AnnotationKeyMatcherService annotationKeyMatcherService;

    private final ServiceTypeRegistryService registry;

    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    private final MetaDataFilter metaDataFilter;

    private final ProxyRequestTypeRegistryService proxyRequestTypeRegistryService;

    public TransactionInfoServiceImpl(@Qualifier("hbaseTraceDaoFactory") TraceDao traceDao,
                                      AnnotationKeyMatcherService annotationKeyMatcherService,
                                      ServiceTypeRegistryService registry,
                                      AnnotationKeyRegistryService annotationKeyRegistryService,
                                      Optional<MetaDataFilter> metaDataFilter, ProxyRequestTypeRegistryService proxyRequestTypeRegistryService) {
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.annotationKeyMatcherService = Objects.requireNonNull(annotationKeyMatcherService, "annotationKeyMatcherService");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.annotationKeyRegistryService = Objects.requireNonNull(annotationKeyRegistryService, "annotationKeyRegistryService");
        this.metaDataFilter = Objects.requireNonNull(metaDataFilter, "metaDataFilter").orElse(null);
        this.proxyRequestTypeRegistryService = Objects.requireNonNull(proxyRequestTypeRegistryService, "proxyRequestTypeRegistryService");
    }

    // Temporarily disabled Because We need to solve authentication problem inter system.
    // @Value("${log.enable:false}")
    // private boolean logLinkEnable;

    // @Value("${log.button.name:}")
    // private String logButtonName;

    // @Value("${log.page.url:}")
    // private String logPageUrl;

    @Override
    public BusinessTransactions selectBusinessTransactions(List<TransactionId> transactionIdList, String applicationName, Range range, Filter filter) {
        Objects.requireNonNull(transactionIdList, "transactionIdList");
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(filter, "filter");
        if (range == null) {
            // TODO range is not used - check the logic again
            throw new NullPointerException("range");
        }

        List<List<SpanBo>> traceList;

        if (filter == Filter.acceptAllFilter()) {
            List<GetTraceInfo> getTraceInfoList = new ArrayList<>(transactionIdList.size());
            for (TransactionId transactionId : transactionIdList) {
                getTraceInfoList.add(new GetTraceInfo(transactionId));
            }
            traceList = this.traceDao.selectSpans(getTraceInfoList);
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
    public RecordSet createRecordSet(CallTreeIterator callTreeIterator, long focusTimestamp, String agentId, long spanId) {
        Objects.requireNonNull(callTreeIterator, "callTreeIterator");

        RecordSet recordSet = new RecordSet();
        final List<Align> alignList = callTreeIterator.values();

        // finds and marks the viewPoint.base on focusTimestamp.
        // focusTimestamp is needed to determine which span to use as reference when there are more than 2 spans making up a transaction.
        // for cases where focus cannot be found due to an error, a separate marker is needed.
        // TODO potential error - because server time is used, there may be more than 2 focusTime due to differences in server times.
        Align viewPointAlign = findViewPoint(alignList, focusTimestamp, agentId, spanId);
        // FIXME patched temporarily for cases where focusTimeSpanBo is not found. Need a more complete solution.
        if (viewPointAlign != null) {
            recordSet.setAgentId(viewPointAlign.getAgentId());
            recordSet.setApplicationId(viewPointAlign.getApplicationId());

            final String applicationName = getRpcArgument(viewPointAlign);
            recordSet.setApplicationName(applicationName);
        }

        // find the startTime to use as reference
        long startTime = getStartTime(alignList);
        recordSet.setStartTime(startTime);

        // find the endTime to use as reference
        long endTime = getEndTime(alignList);
        recordSet.setEndTime(endTime);

        recordSet.setLoggingTransactionInfo(findIsLoggingTransactionInfo(alignList));

        final SpanAlignPopulate spanAlignPopulate = new SpanAlignPopulate();
        List<Record> recordList = spanAlignPopulate.populateSpanRecord(callTreeIterator);
        if (viewPointAlign != null) {
            // mark the record to be used as focus
            long beginTimeStamp = viewPointAlign.getStartTime();

            markFocusRecord(recordList, viewPointAlign);
            recordSet.setBeginTimestamp(beginTimeStamp);
        }

        recordSet.setRecordList(recordList);

        return recordSet;
    }

    private boolean findIsLoggingTransactionInfo(List<Align> alignList) {
        for (Align align : alignList) {
            if (align.isSpan()) {
                if (align.getLoggingTransactionInfo() == LoggingInfo.LOGGED.getCode()) {
                    return true;
                }
            }
        }

        return false;
    }

    private void markFocusRecord(List<Record> recordList, final Align viewPointTimeAlign) {
        final String agentId = viewPointTimeAlign.getAgentId();
        for (Record record : recordList) {
            if (viewPointTimeAlign.getSpanId() == record.getSpanId() && record.getBegin() == viewPointTimeAlign.getStartTime()) {
                if (agentId == null) {
                    if (record.getAgent() == null) {
                        record.setFocused(true);
                        break;
                    }
                } else {
                    if (record.getAgent() != null && agentId.equals(record.getAgent())) {
                        record.setFocused(true);
                        break;
                    }
                }
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

    private long getStartTime(List<Align> alignList) {
        if (CollectionUtils.isEmpty(alignList)) {
            return 0;
        }
        Align align = alignList.get(0);
        return align.getStartTime();
    }

    private long getEndTime(List<Align> alignList) {
        if (CollectionUtils.isEmpty(alignList)) {
            return 0;
        }
        Align align = alignList.get(0);
        return align.getEndTime();
    }

    private Align findViewPoint(List<Align> alignList, long focusTimestamp, String agentId, long spanId) {
        Align firstSpan = null;
        for (Align align : alignList) {
            if (align.isSpan()) {
                if (isViewPoint(align, focusTimestamp, agentId, spanId)) {
                    return align;
                }
                if (firstSpan == null) {
                    firstSpan = align;
                }
            }
        }
        // return firstSpan when focus Span could not be found.
        return firstSpan;
    }

    private boolean isViewPoint(final Align align, long focusTimestamp, String agentId, long spanId) {
        if (align.getCollectorAcceptTime() != focusTimestamp) {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Matched focusTimestamp of view point. focusTimestamp={}, spanAlign={focusTimestamp={}, agentId={}, spanId={}}", focusTimestamp, align.getCollectorAcceptTime(), align.getAgentId(), align.getSpanId());
        }

        // agentId
        if (agentId != null) {
            if (align.getAgentId() == null || !align.getAgentId().equals(agentId)) {
                return false;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Matched agentId of view point. agentId={}, spanAlign={focusTimestamp={}, agentId={}, spanId={}}", agentId, align.getCollectorAcceptTime(), align.getAgentId(), align.getSpanId());
            }
        }

        // spanId
        if (spanId != -1) {
            if (align.getSpanId() != spanId) {
                return false;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Matched spanId of view point. spanId={}, spanAlign={focusTimestamp={}, agentId={}, spanId={}}", spanId, align.getCollectorAcceptTime(), align.getAgentId(), align.getSpanId());
            }
        }

        return true;
    }

    private String getRpcArgument(Align align) {
        final String rpc = align.getRpc();
        if (rpc != null) {
            return rpc;
        }
        SpanBo spanBo = align.getSpanBo();
        return getDisplayArgument(spanBo);
    }

    private String getDisplayArgument(Event event) {
        AnnotationBo displayArgument = getDisplayArgument0(event);
        if (displayArgument == null) {
            return "";
        }
        return Objects.toString(displayArgument.getValue(), "");
    }

    private AnnotationBo getDisplayArgument0(Event event) {
        // TODO needs a more generalized implementation for Arcus
        List<AnnotationBo> list = event.getAnnotationBoList();
        if (list == null) {
            return null;
        }

        final AnnotationKeyMatcher matcher = annotationKeyMatcherService.findAnnotationKeyMatcher(event.getServiceType());
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
                throw new NullPointerException("callTreeIterator");
            }

            final List<Record> recordList = new ArrayList<>(callTreeIterator.size() * 2);
            final RecordFactory factory = new RecordFactory(annotationKeyMatcherService, registry, annotationKeyRegistryService, proxyRequestTypeRegistryService);

            // annotation id has nothing to do with spanAlign's seq and thus may be incremented as long as they don't overlap.
            while (callTreeIterator.hasNext()) {
                final CallTreeNode node = callTreeIterator.next();
                if (node == null) {
                    logger.warn("Corrupt CallTree found : {}", callTreeIterator.toString());
                    throw new IllegalStateException("CallTree corrupted");
                }
                final Align align = node.getAlign();

                if (metaDataFilter != null && metaDataFilter.filter(align, MetaData.API)) {
                    if (align.isSpan()) {
                        Record record = metaDataFilter.createRecord(node, factory);
                        recordList.add(record);
                    }
                    continue;
                }

                if (metaDataFilter != null && metaDataFilter.filter(align, MetaData.PARAM)) {
                    metaDataFilter.replaceAnnotationBo(align, MetaData.PARAM);
                }

                final Record record = factory.get(node);
                recordList.add(record);

                // add exception record.
                if (align.hasException()) {
                    final Record exceptionRecord = factory.getException(record.getTab() + 1, record.getId(), align);
                    if(exceptionRecord != null) {
                        recordList.add(exceptionRecord);
                    }
                }

                // add annotation record.
                if (!align.getAnnotationBoList().isEmpty()) {
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
