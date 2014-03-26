package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.vo.LinkKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public class RawCallDataMap {

//    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<LinkKey, RawCallData> rawCallDataMap = new HashMap<LinkKey, RawCallData>();

    public RawCallDataMap() {
    }

    public RawCallDataMap(RawCallDataMap copyRawCallDataMap) {
        if (copyRawCallDataMap == null) {
            throw new NullPointerException("copyRawCallDataMap must not be null");
        }
        addCallData(copyRawCallDataMap);
    }

    public void addCallData(String sourceAgentId, short sourceServiceType, String targetId, short targetServiceType, long timestamp, short slot, long count) {
        addCallData(sourceAgentId, ServiceType.findServiceType(sourceServiceType), targetId, ServiceType.findServiceType(targetServiceType), timestamp, slot, count);
    }

    public void addCallData(String sourceAgentId, ServiceType sourceServiceType, String targetId, ServiceType targetServiceType, long timestamp, short slot, long count) {
        LinkKey linkKey = createLinkKey(sourceAgentId, sourceServiceType, targetId, targetServiceType);
        RawCallData rawCallData = getRawCallData(linkKey);
        rawCallData.addCallData(timestamp, slot, count);
    }

    private LinkKey createLinkKey(String sourceAgentId, ServiceType sourceServiceType, String targetId, ServiceType targetServiceType) {
        return new LinkKey(sourceAgentId, sourceServiceType, targetId, targetServiceType);
    }

    public void addCallData(RawCallDataMap target) {
        if (target == null) {
            throw new NullPointerException("target must not be null");
        }
        for (Map.Entry<LinkKey, RawCallData> copyEntry : target.rawCallDataMap.entrySet()) {
            final LinkKey key = copyEntry.getKey();
            final RawCallData copyRawCallData = copyEntry.getValue();
            RawCallData rawCallData = getRawCallData(key);
            rawCallData.addRawCallData(copyRawCallData);
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

    public Collection<RawCallData> getRawCallDataMap() {
        return rawCallDataMap.values();
    }

    public CallHistogramList getTargetList() {
        CallHistogramList targetList = new CallHistogramList();
        for (Map.Entry<LinkKey, RawCallData> linkKeyRawCallDataEntry : rawCallDataMap.entrySet()) {
            final LinkKey key = linkKeyRawCallDataEntry.getKey();
            final RawCallData rawCallData = linkKeyRawCallDataEntry.getValue();
            targetList.addCallHistogram(key.getToApplication(), key.getToServiceType(), rawCallData.getTimeHistogram());
        }
        return targetList;
    }

    public CallHistogramList getSourceList() {
        CallHistogramList sourceList = new CallHistogramList();
        for (Map.Entry<LinkKey, RawCallData> linkKeyRawCallDataEntry : rawCallDataMap.entrySet()) {
            final LinkKey key = linkKeyRawCallDataEntry.getKey();
            final RawCallData rawCallData = linkKeyRawCallDataEntry.getValue();
            // to의 ServiceType이 들어가야 한다.
            // 여기서 source란 source의 입장에서 target 호출시의 데이터를 의미하는 것이기 때문에. ServiceType자체는 To의 ServiceType이 들어가야한다.
            sourceList.addCallHistogram(key.getFromApplication(), key.getToServiceType(), rawCallData.getTimeHistogram());
        }
        return sourceList;
    }

    @Override
    public String toString() {
        return "RawCallDataMap{" +
                "rawCallDataMap=" + rawCallDataMap +
                '}';
    }
}
