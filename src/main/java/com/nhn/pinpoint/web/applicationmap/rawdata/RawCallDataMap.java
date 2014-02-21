package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.vo.LinkKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public class RawCallDataMap {

//    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<LinkKey, RawCallData> rawCallDataMap = new HashMap<LinkKey, RawCallData>();


    public void addCallData(String sourceAgentId, short sourceServiceType, String targetId, short targetServiceType, short slot, long count) {
        addCallData(sourceAgentId, ServiceType.findServiceType(sourceServiceType), targetId, ServiceType.findServiceType(targetServiceType), slot, count);
    }

    public void addCallData(String sourceAgentId, ServiceType sourceServiceType, String targetId, ServiceType targetServiceType, short slot, long count) {
        LinkKey linkKey = createLinkKey(sourceAgentId, sourceServiceType, targetId, targetServiceType);
        RawCallData rawCallData = getRawCallData(linkKey);

        final Histogram histogram = rawCallData.getHistogram();
        histogram.addCallCount(slot, count);
    }

    public LinkKey createLinkKey(String sourceAgentId, ServiceType sourceServiceType, String targetId, ServiceType targetServiceType) {
        return new LinkKey(sourceAgentId, sourceServiceType, targetId, targetServiceType);
    }

    public void addCallData(RawCallDataMap target) {
        for (Map.Entry<LinkKey, RawCallData> copyEntry : target.rawCallDataMap.entrySet()) {
            final LinkKey key = copyEntry.getKey();
            final RawCallData value = copyEntry.getValue();
            RawCallData rawCallData = getRawCallData(key);
            rawCallData.getHistogram().add(value.getHistogram());
        }


    }

    private RawCallData getRawCallData(LinkKey key) {
        final Map<LinkKey, RawCallData> rawCallDataMap = this.rawCallDataMap;
        RawCallData rawCallData = rawCallDataMap.get(key);
        if (rawCallData == null) {
            rawCallData = new RawCallData(key);
            rawCallDataMap.put(key, rawCallData);
        }
        return rawCallData;
    }

    public CallHistogramList getTargetList() {
        CallHistogramList targetList = new CallHistogramList();
        for (Map.Entry<LinkKey, RawCallData> linkKeyRawCallDataEntry : rawCallDataMap.entrySet()) {
            final LinkKey key = linkKeyRawCallDataEntry.getKey();
            final RawCallData rawCallData = linkKeyRawCallDataEntry.getValue();
            targetList.addHost(key.getToApplication(), key.getToServiceType(), rawCallData.getHistogram());
        }
        return targetList;
    }

    public CallHistogramList getSourceList() {
        CallHistogramList sourceList = new CallHistogramList();
        for (Map.Entry<LinkKey, RawCallData> linkKeyRawCallDataEntry : rawCallDataMap.entrySet()) {
            final LinkKey key = linkKeyRawCallDataEntry.getKey();
            final RawCallData rawCallData = linkKeyRawCallDataEntry.getValue();
            sourceList.addHostUncheck(key.getFromApplication(), key.getFromServiceType(), rawCallData.getHistogram());
        }
        return sourceList;
    }
}
