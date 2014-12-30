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

import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.bo.AnnotationBo;
import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.bo.SpanEventBo;
import com.navercorp.pinpoint.common.util.AnnotationUtils;
import com.navercorp.pinpoint.common.util.ApiDescription;
import com.navercorp.pinpoint.common.util.ApiDescriptionParser;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.util.Stack;
import com.navercorp.pinpoint.web.vo.BusinessTransactions;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.callstacks.Record;
import com.navercorp.pinpoint.web.vo.callstacks.RecordSet;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class TransactionInfoServiceImpl implements TransactionInfoService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TraceDao traceDao;
    
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
    public RecordSet createRecordSet(List<SpanAlign> spanAlignList, long focusTimestamp) {
        if (spanAlignList == null) {
            throw new NullPointerException("spanAlignList must not be null");
        }

        RecordSet recordSet = new RecordSet();

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

        final SpanAlignPopulate spanAlignPopulate = new SpanAlignPopulate();
        List<Record> recordList = spanAlignPopulate.populateSpanRecord(spanAlignList);
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

    private void markFocusRecord(List<Record> recordList, long beginTimeStamp) {
        for (Record record : recordList) {
            if (record.getBegin() == beginTimeStamp) {
                record.setFocused(true);
                break;
            }
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

    private static class SpanAlignPopulate {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final ApiDescriptionParser apiDescriptionParser = new ApiDescriptionParser();

        // spans with id = 0 are regarded as root - start at 1
        private int idGen = 1;
        private final Stack<SpanDepth> stack = new Stack<SpanDepth>();

        private int getNextId() {
            return idGen++;
        }

        private List<Record> populateSpanRecord(List<SpanAlign> spanAlignList) {
            if (spanAlignList == null) {
                throw new NullPointerException("spanAlignList must not be null");
            }
            final List<Record> recordList = new ArrayList<Record>(spanAlignList.size() * 2);

            // annotation id has nothing to do with spanAlign's seq and thus may be incremented as long as they don't overlap. 
            for (int i = 0; i < spanAlignList.size(); i++) {
                final SpanAlign spanAlign = spanAlignList.get(i);
                if (i == 0) {
                    if (!spanAlign.isSpan()) {
                        throw new IllegalArgumentException("root is not span");
                    }
                    final SpanDepth spanDepth = new SpanDepth(spanAlign, getNextId(), spanAlign.getSpanBo().getStartTime());
                    stack.push(spanDepth);
                } else {
                    final SpanDepth lastSpanDepth = stack.getLast();
                    final int parentDepth = lastSpanDepth.getSpanAlign().getDepth();
                    final int currentDepth = spanAlign.getDepth();
                    logger.debug("parentDepth:{} currentDepth:{} sequence:{}", parentDepth, currentDepth, lastSpanDepth.getId());

                    if (parentDepth < spanAlign.getDepth()) {
                        // push if parentDepth is smaller
                        final SpanDepth last = stack.getLast();
                        final long beforeStartTime = getStartTime(last.getSpanAlign());
                        final SpanDepth spanDepth = new SpanDepth(spanAlign, getNextId(), beforeStartTime);
                        stack.push(spanDepth);
                    } else {
                        if (parentDepth > currentDepth) {
                            // pop if parentDepth is larger
                            // difference in depth may be greater than 1, so pop and check the depth repeatedly until appropriate
                            SpanDepth lastPopSpanDepth;
                            while (true) {
                                logger.trace("pop");
                                lastPopSpanDepth = stack.pop();
                                SpanDepth popLast = stack.getLast();
                                if (popLast.getSpanAlign().getDepth() < currentDepth) {
                                    break;
                                }
                            }
                            final long beforeLastEndTime = getLastTime(lastPopSpanDepth.getSpanAlign());
                            stack.push(new SpanDepth(spanAlign, getNextId(), beforeLastEndTime));
                        } else {
                            // throw away the object right infront if it has the same depth
                            final SpanDepth before = stack.pop();
                            final long beforeLastEndTime = getLastTime(before.getSpanAlign());
                            stack.push(new SpanDepth(spanAlign, getNextId(), beforeLastEndTime));
                        }
                    }
                }

                if (spanAlign.isSpan()) {
                    SpanBo spanBo = spanAlign.getSpanBo();

                    String argument = getRpcArgument(spanBo);

                    final long begin = spanBo.getStartTime();
                    final long elapsed = spanBo.getElapsed();
                    final int spanBoSequence = stack.getLast().getId();
                    int parentSequence;
                    final SpanDepth parent = stack.getParent();
                    if (parent == null) {
                        // root span
                        parentSequence = 0;
                    } else {
                        parentSequence = parent.getId();
                    }
                    logger.debug("spanBoSequence:{}, parentSequence:{}", spanBoSequence, parentSequence);


                    String method = AnnotationUtils.findApiAnnotation(spanBo.getAnnotationBoList());
                    if (method !=  null) {
                        ApiDescription apiDescription = apiDescriptionParser.parse(method);
                        Record record = new Record(spanAlign.getDepth(), 
													spanBoSequence, 
													parentSequence, 
													true, 
													apiDescription.getSimpleMethodDescription(),
													argument, 
													begin,
													elapsed, 
													getGap(stack),
													spanBo.getAgentId(), 
													spanBo.getApplicationId(), 
													spanBo.getServiceType(), 
													null, 
													spanAlign.isHasChild(),
													false);
                        record.setSimpleClassName(apiDescription.getSimpleClassName());
                        record.setFullApiDescription(method);
                        recordList.add(record);
                    } else {
                        AnnotationKey apiMetaDataError = AnnotationUtils.getApiMetaDataError(spanBo.getAnnotationBoList());
                        Record record = new Record(spanAlign.getDepth(),
													spanBoSequence, 
													parentSequence, 
													true, 
													apiMetaDataError.getValue(), 
													argument,
													begin,
													elapsed, 
													getGap(stack),
													spanBo.getAgentId(), 
													spanBo.getApplicationId(),
													spanBo.getServiceType(),
													null, 
													spanAlign.isHasChild(),
													false);
                        record.setSimpleClassName("");
                        record.setFullApiDescription("");
                        recordList.add(record);
                    }
                    // add exception record
                    final Record exceptionRecord = getExceptionRecord(spanAlign, spanBoSequence);
                    if (exceptionRecord != null) {
                        recordList.add(exceptionRecord);
                    }

                    List<Record> annotationRecord = createAnnotationRecord(spanAlign.getDepth() + 1, spanBoSequence, spanBo.getAnnotationBoList());
                    recordList.addAll(annotationRecord);
                    if (spanBo.getRemoteAddr() != null) {
                        Record remoteAddress = createParameterRecord(spanAlign.getDepth() + 1, spanBoSequence, "REMOTE_ADDRESS", spanBo.getRemoteAddr());
                        recordList.add(remoteAddress);
                    }
                } else {
                    SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
                    SpanBo spanBo = spanAlign.getSpanBo();

                    String argument = getDisplayArgument(spanEventBo);
                    final int spanBoEventSequence = stack.getLast().getId();
                    final SpanDepth parent = stack.getParent();
                    if (parent == null) {
                        throw new IllegalStateException("parent is null. stack:" + stack);
                    }
                    final int parentSequence = parent.getId();
                    logger.debug("spanBoEventSequence:{}, parentSequence:{}", spanBoEventSequence, parentSequence);

                    final String method = AnnotationUtils.findApiAnnotation(spanEventBo.getAnnotationBoList());
                    if (method != null) {
                        ApiDescription apiDescription = apiDescriptionParser.parse(method);
                        String destinationId = spanEventBo.getDestinationId();

                        long begin = spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
                        long elapsed = spanEventBo.getEndElapsed();

                        // use spanBo's applicationId instead of spanEventBo's destinationId to display the name of the calling application on the call stack.
                        Record record = new Record(spanAlign.getDepth(), 
													spanBoEventSequence,
													parentSequence, 
													true, 
													apiDescription.getSimpleMethodDescription(), 
													argument, 
													begin, 
													elapsed,
													getGap(stack),
													spanEventBo.getAgentId(), 
													spanBo.getApplicationId(),
                                                    spanEventBo.getServiceType(),
													/* spanEventBo.getDestinationId(), spanEventBo.getServiceType(),*/ 
													destinationId,
													spanAlign.isHasChild(),
													false);
                        record.setSimpleClassName(apiDescription.getSimpleClassName());
                        record.setFullApiDescription(method);

                        recordList.add(record);
                    } else {
                        AnnotationKey apiMetaDataError = AnnotationUtils.getApiMetaDataError(spanEventBo.getAnnotationBoList());
                        String destinationId = spanEventBo.getDestinationId();

                        long begin = spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
                        long elapsed = spanEventBo.getEndElapsed();

                     // use spanBo's applicationId instead of spanEventBo's destinationId to display the name of the calling application on the call stack.
                        Record record = new Record(spanAlign.getDepth(),
													spanBoEventSequence, 
													parentSequence, 
													true, 
													apiMetaDataError.getValue(), 
													argument, 
													begin,
													elapsed, 
													getGap(stack),
													spanEventBo.getAgentId(),
													spanBo.getApplicationId(),
                                                    spanEventBo.getServiceType(),
													/*spanEventBo.getDestinationId(), spanEventBo.getServiceType(),*/ 
													destinationId, 
													spanAlign.isHasChild(),
													false);
                        record.setSimpleClassName("");
                        record.setFullApiDescription(method);

                        recordList.add(record);
                    }
                    // add exception record
                    final Record exceptionRecord = getExceptionRecord(spanAlign, spanBoEventSequence);
                    if (exceptionRecord != null) {
                        recordList.add(exceptionRecord);
                    }

                    List<Record> annotationRecord = createAnnotationRecord(spanAlign.getDepth() + 1, spanBoEventSequence, spanEventBo.getAnnotationBoList());
                    recordList.addAll(annotationRecord);
                }
            }
            return recordList;
        }

        private Record getExceptionRecord(SpanAlign spanAlign, int parentSequence) {
            if (spanAlign.isSpan()) {
                final SpanBo spanBo = spanAlign.getSpanBo();
                if (spanBo.hasException()) {
                    String simpleExceptionClass = getSimpleExceptionName(spanBo.getExceptionClass());
                    return new Record(spanAlign.getDepth() + 1, 
										getNextId(), 
										parentSequence,
										false, 
										simpleExceptionClass, 
										spanBo.getExceptionMessage(), 
										0L, 
										0L, 
										0, 
										null, 
										null, 
										null, 
										null,
										false,
										false);
                }
            } else {
                final SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
                if (spanEventBo.hasException()) {
                    String simpleExceptionClass = getSimpleExceptionName(spanEventBo.getExceptionClass());
                    return new Record(spanAlign.getDepth() + 1, 
										getNextId(), 
										parentSequence, 
										false, 
										simpleExceptionClass, 
										spanEventBo.getExceptionMessage(),
										0L, 
										0L, 
										0, 
										null, 
										null, 
										null, 
										null, 
										false,
										true);
                }
            }
            return null;
        }

        private String getSimpleExceptionName(String exceptionClass) {
            if (exceptionClass == null) {
                return "";
            }
            final int index = exceptionClass.lastIndexOf('.');
            if (index != -1) {
                exceptionClass = exceptionClass.substring(index+1, exceptionClass.length());
            }
            return exceptionClass;
        }


        private long getGap(Stack<SpanDepth> stack) {
            SpanDepth last = stack.getLast();
            final long lastExecuteTime = last.getLastExecuteTime();
            SpanAlign spanAlign = last.getSpanAlign();
            if (spanAlign.isSpan()) {
                return spanAlign.getSpanBo().getStartTime() - lastExecuteTime;
            } else {
                return (spanAlign.getSpanBo().getStartTime() + spanAlign.getSpanEventBo().getStartElapsed()) - lastExecuteTime;
            }
        }

        private long getLastTime(SpanAlign spanAlign) {
            final SpanBo spanBo = spanAlign.getSpanBo();
            if (spanAlign.isSpan()) {
                return spanBo.getStartTime() + spanBo.getElapsed();
            } else {
                SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
                return spanBo.getStartTime() + spanEventBo.getStartElapsed() + spanEventBo.getEndElapsed();
            }
        }

        private long getStartTime(SpanAlign spanAlign) {
            final SpanBo spanBo = spanAlign.getSpanBo();
            if (spanAlign.isSpan()) {
                return spanBo.getStartTime();
            } else {
                return spanBo.getStartTime() + spanAlign.getSpanEventBo().getStartElapsed();
            }
        }


        private List<Record> createAnnotationRecord(int depth, int parentId, List<AnnotationBo> annotationBoList) {
            List<Record> recordList = new ArrayList<Record>(annotationBoList.size());

            for (AnnotationBo ann : annotationBoList) {
                AnnotationKey annotation = AnnotationKey.findAnnotationKey(ann.getKey());
                if (annotation.isViewInRecordSet()) {
                    Record record = new Record(depth, getNextId(), parentId, false, annotation.getValue(), ann.getValue().toString(), 0L, 0L, 0, null, null, null, null, false, false);
                    recordList.add(record);
                }
            }

            return recordList;
        }

        private Record createParameterRecord(int depth, int parentId, String method, String argument) {
            return new Record(depth, getNextId(), parentId, false, method, argument, 0L, 0L, 0, null, null, null, null, false, false);
        }
    }

    private static String getDisplayArgument(SpanBo spanBo) {
        AnnotationBo displayArgument = AnnotationUtils.getDisplayArgument(spanBo);
        if (displayArgument == null) {
            return "";
        }
        return ObjectUtils.toString(displayArgument.getValue());
    }

    private static String getDisplayArgument(SpanEventBo spanEventBo) {
        AnnotationBo displayArgument = AnnotationUtils.getDisplayArgument(spanEventBo);
        if (displayArgument == null) {
            return "";
        }
        return ObjectUtils.toString(displayArgument.getValue());
    }

    private static String getRpcArgument(SpanBo spanBo) {
        String rpc = spanBo.getRpc();
        if (rpc != null) {
            return rpc;
        }
        return getDisplayArgument(spanBo);
    }
}
