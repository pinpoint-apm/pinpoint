package com.nhn.pinpoint.collector.monitor;

import com.nhn.pinpoint.common.dto2.thrift.AgentStat;
import com.nhn.pinpoint.common.dto2.thrift.StatWithCmsCollector;
import com.nhn.pinpoint.common.dto2.thrift.StatWithG1Collector;
import com.nhn.pinpoint.common.dto2.thrift.StatWithParallelCollector;
import com.nhn.pinpoint.common.dto2.thrift.AgentStat._Fields;

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
