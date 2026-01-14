/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.trace.service;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.Event;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatcher;
import com.navercorp.pinpoint.common.trace.ErrorCategory;
import com.navercorp.pinpoint.common.trace.ErrorCategoryResolver;
import com.navercorp.pinpoint.common.trace.LoggingInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.component.AnnotationKeyMatcherService;
import com.navercorp.pinpoint.web.trace.callstacks.Record;
import com.navercorp.pinpoint.web.trace.callstacks.RecordFactory;
import com.navercorp.pinpoint.web.trace.callstacks.RecordSet;
import com.navercorp.pinpoint.web.trace.security.MetaDataFilter;
import com.navercorp.pinpoint.web.trace.security.MetaDataFilter.MetaData;
import com.navercorp.pinpoint.web.trace.span.Align;
import com.navercorp.pinpoint.web.trace.span.CallTreeIterator;
import com.navercorp.pinpoint.web.trace.span.CallTreeNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author jaehong.kim
 */
@Service
public class TransactionInfoServiceImpl implements TransactionInfoService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AnnotationKeyMatcherService annotationKeyMatcherService;

    private final MetaDataFilter metaDataFilter;

    private final RecorderFactoryProvider recordFactoryProvider;
    private final ServiceTypeRegistryService registry;

    private final ErrorCategoryResolver errorCategoryResolver = new ErrorCategoryResolver();

    public TransactionInfoServiceImpl(AnnotationKeyMatcherService annotationKeyMatcherService,
                                      Optional<MetaDataFilter> metaDataFilter,
                                      RecorderFactoryProvider recordFactoryProvider,
                                      ServiceTypeRegistryService registry) {
        this.annotationKeyMatcherService = Objects.requireNonNull(annotationKeyMatcherService, "annotationKeyMatcherService");
        this.metaDataFilter = Objects.requireNonNull(metaDataFilter, "metaDataFilter").orElse(null);
        this.recordFactoryProvider = Objects.requireNonNull(recordFactoryProvider, "recordFactoryProvider");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public RecordSet createRecordSet(CallTreeIterator callTreeIterator, Predicate<SpanBo> viewPointFilter) {
        Objects.requireNonNull(callTreeIterator, "callTreeIterator");
        Objects.requireNonNull(viewPointFilter, "viewPointFilter");

        RecordSet recordSet = new RecordSet();
        final List<Align> alignList = callTreeIterator.values();

        // finds and marks the viewPoint.base on focusTimestamp.
        // focusTimestamp is needed to determine which span to use as reference when there are more than 2 spans making up a transaction.
        // for cases where focus cannot be found due to an error, a separate marker is needed.
        // TODO potential error - because server time is used, there may be more than 2 focusTime due to differences in server times.
        Align viewPointAlign = findViewPoint(alignList, viewPointFilter);
        // FIXME patched temporarily for cases where focusTimeSpanBo is not found. Need a more complete solution.
        if (viewPointAlign != null) {
            recordSet.setAgentId(viewPointAlign.getAgentId());
            recordSet.setAgentName(viewPointAlign.getAgentName());
            recordSet.setApplicationName(viewPointAlign.getApplicationName());
            final ServiceType servicetype = registry.findServiceType(viewPointAlign.getApplicationServiceType());
            if (servicetype != null) {
                recordSet.setServiceType(servicetype.toString());
            }
            final String uri = getRpcArgument(viewPointAlign);
            recordSet.setUri(uri);
        }

        // find the startTime to use as reference
        long startTime = getStartTime(alignList);
        recordSet.setStartTime(startTime);

        // find the endTime to use as reference
        long endTime = getEndTime(alignList);

        /*
         * Workaround codes to prevent issues occurred
         * when endTime is too far away from startTime
         */
        long rootEndTime = getRootEndTime(alignList);

        if (rootEndTime - startTime <= 0) {
            recordSet.setEndTime(endTime);
        } else if ((double) (rootEndTime - startTime) / (endTime - startTime) < 0.1) {
            recordSet.setEndTime(rootEndTime);
        } else {
            recordSet.setEndTime(endTime);
        }

        recordSet.setLoggingTransactionInfo(findIsLoggingTransactionInfo(alignList));

        final SpanAlignPopulate spanAlignPopulate = new SpanAlignPopulate();
        List<Record> recordList = spanAlignPopulate.populateSpanRecord(callTreeIterator);
        if (viewPointAlign != null) {
            // mark the record to be used as focus
            long beginTimeStamp = viewPointAlign.getStartTime();

            markFocusRecord(recordSet, recordList, viewPointAlign);
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

    private void markFocusRecord(RecordSet recordSet, List<Record> recordList, final Align viewPointTimeAlign) {
        final String agentId = viewPointTimeAlign.getAgentId();
        for (Record record : recordList) {
            if (viewPointTimeAlign.getSpanId() == record.getSpanId() && record.getBegin() == viewPointTimeAlign.getStartTime()) {
                if (agentId == null) {
                    if (record.getAgentId() == null) {
                        recordSet.setFocusCallStackId(record.getId());
                        break;
                    }
                } else {
                    if (Strings.CS.equals(record.getAgentId(), record.getAgentId())) {
                        recordSet.setFocusCallStackId(record.getId());
                        break;
                    }
                }
            }
        }
    }

    // private void addlogLink(RecordSet recordSet) {
    // List<Record> records = recordSet.getRecordList();
    // List<TransactionInfo> transactionInfoes = new ArrayList<TransactionInfo>();
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

        long min = Long.MAX_VALUE;
        for (Align align : alignList) {
            min = Math.min(min, align.getStartTime());
        }
        return min;
    }

    private long getRootEndTime(List<Align> alignList) {
        if (CollectionUtils.isEmpty(alignList)) {
            return 0;
        }

        return alignList.get(0).getEndTime();
    }

    private long getEndTime(List<Align> alignList) {
        if (CollectionUtils.isEmpty(alignList)) {
            return 0;
        }
        long max = Long.MIN_VALUE;
        for (Align align : alignList) {
            max = Math.max(max, align.getEndTime());
        }
        return max;
    }

    private Align findViewPoint(List<Align> alignList, Predicate<SpanBo> viewPointFilter) {
        Align firstSpan = null;
        for (Align align : alignList) {
            if (align.isSpan()) {
                final SpanBo spanBo = align.getSpanBo();
                if (isViewPoint(spanBo, viewPointFilter)) {
                    return align;
                }
                // do not use unknown, corrupted span as firstSpan
                if (firstSpan == null && !align.isMeta()) {
                    firstSpan = align;
                }
            }
        }
        // return firstSpan when focus Span could not be found.
        return firstSpan;
    }

    private boolean isViewPoint(final SpanBo spanBo, Predicate<SpanBo> viewPointFilter) {
        if (viewPointFilter.test(spanBo)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Matched view point. viewPointFilter={}, spanAlign={focusTimestamp={}, agentId={}, spanId={}}",
                        viewPointFilter, spanBo.getCollectorAcceptTime(), spanBo.getAgentId(), spanBo.getSpanId());
            }
            return true;
        }
        return false;
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
            Objects.requireNonNull(callTreeIterator, "callTreeIterator");

            final List<Record> recordList = new ArrayList<>(callTreeIterator.size() * 2);
            final RecordFactory factory = recordFactoryProvider.getRecordFactory();

            // annotation id has nothing to do with spanAlign's seq and thus may be incremented as long as they don't overlap.
            while (callTreeIterator.hasNext()) {
                final CallTreeNode node = callTreeIterator.next();
                if (node == null) {
                    logger.warn("Corrupt CallTree found : {}", callTreeIterator);
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

                // add error category record.(span only)
                if (align.isSpan()) {
                    final SpanBo spanBo = align.getSpanBo();
                    if (spanBo.hasError()) {
                        Set<ErrorCategory> flagged = errorCategoryResolver.resolve(spanBo.getErrCode());
                        if (!flagged.isEmpty()) {
                            final Record errorCategoryRecord = factory.getErrorCategory(record.getTab() + 1, record.getId(), flagged);
                            recordList.add(errorCategoryRecord);
                        }
                    }
                }

                // add exception record.
                if (align.hasException()) {
                    final Record exceptionRecord = factory.getException(record.getTab() + 1, record.getId(), align);
                    if (exceptionRecord != null) {
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

                // add endPoint.(span only)
                if (align.isSpan()) {
                    final SpanBo spanBo = align.getSpanBo();
                    final String endPoint = spanBo.getEndPoint();
                    if (endPoint != null) {
                        final Record endPointRecord = factory.getParameter(record.getTab() + 1, record.getId(), "ENDPOINT", endPoint);
                        recordList.add(endPointRecord);
                    }
                }
            }

            return recordList;
        }
    }
}
