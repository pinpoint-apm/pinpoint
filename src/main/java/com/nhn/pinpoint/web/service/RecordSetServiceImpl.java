package com.nhn.pinpoint.web.service;


import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
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

    private ApiDescriptionParser apiDescriptionParser = new ApiDescriptionParser();

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

        List<Record> recordList = populateSpanRecord(spanAlignList);
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


    private String getRpcArgument(SpanBo spanBo) {
        String rpc = spanBo.getRpc();
        if (rpc != null) {
            return rpc;
        }
        return getDisplayArgument(spanBo);
    }

    private String getDisplayArgument(SpanBo spanBo) {
        AnnotationBo displayArgument = AnnotationUtils.getDisplayArgument(spanBo);
        if (displayArgument == null) {
            return "";
        }
        return ObjectUtils.toString(displayArgument.getValue());
    }
    private String getDisplayArgument(SpanEventBo spanEventBo) {
        AnnotationBo displayArgument = AnnotationUtils.getDisplayArgument(spanEventBo);
        if (displayArgument == null) {
            return "";
        }
        return ObjectUtils.toString(displayArgument.getValue());
    }

    private List<Record> createAnnotationRecord(int depth, Integer id, int pId, List<AnnotationBo> annotationBoList) {
        List<Record> recordList = new ArrayList<Record>(annotationBoList.size());

        for (AnnotationBo ann : annotationBoList) {
            AnnotationKey annotation = AnnotationKey.findAnnotationKey(ann.getKey());
            if (annotation.isViewInRecordSet()) {
                Record record = new Record(depth, id++, pId, false, annotation.getValue(), ann.getValue().toString(), 0L, 0L, null, null, null, null, false);
                recordList.add(record);
            }
        }
        
        return recordList;
    }

    private Record createParameterRecord(int depth, int id, int pId, String method, String argument) {
       Record record = new Record(depth, id, pId, false, method, argument, 0L, 0L, null, null, null, null, false);
       return record;
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


    private List<Record> populateSpanRecord(List<SpanAlign> spanAlignList) {
        List<Record> recordList = new ArrayList<Record>(spanAlignList.size() * 2);

        // annotation id는 spanalign의 seq와 무관하게 순서대로 따도 됨. 겹치지만 않으면 됨.
        Integer annotationSeq = spanAlignList.size() + 1;
        Deque<Integer> stack = new LinkedList<Integer>();
        stack.add(-1);
        
        int prevDepth = 0;
		for (int i = 0; i < spanAlignList.size(); i++) {
			SpanAlign spanAlign = spanAlignList.get(i);
			
			int parentSeq;
			int currentSeq = spanAlign.getSequence();
			int currentDepth = spanAlign.getDepth();
			
//			System.out.println(i + ", prevDepth=" + prevDepth + ", currentDepth=" + currentDepth + ", currentSeq=" + currentSeq);
			
			if (i == 0) {
				// view에서 -1은 ""으로 변환됨. 최상위 노드의 부모는 ""로 표기되어야 하기 때문.
				parentSeq = -1;
				prevDepth = currentDepth;
				stack.add(currentSeq);
			} else {
				if (prevDepth < currentDepth) {
					parentSeq = stack.getLast();
					stack.add(currentSeq);
				} else if (prevDepth > currentDepth) {
					stack.pollLast();
					parentSeq = stack.getLast();
				} else {
					parentSeq = stack.getLast();
				}
				prevDepth = currentDepth;
			}
			
//			System.out.println("\tparent=" + parentSeq);
//			System.out.println(stack);

            if (spanAlign.isSpan()) {
                SpanBo spanBo = spanAlign.getSpanBo();

                String argument = getRpcArgument(spanBo);

                long begin = spanBo.getStartTime();
                long elapsed = spanBo.getElapsed();

                String method = AnnotationUtils.findApiAnnotation(spanBo.getAnnotationBoList());
                if (method !=  null) {
                    ApiDescription apiDescription = apiDescriptionParser.parse(method);
                    Record record = new Record(spanAlign.getDepth(), spanAlign.getSequence(), parentSeq, true, apiDescription.getSimpleMethodDescription(), argument, begin, elapsed, spanBo.getAgentId(), spanBo.getApplicationId(), spanBo.getServiceType(), null, spanAlign.isHasChild());
                    record.setSimpleClassName(apiDescription.getSimpleClassName());
                    record.setFullApiDescription(method);
                    recordList.add(record);
                } else {
                    AnnotationKey apiMetaDataError = AnnotationUtils.getApiMetaDataError(spanBo.getAnnotationBoList());
                    Record record = new Record(spanAlign.getDepth(), spanAlign.getSequence(), parentSeq, true, apiMetaDataError.getValue(), argument, begin, elapsed, spanBo.getAgentId(), spanBo.getApplicationId(), spanBo.getServiceType(), null, spanAlign.isHasChild());
                    record.setSimpleClassName("");
                    record.setFullApiDescription("");
                    recordList.add(record);
                }
                
				List<Record> annotationRecord = createAnnotationRecord(spanAlign.getDepth() + 1, annotationSeq++, parentSeq, spanBo.getAnnotationBoList());
                recordList.addAll(annotationRecord);
                if (spanBo.getRemoteAddr() != null) {
                    Record remoteAddress = createParameterRecord(spanAlign.getDepth() + 1, annotationSeq++, parentSeq, "REMOTE_ADDRESS", spanBo.getRemoteAddr());
                    recordList.add(remoteAddress);
                }
            } else {
                SpanEventBo spanEventBo = spanAlign.getSpanEventBo();

                String argument = getDisplayArgument(spanEventBo);

                final String method = AnnotationUtils.findApiAnnotation(spanEventBo.getAnnotationBoList());
                if (method != null) {
                    ApiDescription apiDescription = apiDescriptionParser.parse(method);
                    String destinationId = spanEventBo.getDestinationId();

                    long begin = spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
                    long elapsed = spanEventBo.getEndElapsed();

                    Record record = new Record(spanAlign.getDepth(), spanAlign.getSequence(), parentSeq, true, apiDescription.getSimpleMethodDescription(), argument, begin, elapsed, spanEventBo.getAgentId(), spanEventBo.getDestinationId(), spanEventBo.getServiceType(), destinationId, spanAlign.isHasChild());
                    record.setSimpleClassName(apiDescription.getSimpleClassName());
                    record.setFullApiDescription(method);

                    recordList.add(record);
                } else {
                    AnnotationKey apiMetaDataError = AnnotationUtils.getApiMetaDataError(spanEventBo.getAnnotationBoList());
                    String destinationId = spanEventBo.getDestinationId();

                    long begin = spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
                    long elapsed = spanEventBo.getEndElapsed();

                    Record record = new Record(spanAlign.getDepth(), spanAlign.getSequence(), parentSeq, true, apiMetaDataError.getValue(), argument, begin, elapsed, spanEventBo.getAgentId(), spanEventBo.getDestinationId(), spanEventBo.getServiceType(), destinationId, spanAlign.isHasChild());
                    record.setSimpleClassName("");
                    record.setFullApiDescription(method);

                    recordList.add(record);
                }

				List<Record> annotationRecord = createAnnotationRecord(spanAlign.getDepth() + 1, annotationSeq++, parentSeq, spanEventBo.getAnnotationBoList());
				recordList.addAll(annotationRecord);
            }
        }
        return recordList;
    }
}
