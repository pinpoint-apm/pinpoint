package com.nhn.pinpoint.web.service;


import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.web.util.Stack;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.bo.AnnotationBo;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.common.util.AnnotationUtils;
import com.nhn.pinpoint.common.util.ApiDescription;
import com.nhn.pinpoint.common.util.ApiDescriptionParser;
import com.nhn.pinpoint.web.calltree.span.SpanAlign;
import com.nhn.pinpoint.web.vo.callstacks.Record;
import com.nhn.pinpoint.web.vo.callstacks.RecordSet;

/**
 *
 */
@Service
public class RecordSetServiceImpl implements RecordSetService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());



    @Override
    public RecordSet createRecordSet(List<SpanAlign> spanAlignList, long focusTimestamp) {

        RecordSet recordSet = new RecordSet();

        // focusTimeStamp 를 찾아서 마크한다.
        // span이 2개 이상으로 구성되었을 경우, 내가 어떤 span을 기준으로 보는지 알려면 focus시점을 찾아야 한다.
        // 오류로 인해 foucs를 못찾을수 도있으므로, 없을 경우 별도 mark가 추가적으로 있어야 함.
        // TODO 잘못 될수 있는점 foucusTime은 실제로 2개 이상 나올수 잇음. 서버의 time을 사용하므로 오차로 인해 2개가 나올수도 있음.
        SpanBo focusTimeSpanBo = findFocusTimeSpanBo(spanAlignList, focusTimestamp);
        recordSet.setAgentId(focusTimeSpanBo.getAgentId());
        recordSet.setApplicationId(focusTimeSpanBo.getApplicationId());

        String applicationName = getRpcArgument(focusTimeSpanBo);
        recordSet.setApplicationName(applicationName);


        // 기준이 되는 시작시간을 찾는다.
        long startTime = getStartTime(spanAlignList);
        recordSet.setStartTime(startTime);

        // 기준이 되는 종료 시간을 찾는다.
        long endTime = getEndTime(spanAlignList);
        recordSet.setEndTime(endTime);

        final SpanAlignPopulate spanAlignPopulate = new SpanAlignPopulate();
        List<Record> recordList = spanAlignPopulate.populateSpanRecord(spanAlignList);
        logger.debug("RecordList:{}", recordList);

        // focus 대상 record를 체크한다.
        long beginTimeStamp = focusTimeSpanBo.getStartTime();
        markFocusRecord(recordList, beginTimeStamp);
        recordSet.setBeginTimestamp(beginTimeStamp);

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
        // foucus된 Span을 찾지 못할 경우 firstSpan을 리턴한다.
        return firstSpan;
    }

    private static class SpanAlignPopulate {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final ApiDescriptionParser apiDescriptionParser = new ApiDescriptionParser();

        // id가 0일 경우 root로 취급하는 문제가 있어 1부터 시작하도록 함.
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

            // annotation id는 spanalign의 seq와 무관하게 순서대로 따도 됨. 겹치지만 않으면 됨.
            for (int i = 0; i < spanAlignList.size(); i++) {
                final SpanAlign spanAlign = spanAlignList.get(i);
                if (i == 0) {
                    if (!spanAlign.isSpan()) {
                        throw new IllegalArgumentException("root is not span");
                    }
                    // spanAlign의 startTime을 넣을 경우 동일 시간으로 빼면 0이 나오므로 동일 값을 넣는다..
                    final SpanDepth spanDepth = new SpanDepth(spanAlign, getNextId(), spanAlign.getSpanBo().getStartTime());
                    stack.push(spanDepth);
                } else {
                    final SpanDepth lastSpanDepth = stack.getLast();
                    final int parentDepth = lastSpanDepth.getSpanAlign().getDepth();
                    final int currentDepth = spanAlign.getDepth();
                    logger.debug("parentDepth:{} currentDepth:{} sequence:{}", parentDepth, currentDepth, lastSpanDepth.getId());

                    if (parentDepth < spanAlign.getDepth()) {
                        // 부모의 깊이가 더 작을 경우 push해야 한다.
                        final SpanDepth last = stack.getLast();
                        final long beforeStartTime = getStartTime(last.getSpanAlign());
                        final SpanDepth spanDepth = new SpanDepth(spanAlign, getNextId(), beforeStartTime);
                        stack.push(spanDepth);
                    } else {
                        if (parentDepth > currentDepth) {
                            // 부모의 깊이가 클 경우 pop해야 한다.
                            // 단 depth차가 1depth이상 날수 있기 때문에. depth를 확인하면서 pop을 해야 한다.
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
                            // 바로 앞 동일 depth의 object는 버려야 한다.
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
                    if (stack.getParent() == null) {
                        // 자기 자신이 root인 경우
                        parentSequence = 0;
                    } else {
                        parentSequence = stack.getParent().getId();
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
                    // exception이 발생했을 경우 record추가.
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
                    final int parentSequence = stack.getParent().getId();
                    logger.debug("spanBoEventSequence:{}, parentSequence:{}", spanBoEventSequence, parentSequence);

                    final String method = AnnotationUtils.findApiAnnotation(spanEventBo.getAnnotationBoList());
                    if (method != null) {
                        ApiDescription apiDescription = apiDescriptionParser.parse(method);
                        String destinationId = spanEventBo.getDestinationId();

                        long begin = spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
                        long elapsed = spanEventBo.getEndElapsed();

                        // stacktrace에 호출한 application name을 보여주기 위해서 eventbo.destinationid 대신에 spanbo.applicaitonid를 넣어줌.
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
													spanBo.getServiceType(),
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

                        // stacktrace에 호출한 application name을 보여주기 위해서 eventbo.destinationid 대신에 spanbo.applicaitonid를 넣어줌.
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
													spanBo.getServiceType(), 
													/*spanEventBo.getDestinationId(), spanEventBo.getServiceType(),*/ 
													destinationId, 
													spanAlign.isHasChild(),
													false);
                        record.setSimpleClassName("");
                        record.setFullApiDescription(method);

                        recordList.add(record);
                    }
                    // exception이 발생했을 경우 record추가.
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
