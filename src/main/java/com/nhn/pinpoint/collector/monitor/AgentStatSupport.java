package com.nhn.pinpoint.collector.monitor;

import com.nhn.pinpoint.thrift.dto.*;
import com.nhn.pinpoint.thrift.dto.AgentStat._Fields;

/**
 * AgentStat 메시지에서 agentId, timestamp 등의 공통 정보를 꺼내올 수 있는 유틸리티.
 * AgentStat에 포함될 struct들이 agentId, timestamp 필드를 가지고 있다고 가정한다.
 *
 * @author harebox
 */
public class AgentStatSupport {

    public static String getAgentId(AgentStat agentStat) {
        _Fields type = agentStat.getSetField();
        Object typeObject = agentStat.getFieldValue();
        switch (type) {
            case CMS:
                return ((StatWithCmsCollector) typeObject).getAgentId();
            case G1:
                return ((StatWithG1Collector) typeObject).getAgentId();
            case PARALLEL:
                return ((StatWithParallelCollector) typeObject).getAgentId();
            case SERIAL:
                return ((StatWithSerialCollector) typeObject).getAgentId();
            default:
                throw new RuntimeException("type not found. type:" + type);
        }
    }

    public static long getTimestamp(AgentStat agentStat) {
        _Fields type = agentStat.getSetField();
        Object typeObject = agentStat.getFieldValue();
        switch (type) {
            case CMS:
                return ((StatWithCmsCollector) typeObject).getTimestamp();
            case G1:
                return ((StatWithG1Collector) typeObject).getTimestamp();
            case PARALLEL:
                return ((StatWithParallelCollector) typeObject).getTimestamp();
            case SERIAL:
                return ((StatWithSerialCollector) typeObject).getTimestamp();
            default:
                throw new RuntimeException("type not found. type:" + type);
        }
    }

}
