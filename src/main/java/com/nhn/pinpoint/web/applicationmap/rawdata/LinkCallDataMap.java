package com.nhn.pinpoint.web.applicationmap.rawdata;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.vo.LinkKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public class LinkCallDataMap {

//    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<LinkKey, LinkCallData> rawCallDataMap = new HashMap<LinkKey, LinkCallData>();

    public LinkCallDataMap() {
    }

    public LinkCallDataMap(LinkCallDataMap copyLinkCallDataMap) {
        if (copyLinkCallDataMap == null) {
            throw new NullPointerException("copyLinkCallDataMap must not be null");
        }
        addCallData(copyLinkCallDataMap);
    }

    public void addCallData(String sourceAgentId, short sourceServiceType, String targetId, short targetServiceType, long timestamp, short slot, long count) {
        addCallData(sourceAgentId, ServiceType.findServiceType(sourceServiceType), targetId, ServiceType.findServiceType(targetServiceType), timestamp, slot, count);
    }

    public void addCallData(String sourceAgentId, ServiceType sourceServiceType, String targetId, ServiceType targetServiceType, long timestamp, short slot, long count) {
        LinkKey linkKey = createLinkKey(sourceAgentId, sourceServiceType, targetId, targetServiceType);
        LinkCallData linkCallData = getRawCallData(linkKey);
        linkCallData.addCallData(timestamp, slot, count);
    }

    private LinkKey createLinkKey(String sourceAgentId, ServiceType sourceServiceType, String targetId, ServiceType targetServiceType) {
        return new LinkKey(sourceAgentId, sourceServiceType, targetId, targetServiceType);
    }

    public void addCallData(LinkCallDataMap target) {
        if (target == null) {
            throw new NullPointerException("target must not be null");
        }
        for (Map.Entry<LinkKey, LinkCallData> copyEntry : target.rawCallDataMap.entrySet()) {
            final LinkKey key = copyEntry.getKey();
            final LinkCallData copyLinkCallData = copyEntry.getValue();
            LinkCallData linkCallData = getRawCallData(key);
            linkCallData.addRawCallData(copyLinkCallData);
        }

    }

    private LinkCallData getRawCallData(LinkKey key) {
        final Map<LinkKey, LinkCallData> rawCallDataMap = this.rawCallDataMap;
        LinkCallData linkCallData = rawCallDataMap.get(key);
        if (linkCallData == null) {
            linkCallData = new LinkCallData(key);
            rawCallDataMap.put(key, linkCallData);
        }
        return linkCallData;
    }

    public Collection<LinkCallData> getRawCallDataMap() {
        return rawCallDataMap.values();
    }

    public CallHistogramList getTargetList() {
        CallHistogramList targetList = new CallHistogramList();
        for (Map.Entry<LinkKey, LinkCallData> linkKeyRawCallDataEntry : rawCallDataMap.entrySet()) {
            final LinkKey key = linkKeyRawCallDataEntry.getKey();
            final LinkCallData linkCallData = linkKeyRawCallDataEntry.getValue();
            targetList.addCallHistogram(key.getToApplication(), key.getToServiceType(), linkCallData.getTimeHistogram());
        }
        return targetList;
    }

    public CallHistogramList getSourceList() {
        CallHistogramList sourceList = new CallHistogramList();
        for (Map.Entry<LinkKey, LinkCallData> linkKeyRawCallDataEntry : rawCallDataMap.entrySet()) {
            final LinkKey key = linkKeyRawCallDataEntry.getKey();
            final LinkCallData linkCallData = linkKeyRawCallDataEntry.getValue();
            // to의 ServiceType이 들어가야 한다.
            // 여기서 source란 source의 입장에서 target 호출시의 데이터를 의미하는 것이기 때문에. ServiceType자체는 To의 ServiceType이 들어가야한다.
            sourceList.addCallHistogram(key.getFromApplication(), key.getToServiceType(), linkCallData.getTimeHistogram());
        }
        return sourceList;
    }

    @Override
    public String toString() {
        return "LinkCallDataMap{" +
                "rawCallDataMap=" + rawCallDataMap +
                '}';
    }
}
