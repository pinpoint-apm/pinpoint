package com.nhn.pinpoint.web.vo;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class DefaultTimeSeriesStoreImpl implements TimeSeriesStore {

	private static final String EMPTY = "{}";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Range range;
	private final Map<LinkKey, LoadFactor> linkData = new HashMap<LinkKey, LoadFactor>();
    private final Map<Application, LoadFactor> nodeData = new HashMap<Application, LoadFactor>();

	private final boolean enabled;

	public DefaultTimeSeriesStoreImpl(Range range) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        this.range = range;
		this.enabled = range.getFrom() == -1L || range.getTo() == -1L;
	}


	@Override
	public void addLinkStat(LinkKey linkKey, long timeStamp, int responseTimeSlot, long callCount, boolean isFailed) {
        if (linkKey == null) {
            throw new NullPointerException("linkKey must not be null");
        }
        if (!enabled) {
			return;
		}
		logger.debug("add sample linkKey={}, timeStamp={} responseTimeSlot={}, count={}", linkKey, timeStamp, responseTimeSlot, callCount, isFailed);

		LoadFactor stat = linkData.get(linkKey);
		if (stat == null) {
			stat = new LoadFactor(range);
			linkData.put(linkKey, stat);
		}

		stat.addSample(timeStamp, responseTimeSlot, callCount, isFailed);
	}

    @Override
    public void addNodeStat(Application nodeKey, long timeStamp, int responseTimeSlot, long callCount, boolean isFailed) {
        if (nodeKey == null) {
            throw new NullPointerException("nodeKey must not be null");
        }
        if (!enabled) {
            return;
        }
        logger.debug("add sample nodeKey={}, timeStamp={} responseTimeSlot={}, count={}", nodeKey, timeStamp, responseTimeSlot, callCount, isFailed);

        LoadFactor stat = nodeData.get(nodeKey);
        if (stat == null) {
            stat = new LoadFactor(range);
            nodeData.put(nodeKey, stat);
        }
        stat.addSample(timeStamp, responseTimeSlot, callCount, isFailed);
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
