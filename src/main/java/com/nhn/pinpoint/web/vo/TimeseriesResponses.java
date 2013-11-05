package com.nhn.pinpoint.web.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.util.TimeWindowUtils;

/**
 * <pre>
 * 여러 개 application의 request에 대한 시간별 응답시간 분포를 저장하는 클래스.
 * thread safe하지 않음.
 * 
 * toJson()을 호출하면 다음 JSON을 반환한다.
 * 
 * {
 *     key : {
 *         id1 : value의 인덱스,
 *         id2 : value의 인덱스,
 *         idN : value의 인덱스, ...
 *     }  
 *     time : [ value의 시간1, 시간2, 시간3, ... , 시간N ],
 *     values : [
 *         [ 시간1의 값, 시간2의 값, 시간3의 값, ... , 시간N의 값],
 *         [ ... ], ...
 *     ]
 * }
 * </pre>
 * 
 * @author netspider
 * 
 */
public class TimeseriesResponses {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final List<Long> time = new ArrayList<Long>();

	/**
	 * key = id value = value list
	 */
	private final Map<String, List<Long>> values = new HashMap<String, List<Long>>();

	private final long from;
	private final long to;
	private final int windowSize;
	private final int windowCount;

	private String json = null;

	public TimeseriesResponses(long from, long to) {
		this.from = from;
		this.to = to;
		windowSize = TimeWindowUtils.getWindowSize(from, to);
		windowCount = (int) (to - from) / windowSize;

		logger.debug("window size {}, window count {}", windowSize, windowCount);

		for (long t = from; t <= to; t += windowSize) {
			this.time.add(t);
		}
	}

	private List<Long> makeDefaultValueList() {
		List<Long> list = new ArrayList<Long>(windowCount);
		for (int i = 0; i < windowCount; i++) {
			list.add(0L);
		}
		return list;
	}

	public void add(String id, long timestamp, int responseTime, long count) {
		logger.debug("add sample id={}, timestamp={} responseTime={}, count={}", id, timestamp, responseTime, count);
		
		List<Long> list = values.get(id);

		if (list == null) {
			list = makeDefaultValueList();
		}

		int index = TimeWindowUtils.getWindowIndex(from, windowSize, timestamp);

		long value = list.get(index) + count;

		list.set(index, value);
		
		values.put(id, list);
	}

	public String getJson() {
		if (json != null) {
			return json;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{\"values\":{");
		Iterator<Entry<String, List<Long>>> entryIterator = values.entrySet().iterator();
		while (entryIterator.hasNext()) {
			Entry<String, List<Long>> entry = entryIterator.next();
			sb.append("\"").append(entry.getKey()).append("\":[");
			Iterator<Long> valueIterator = entry.getValue().iterator();
			while (valueIterator.hasNext()) {
				sb.append(valueIterator.next());
				if (valueIterator.hasNext()) {
					sb.append(",");
				}
			}
			sb.append("]");
			if (entryIterator.hasNext()) {
				sb.append(",");
			}
		}
		sb.append("},\"time\":[");
		Iterator<Long> timeIterator = time.iterator();
		while (timeIterator.hasNext()) {
			sb.append(timeIterator.next());
			if (timeIterator.hasNext()) {
				sb.append(",");
			}
		}
		sb.append("]}");
		return json = sb.toString();
	}

	@Override
	public String toString() {
		return "TimeseriesResponses [from=" + from + ", to=" + to + ", windowSize=" + windowSize + ", windowCount=" + windowCount + ", time=" + time + ", json=" + json + "]";
	}
}
