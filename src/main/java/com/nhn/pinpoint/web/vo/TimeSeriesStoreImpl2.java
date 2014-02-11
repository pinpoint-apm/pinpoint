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

	private final Range range;
	private final Map<NodeId, LoadFactor> data = new HashMap<NodeId, LoadFactor>();
	private final boolean enabled;

	public TimeSeriesStoreImpl2(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.range = range;
		this.enabled = range.getFrom() == -1L || range.getTo() == -1L;
	}

	private LoadFactor makeNewLinkStatistics() {
		return new LoadFactor(range);
	}

	@Override
	public void add(NodeId key, long timestamp, int responseTimeslot, long callCount, boolean isFailed) {
		if (!enabled) {
			return;
		}
		logger.debug("add sample key={}, timestamp={} responseTimeSlot={}, count={}", key, timestamp, responseTimeslot, callCount, isFailed);

		LoadFactor stat = data.get(key);

		if (stat == null) {
			stat = makeNewLinkStatistics();
			data.put(key, stat);
		}

		stat.addSample(timestamp, responseTimeslot, callCount, isFailed);
	}

	@Override
	public String getJson() {
		if (!enabled) {
			logger.debug("store is not enabled. there's no data.");
			return EMPTY;
		}
		return "{}";
	}

}
