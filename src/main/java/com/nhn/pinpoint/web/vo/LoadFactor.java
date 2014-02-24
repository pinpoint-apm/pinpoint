package com.nhn.pinpoint.web.vo;

import java.util.*;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.web.util.TimeWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class LoadFactor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final Integer SLOT_VERY_SLOW = Integer.MAX_VALUE - 1;
	private static final Integer SLOT_ERROR = (int)Integer.MAX_VALUE;

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
	private final List<Map<Long, Long>> timeseriesValueList = new ArrayList<Map<Long, Long>>();
	private final Map<Integer, Integer> timeseriesSlotIndex = new TreeMap<Integer, Integer>();

    private final Range range;

	private long successCount = 0;
	private long failedCount = 0;

    private final TimeWindow timeWindow;

	public LoadFactor(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.range = range;
        this.timeWindow = new TimeWindow(range);
	}

	/**
	 * timeseries 기본값 채운다. 빈 공간은 그냥 적당히 채워준다. 모두 채우면 느리니까..
	 * 
	 * @return
	 */
	private Map<Long, Long> makeEmptyTimeseriesValueMap() {
		Map<Long, Long> map = new TreeMap<Long, Long>();
        for (Long time : timeWindow) {
            map.put(time, 0L);
        }
		return map;
	}

	/**
	 * histogram slot을 설정하면 view에서 값이 없는 slot의 값을 0으로 보여줄 수 있다. 설정되지 않으면 key를
	 * 몰라서 보여주지 못함. 입력된 값만 보이게 됨.
	 * 
	 * @param schema
	 */
	public void setDefaultHistogramSlotList(HistogramSchema schema) {
		if (successCount > 0 || failedCount > 0) {
			throw new IllegalStateException("Can't set slot list while containing the data.");
		}

//		histogramSummary.clear();
		timeseriesSlotIndex.clear();
		timeseriesValueList.clear();

//		histogramSummary.put(SLOT_VERY_SLOW, 0L);
//		histogramSummary.put(SLOT_ERROR, 0L);

        timeseriesSlotIndex.put((int)schema.getFastSlot().getSlotTime(), timeseriesSlotIndex.size());
        timeseriesValueList.add(makeEmptyTimeseriesValueMap());

        timeseriesSlotIndex.put((int)schema.getNormalSlot().getSlotTime(), timeseriesSlotIndex.size());
        timeseriesValueList.add(makeEmptyTimeseriesValueMap());

        timeseriesSlotIndex.put((int)schema.getSlowSlot().getSlotTime(), timeseriesSlotIndex.size());
        timeseriesValueList.add(makeEmptyTimeseriesValueMap());

		timeseriesSlotIndex.put(SLOT_VERY_SLOW, timeseriesSlotIndex.size());
		timeseriesSlotIndex.put(SLOT_ERROR, timeseriesSlotIndex.size());

		timeseriesValueList.add(makeEmptyTimeseriesValueMap());
		timeseriesValueList.add(makeEmptyTimeseriesValueMap());
	}

	public void addSample(long timestamp, int responseTimeslot, long callCount, boolean isFailed) {
        if (logger.isDebugEnabled()) {
		    logger.debug("Add sample. timeslot={}, responseTimeslot={}, callCount={}, failed={}", timestamp, responseTimeslot, callCount, isFailed);
        }

		timestamp = timeWindow.refineTimestamp(timestamp);

		if (isFailed) {
			failedCount += callCount;
		} else {
			successCount += callCount;
		}

		// TODO 이렇게 하는게 뭔가 좋지 않은것 같음.
		if (responseTimeslot == -1) {
			responseTimeslot = SLOT_ERROR;
		} else if (responseTimeslot == 0) {
			responseTimeslot = SLOT_VERY_SLOW;
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
			Map<Long, Long> map = timeseriesValueList.get(i);

			// 다른 slot에도 같은 시간이 존재해야한다.
			// FIXME responseTimeSlot의 자료형을 short으로 변경할 것.
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

	public Map<Integer, Integer> getTimeseriesSlotIndex() {
		return timeseriesSlotIndex;
	}

	public List<Map<Long, Long>> getTimeseriesValue() {
		return timeseriesValueList;
	}


	public int getVerySlow() {
		return SLOT_VERY_SLOW;
	}

	public int getError() {
		return SLOT_ERROR;
	}

	@Override
	public String toString() {
		return "LoadFactor [timeseriesValue=" + timeseriesValueList + ", timeseriesSlotIndex=" + timeseriesSlotIndex + ", range=" + range + ", successCount=" + successCount + ", failedCount=" + failedCount + "]";
	}
}