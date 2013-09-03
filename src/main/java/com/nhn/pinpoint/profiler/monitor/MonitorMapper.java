package com.nhn.pinpoint.profiler.monitor;

import com.nhn.pinpoint.common.dto.thrift.AgentStat;
import com.nhn.pinpoint.profiler.monitor.MonitorRegistry;

/**
 * FIXME common에 동일한 인터페이스가 존재. DTO가 common, profiler 따로따로 있기 때문에
 * 일단 복제본을 여기에 둔다. 
 */
public interface MonitorMapper {

	String convertName(String name);
	
	void map(final MonitorRegistry registry, final AgentStat agentStat);
	
}
