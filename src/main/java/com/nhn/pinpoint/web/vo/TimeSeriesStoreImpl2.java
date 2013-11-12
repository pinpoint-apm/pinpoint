package com.nhn.pinpoint.web.vo;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.web.service.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class TimeSeriesStoreImpl2 implements TimeSeriesStore {

	private static final String EMPTY = "{}";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final long from;
	private final long to;
	private final Map<NodeId, LinkStatistics> data = new HashMap<NodeId, LinkStatistics>();
	private final boolean enabled;

	public TimeSeriesStoreImpl2(long from, long to) {
		this.from = from;
		this.to = to;
		this.enabled = from == -1L || to == -1L;
	}

	private LinkStatistics makeNewLinkStatistics() {
		return new LinkStatistics(from, to);
	}

	@Override
	public void add(NodeId key, long timestamp, int responseTimeslot, long callCount, boolean isFailed) {
		if (!enabled) {
			logger.debug("store is not enabled.");
			return;
		}
		logger.debug("add sample key={}, timestamp={} responseTimeSlot={}, count={}", key, timestamp, responseTimeslot, callCount, isFailed);

		LinkStatistics stat = data.get(key);

		if (stat == null) {
			stat = makeNewLinkStatistics();
			data.put(key, stat);
		}

		stat.addSample(timestamp, responseTimeslot, callCount, isFailed);
	}

	@Override
	public String getJson() {
		if (!enabled) {
			return EMPTY;
		}
		return "{}";
	}

}
