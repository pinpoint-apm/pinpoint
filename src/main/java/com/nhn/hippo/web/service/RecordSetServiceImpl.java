package com.nhn.hippo.web.service;


import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.vo.callstacks.Record;
import com.nhn.hippo.web.vo.callstacks.RecordSet;
import com.profiler.common.AnnotationKey;
import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SpanEventBo;
import com.profiler.common.util.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class RecordSetServiceImpl implements RecordSetService {

    @Override
    public RecordSet createRecordSet(List<SpanAlign> spanAlignList, long focusTimestamp) {

        // 이쪽의 sort로직을 spanAlign을 생성하는 부분에 넣어야 되나?
        sortSpanAlignAnnotation(spanAlignList);

        RecordSet recordSet = new RecordSet();

        // focusTimeStamp 를 찾아서 마크한다.
        // span이 2개 이상으로 구성되었을 경우, 내가 어떤 span을 기준으로 보는지 알려면 focus시점을 찾아야 한다.
        // 오류로 인해 foucs를 못찾을수 도있으므로, 없을 경우 별도 mark가 추가적으로 있어야 함.
        // TODO 잘못 될수 있는점 foucusTime은 실제로 2개 이상 나올수 잇음. 서버의 time을 사용하므로 오차로 인해 2개가 나올수도 있음.
        SpanBo focusTimeSpanBo = findFocusTimeSpanBo(spanAlignList, focusTimestamp);

        String applicationName = getDisplayArgument(focusTimeSpanBo);
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

        recordSet.setRecordSet(recordList);
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

    /**
     * 복사본을 생성하지 않고 원본을 그냥 소트함.
     *
     * @param spanAlignList
     */
    private void sortSpanAlignAnnotation(List<SpanAlign> spanAlignList) {
        for (SpanAlign spanAlign : spanAlignList) {
            if (spanAlign.isSpan()) {
                SpanBo spanBo = spanAlign.getSpanBo();
                AnnotationUtils.sortAnnotationListByKey(spanBo);
            } else {
                SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
                AnnotationUtils.sortAnnotationListByKey(spanEventBo);
            }
        }
    }

    private String getDisplayArgument(SpanBo focusTimeSpanBo) {
        Object displayArgument = AnnotationUtils.getDisplayArgument(focusTimeSpanBo);
        if (displayArgument == null) {
            return "";
        }
        return displayArgument.toString();
    }

    private List<Record> createAnnotationRecord(int depth, List<AnnotationBo> annotationBoList) {
        List<Record> recordList = new ArrayList<Record>(annotationBoList.size());

        for (AnnotationBo ann : annotationBoList) {
            AnnotationKey annotation = AnnotationKey.findAnnotationKey(ann.getKey());
            if (annotation.isViewInRecordSet()) {
                Record record = new Record(depth, false, annotation.getValue(), ann.getValue().toString(), 0L, 0L, null, null, null, null);
                recordList.add(record);
            }
        }
        return recordList;
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
        for (SpanAlign spanAlign : spanAlignList) {
            if (spanAlign.isSpan()) {
                SpanBo spanBo = spanAlign.getSpanBo();
                String method = (String) AnnotationUtils.getDisplayMethod(spanBo);
                String arguments = (String) AnnotationUtils.getDisplayArgument(spanBo);

                long begin = spanBo.getStartTime();
                long elapsed = spanBo.getElapsed();

                Record record = new Record(spanAlign.getDepth(), true, method, arguments, begin, elapsed, spanBo.getAgentId(), spanBo.getApplicationId(), spanBo.getServiceType(), null);
                recordList.add(record);

                List<Record> annotationRecord = createAnnotationRecord(spanAlign.getDepth() + 1, spanBo.getAnnotationBoList());
                recordList.addAll(annotationRecord);
            } else {
                SpanEventBo spanEventBo = spanAlign.getSpanEventBo();

                String method = (String) AnnotationUtils.getDisplayMethod(spanEventBo);
                Object arguments = AnnotationUtils.getDisplayArgument(spanEventBo);

                long begin = spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
                long elapsed = spanEventBo.getEndElapsed();

                String destinationId = spanEventBo.getDestinationId();
                Record record = new Record(spanAlign.getDepth(), true, method, (arguments != null) ? arguments.toString() : "", begin, elapsed, spanEventBo.getAgentId(), spanEventBo.getDestinationId(), spanEventBo.getServiceType(), destinationId);
                recordList.add(record);
                List<Record> annotationRecord = createAnnotationRecord(spanAlign.getDepth() + 1, spanEventBo.getAnnotationBoList());
                recordList.addAll(annotationRecord);
            }
        }
        return recordList;
    }
}
