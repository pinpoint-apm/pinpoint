package com.nhn.pinpoint.web.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.web.util.TimeWindowUtils;

/**
 * 
 * @author netspider
 * 
 */
public class LinkStatistics {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final int SLOT_SLOW = Integer.MAX_VALUE - 1;
	private static final int SLOT_ERROR = Integer.MAX_VALUE;

//	/**
//	 * <pre>
//	 * key = responseTimeslot
//	 * value = count
//	 * </pre>
//	 */
//	private final SortedMap<Integer, Long> histogramSummary = new TreeMap<Integer, Long>();;

	/**
	 * <pre>
	 * index = responseTimeslot index,
	 * value = key=timestamp, value=value
	 * </pre>
	 */
	private final List<SortedMap<Long, Long>> timeseriesValueList = new ArrayList<SortedMap<Long, Long>>();
	private final SortedMap<Integer, Integer> timeseriesSlotIndex = new TreeMap<Integer, Integer>();

	private final long from;
	private final long to;

	private long successCount = 0;
	private long failedCount = 0;

	public LinkStatistics(long from, long to) {
		this.from = from;
		this.to = to;
	}

	/**
	 * timeseries 기본값 채운다. 빈 공간은 그냥 적당히 채워준다. 모두 채우면 느리니까..
	 * 
	 * @return
	 */
	private SortedMap<Long, Long> makeEmptyTimeseriesValueMap() {
		SortedMap<Long, Long> map = new TreeMap<Long, Long>();
		long windowSize = TimeWindowUtils.getWindowSize(from, to);
		for (long time = from; time <= to; time += windowSize) {
			map.put(time, 0L);
		}
		return map;
	}

	/**
	 * histogram slot을 설정하면 view에서 값이 없는 slot의 값을 0으로 보여줄 수 있다. 설정되지 않으면 key를
	 * 몰라서 보여주지 못함. 입력된 값만 보이게 됨.
	 * 
	 * @param slotList
	 */
	public void setDefaultHistogramSlotList(List<HistogramSlot> slotList) {
		if (successCount > 0 || failedCount > 0) {
			throw new IllegalStateException("Can't set slot list while containing the data.");
		}

//		histogramSummary.clear();
		timeseriesSlotIndex.clear();
		timeseriesValueList.clear();

//		histogramSummary.put(SLOT_SLOW, 0L);
//		histogramSummary.put(SLOT_ERROR, 0L);

		for (HistogramSlot slot : slotList) {
//			histogramSummary.put(slot.getSlotTime(), 0L);
			timeseriesSlotIndex.put(slot.getSlotTime(), timeseriesSlotIndex.size());
			timeseriesValueList.add(makeEmptyTimeseriesValueMap());
		}

		timeseriesSlotIndex.put(SLOT_SLOW, timeseriesSlotIndex.size());
		timeseriesSlotIndex.put(SLOT_ERROR, timeseriesSlotIndex.size());

		timeseriesValueList.add(makeEmptyTimeseriesValueMap());
		timeseriesValueList.add(makeEmptyTimeseriesValueMap());
	}

	public void addSample(long timestamp, int responseTimeslot, long callCount, boolean failed) {
		logger.info("Add sample. timeslot=" + timestamp + ", responseTimeslot=" + responseTimeslot + ", callCount=" + callCount + ", failed=" + failed);

		timestamp = TimeWindowUtils.refineTimestamp(from, to, timestamp);

		if (failed) {
			failedCount += callCount;
		} else {
			successCount += callCount;
		}

		// TODO 이렇게 하는게 뭔가 좋지 않은것 같음.
		if (responseTimeslot == -1) {
			responseTimeslot = SLOT_ERROR;
		} else if (responseTimeslot == 0) {
			responseTimeslot = SLOT_SLOW;
		}

		// add summary
//		long value = histogramSummary.containsKey(responseTimeslot) ? histogramSummary.get(responseTimeslot) + callCount : callCount;
//		histogramSummary.put(responseTimeslot, value);

		/**
		 * <pre>
		 * timeseriesValueList의 구조는..
		 * list[respoinse_slot_no + 0] = value<timestamp, call count> 
		 * list[respoinse_slot_no + 1] = value<timestamp, call count> 
		 * list[respoinse_slot_no + N] = value<timestamp, call count>
		 * </pre>
		 */
		for (int i = 0; i < timeseriesValueList.size(); i++) {
			SortedMap<Long, Long> map = timeseriesValueList.get(i);

			// 다른 slot에도 같은 시간이 존재해야한다.
			if (i == timeseriesSlotIndex.get(responseTimeslot)) {
				long v = map.containsKey(timestamp) ? map.get(timestamp) + callCount : callCount;
				map.put(timestamp, v);
			} else {
				if (!map.containsKey(timestamp)) {
					map.put(timestamp, 0L);
				}
			}
		}
	}

//	public Map<Integer, Long> getHistogramSummary() {
//		return histogramSummary;
//	}

	public long getSuccessCount() {
		return successCount;
	}

	public long getFailedCount() {
		return failedCount;
	}

	public SortedMap<Integer, Integer> getTimeseriesSlotIndex() {
		return timeseriesSlotIndex;
	}

	public List<SortedMap<Long, Long>> getTimeseriesValue() {
		return timeseriesValueList;
	}

	public int getSlow() {
		return SLOT_SLOW;
	}

	public int getError() {
		return SLOT_ERROR;
	}

	@Override
	public String toString() {
		return "LinkStatistics [timeseriesValue=" + timeseriesValueList + ", timeseriesSlotIndex=" + timeseriesSlotIndex + ", from=" + from + ", to=" + to + ", successCount=" + successCount + ", failedCount=" + failedCount + "]";
	}
}