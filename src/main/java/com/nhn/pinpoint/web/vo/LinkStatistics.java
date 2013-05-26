package com.nhn.pinpoint.web.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.profiler.common.HistogramSlot;

/**
 * 
 * @author netspider
 * 
 */
public class LinkStatistics {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final int SUCCESS = 0;
	private static final int FAILED = 1;
	private static final int SLOW = Integer.MAX_VALUE - 1;
	private static final int ERROR = Integer.MAX_VALUE;

	/**
	 * <pre>
	 * key = responseTimeslot
	 * value = count
	 * </pre>
	 */
	private final SortedMap<Integer, Long> histogramSummary = new TreeMap<Integer, Long>();;

	/**
	 * <pre>
	 * key = timeslot
	 * value = { responseTimeslot, count }
	 * </pre>
	 */
	private final Map<Long, SortedMap<Integer, Long>> timeseriesHistogram = new TreeMap<Long, SortedMap<Integer, Long>>();;

	/**
	 * <pre>
	 * key = timeslot
	 * value = long[0]:success, long[1]:failed
	 * </pre>
	 */
	private final Map<Long, Long[]> timeseriesFaileureHistogram = new TreeMap<Long, Long[]>();;

	private long successCount = 0;
	private long failedCount = 0;
	private final List<Integer> histogramSlotList = new ArrayList<Integer>();

	/**
	 * view에서 값이 없는 slot도 보여주기위해서..
	 * 
	 * @return
	 */
	private SortedMap<Integer, Long> makeDefaultHistogram() {
		SortedMap<Integer, Long> map = new TreeMap<Integer, Long>();
		for (int key : histogramSlotList) {
			map.put(key, 0L);
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

		histogramSlotList.clear();
		histogramSummary.clear();

		// -1 is failed
		histogramSlotList.add(ERROR);
		histogramSummary.put(ERROR, 0L);

		// 0 is slow
		histogramSlotList.add(SLOW);
		histogramSummary.put(SLOW, 0L);

		for (HistogramSlot slot : slotList) {
			histogramSlotList.add(slot.getSlotTime());
			histogramSummary.put(slot.getSlotTime(), 0L);
		}
	}

	public void addSample(long timeslot, int responseTimeslot, long callCount, boolean failed) {
		logger.info("Add sample. timeslot=" + timeslot + ", responseTimeslot=" + responseTimeslot + ", callCount=" + callCount + ", failed=" + failed);
		
		if (failed) {
			failedCount += callCount;
		} else {
			successCount += callCount;
		}

		if (responseTimeslot == -1) {
			responseTimeslot = ERROR;
		} else if (responseTimeslot == 0) {
			responseTimeslot = SLOW;
		}

		// add summary
		long value = histogramSummary.containsKey(responseTimeslot) ? histogramSummary.get(responseTimeslot) : 0L;
		histogramSummary.put(responseTimeslot, value + callCount);

		// add timeseries histogram
		if (timeseriesHistogram.containsKey(timeslot)) {
			SortedMap<Integer, Long> eachResponseHistogram = timeseriesHistogram.get(timeslot);
			long count = eachResponseHistogram.containsKey(responseTimeslot) ? eachResponseHistogram.get(responseTimeslot) : 0L;
			eachResponseHistogram.put(responseTimeslot, count + callCount);
		} else {
			SortedMap<Integer, Long> map = makeDefaultHistogram();
			map.put(responseTimeslot, callCount);
			timeseriesHistogram.put(timeslot, map);
		}

		// add failure rate histogram
		if (timeseriesFaileureHistogram.containsKey(timeslot)) {
			Long[] array = timeseriesFaileureHistogram.get(timeslot);

			if (failed) {
				array[FAILED] += callCount;
			} else {
				array[SUCCESS] += callCount;
			}
			// timeseriesFaileureRateHistogram.put(timeslot, array);
		} else {
			Long[] array = new Long[2];
			array[SUCCESS] = 0L;
			array[FAILED] = 0L;

			if (failed) {
				array[FAILED] += callCount;
			} else {
				array[SUCCESS] += callCount;
			}
			timeseriesFaileureHistogram.put(timeslot, array);
		}
	}

	public Map<Integer, Long> getHistogramSummary() {
		return histogramSummary;
	}

	public Map<Long, SortedMap<Integer, Long>> getTimeseriesHistogram() {
		return timeseriesHistogram;
	}

	public Map<Long, Long[]> getTimeseriesFaileureHistogram() {
		return timeseriesFaileureHistogram;
	}

	public long getSuccessCount() {
		return successCount;
	}

	public long getFailedCount() {
		return failedCount;
	}

	public int getSlow() {
		return SLOW;
	}

	public int getError() {
		return ERROR;
	}

	@Override
	public String toString() {
		return "LinkStatistics [histogramSummary=" + histogramSummary + ", timeseriesHistogram=" + timeseriesHistogram + ", timeseriesFaileureRateHistogram=" + timeseriesFaileureHistogram + ", successCount=" + successCount + ", failedCount=" + failedCount + ", histogramSlotList=" + histogramSlotList + "]";
	}
}