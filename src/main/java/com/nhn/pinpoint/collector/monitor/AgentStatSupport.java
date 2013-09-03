package com.nhn.pinpoint.collector.monitor;

import com.nhn.pinpoint.thrift.dto.AgentStat;
import com.nhn.pinpoint.thrift.dto.StatWithCmsCollector;
import com.nhn.pinpoint.thrift.dto.StatWithG1Collector;
import com.nhn.pinpoint.thrift.dto.StatWithParallelCollector;
import com.nhn.pinpoint.thrift.dto.AgentStat._Fields;

/**
 * AgentStat 메시지에서 agentId, timestamp 등의 공통 정보를 꺼내올 수 있는 유틸리티.
 * AgentStat에 포함될 struct들이 agentId, timestamp 필드를 가지고 있다고 가정한다.
 * 
 * @author harebox
 */
public class AgentStatSupport {

	public static String getAgentId(AgentStat agentStat) {
		String result = null;
		
		_Fields type = agentStat.getSetField();
		Object typeObject = agentStat.getFieldValue();
		switch (type) {
		case CMS:
			result = ((StatWithCmsCollector) typeObject).getAgentId();
			break;
		case G1:
			result = ((StatWithG1Collector) typeObject).getAgentId();
			break;
		case PARALLEL:
			result = ((StatWithParallelCollector) typeObject).getAgentId();
			break;
		}
		
		return result;
	}
	
	public static long getTimestamp(AgentStat agentStat) {
		long result = 0;
		
		_Fields type = agentStat.getSetField();
		Object typeObject = agentStat.getFieldValue();
		switch (type) {
		case CMS:
			result = ((StatWithCmsCollector) typeObject).getTimestamp();
			break;
		case G1:
			result = ((StatWithG1Collector) typeObject).getTimestamp();
			break;
		case PARALLEL:
			result = ((StatWithParallelCollector) typeObject).getTimestamp();
			break;
		}
		
		return result;
	}
	
}
