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

	private final List<Record> recordset;
	private final long focusTimestamp;
	private String applicationName;
	private long beginTimestamp;

	public RecordSet(List<SpanAlign> spanAligns, long focusTimestamp) {
		this.recordset = new ArrayList<Record>();
		this.focusTimestamp = focusTimestamp;
		addSpanRecord(spanAligns);
	}

	public Iterator<Record> getIterator() {
		return recordset.iterator();
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
                recordset.add(record);
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

				// scatter에 local call에 대해서도 표시를 해주기 때문에 정확한 URL을 알려면 이렇게...
				if (spanBo.getCollectorAcceptTime() == focusTimestamp) {
					applicationName = arguments;
					beginTimestamp = begin;
				}
                Record record = new Record(spanAlign.getDepth(), true, method, arguments, begin, elapsed, spanBo.getAgentId(), spanBo.getApplicationId(), spanBo.getServiceType(), null);
                recordset.add(record);
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
//                recordset.add(new Record(spanAlign.getDepth(), true, method, (arguments != null) ? arguments.toString() : "", begin, elapsed, spanEvent.getAgentId(), spanEvent.getServiceName(), spanEvent.getServiceType(), destinationId));
                recordset.add(new Record(spanAlign.getDepth(), true, method, (arguments != null) ? arguments.toString() : "", begin, elapsed, spanEventBo.getAgentId(), spanEventBo.getDestinationId(), spanEventBo.getServiceType(), destinationId));
				addAnnotationRecord(spanAlign.getDepth() + 1, spanEventBo.getAnnotationBoList());
			}
		}
	}
}
