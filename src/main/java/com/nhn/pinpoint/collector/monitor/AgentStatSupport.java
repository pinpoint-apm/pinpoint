package com.nhn.pinpoint.collector.monitor;

import com.nhn.pinpoint.thrift.dto.TAgentStat;

/**
 * AgentStat 메시지에서 agentId, timestamp 등의 공통 정보를 꺼내올 수 있는 유틸리티.
 * AgentStat에 포함될 struct들이 agentId, timestamp 필드를 가지고 있다고 가정한다.
 *
 * @author harebox
 */
@Deprecated
public class AgentStatSupport {

	@Deprecated
    public static String getAgentId(TAgentStat agentStat) {
        if (agentStat == null) {
            throw new NullPointerException("agentStat must not be null");
        }
//        _Fields type = agentStat.getSetField();
//        Object typeObject = agentStat.getFieldValue();
//        switch (type) {
//            case CMS:
//                return ((TStatWithCmsCollector) typeObject).getAgentId();
//            case G1:
//                return ((TStatWithG1Collector) typeObject).getAgentId();
//            case PARALLEL:
//                return ((TStatWithParallelCollector) typeObject).getAgentId();
//            case SERIAL:
//                return ((TStatWithSerialCollector) typeObject).getAgentId();
//            default:
//                throw new RuntimeException("type not found. type:" + type);
//        }
    	return agentStat.getAgentId();
    }

	@Deprecated
    public static long getTimestamp(TAgentStat agentStat) {
        if (agentStat == null) {
            throw new NullPointerException("agentStat must not be null");
        }
//        _Fields type = agentStat.getSetField();
//        Object typeObject = agentStat.getFieldValue();
//        switch (type) {
//            case CMS:
//                return ((TStatWithCmsCollector) typeObject).getTimestamp();
//            case G1:
//                return ((TStatWithG1Collector) typeObject).getTimestamp();
//            case PARALLEL:
//                return ((TStatWithParallelCollector) typeObject).getTimestamp();
//            case SERIAL:
//                return ((TStatWithSerialCollector) typeObject).getTimestamp();
//            default:
//                throw new RuntimeException("type not found. type:" + type);
//        }
        return agentStat.getTimestamp();
    }

}
