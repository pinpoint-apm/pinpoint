package com.nhn.hippo.web.vo.callstacks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.profiler.common.AnnotationKey;
import com.profiler.common.bo.AnnotationBo;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SpanEventBo;
import com.profiler.common.util.AnnotationUtils;

/**
 * @author netspider
 */
public class RecordSet {

    private long startTime = -1;
	private long endTime = -1;

	private List<Record> recordSet;
	private String applicationName;
	private long beginTimestamp;

	public RecordSet() {
	}

    public void setRecordSet(List<Record> recordSet) {
        this.recordSet = recordSet;
    }

    public Iterator<Record> getIterator() {
		return recordSet.iterator();
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isStartTimeSet() {
		return startTime != -1;
	}

	public boolean isEndTimeSet() {
		return endTime != -1;
	}

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setBeginTimestamp(long beginTimestamp) {
        this.beginTimestamp = beginTimestamp;
    }

    public String getApplicationName() {
		return applicationName;
	}

	public long getBeginTimestamp() {
		return beginTimestamp;
	}

	private void addAnnotationRecord(int depth, List<AnnotationBo> annotationBoList) {
		for (AnnotationBo ann : annotationBoList) {
			AnnotationKey annotation = AnnotationKey.findAnnotationKey(ann.getKey());
			if (annotation.isViewInRecordSet()) {
                Record record = new Record(depth, false, annotation.getValue(), ann.getValue().toString(), 0L, 0L, null, null, null, null);
                recordSet.add(record);
			}
		}
	}

	private void addSpanRecord(List<SpanAlign> spanAligns) {
		boolean marked = false;

		for (SpanAlign spanAlign : spanAligns) {
			if (spanAlign.isSpan()) {
				SpanBo spanBo = spanAlign.getSpanBo();
				AnnotationUtils.sortAnnotationListByKey(spanBo);
				String method = (String) AnnotationUtils.getDisplayMethod(spanBo);
				String arguments = (String) AnnotationUtils.getDisplayArgument(spanBo);

				long begin = spanBo.getStartTime();
				long elapsed = spanBo.getElapsed();

				if (!marked) {
                        setStartTime(begin);
                        setEndTime(begin + elapsed);
                        applicationName = arguments;
                        beginTimestamp = begin;
                        marked = true;
				}

                Record record = new Record(spanAlign.getDepth(), true, method, arguments, begin, elapsed, spanBo.getAgentId(), spanBo.getApplicationId(), spanBo.getServiceType(), null);
                recordSet.add(record);
				addAnnotationRecord(spanAlign.getDepth() + 1, spanBo.getAnnotationBoList());
			} else {
				SpanEventBo spanEventBo = spanAlign.getSpanEventBo();

				AnnotationUtils.sortAnnotationListByKey(spanEventBo);
				String method = (String) AnnotationUtils.getDisplayMethod(spanEventBo);
				Object arguments = AnnotationUtils.getDisplayArgument(spanEventBo);

				long begin = spanAlign.getSpanBo().getStartTime() + spanEventBo.getStartElapsed();
				long elapsed = spanEventBo.getEndElapsed();

				if (!marked) {
					setStartTime(begin);
					setEndTime(begin + elapsed);
					marked = true;
				}
                String destinationId = spanEventBo.getDestinationId();
                Record record = new Record(spanAlign.getDepth(), true, method, (arguments != null) ? arguments.toString() : "", begin, elapsed, spanEventBo.getAgentId(), spanEventBo.getDestinationId(), spanEventBo.getServiceType(), destinationId);
                recordSet.add(record);
				addAnnotationRecord(spanAlign.getDepth() + 1, spanEventBo.getAnnotationBoList());
			}
		}
	}
}
