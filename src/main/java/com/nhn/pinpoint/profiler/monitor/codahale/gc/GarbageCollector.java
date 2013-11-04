package com.nhn.pinpoint.profiler.monitor.codahale.gc;

import com.nhn.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.nhn.pinpoint.thrift.dto.TAgentStat;

/**
 * @author harebox
 */
public class GarbageCollector {

	protected GarbageCollectorType type;

	public int getType() {
		return type.getTypeCode();
	}
	
	/**
	 * 가비지 컬렉터 타입을 지정한다.
	 */
	public void setType(int type) {
		this.type = GarbageCollectorType.newType(type);
	}
	
	/**
	 * Metrics 통계 데이터를 이용하여 가비지 컬렉터 타입을 지정한다.
	 */
	public void setType(MetricMonitorRegistry registry) {
		this.type = GarbageCollectorType.newType(registry);
	}
	
	/**
	 * AgentStat 객체에 통계 데이터를 매핑한다.
	 */
	public void map(MetricMonitorRegistry registry, TAgentStat agentStat, String agentId) {
		if (type == null || registry == null) {
			return;
		}
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

		type.map(registry, agentStat, agentId);
	}
	
	public String toString() {
		return "GarbageCollector[" + type.toString() + "]";
	}
	
}
