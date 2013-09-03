package com.nhn.pinpoint.profiler.monitor.codahale;

import java.security.InvalidParameterException;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.nhn.pinpoint.common.dto.thrift.AgentStat;
import com.nhn.pinpoint.common.dto.thrift.AgentStat._Fields;
import com.nhn.pinpoint.common.dto.thrift.StatWithCmsCollector;
import com.nhn.pinpoint.common.dto.thrift.StatWithG1Collector;
import com.nhn.pinpoint.common.dto.thrift.StatWithParallelCollector;
import com.nhn.pinpoint.profiler.monitor.MonitorMapper;
import com.nhn.pinpoint.profiler.monitor.MonitorRegistry;

/**
 * FIXME Thrift DTO가 두 벌(common, profiler)이기 때문에 임시로 복제본을 두었음.
 * 
 * @author harebox
 */
public class MetricMonitorMapper implements MonitorMapper {

	/**
	 * FIXME 성능 개선을 위해 캐싱해 둘 필요 있음.
	 */
	public String convertName(String name) {
		return name.toLowerCase().replace("non_", "non-").replace("_", ".");
	}
	
	/**
	 * @param registry
	 * @param agentStat
	 */
	@SuppressWarnings("rawtypes")
	public void map(final MonitorRegistry registry, final AgentStat agentStat) {
		if (agentStat == null) {
			throw new NullPointerException("AgentStat is null");
		}
		
		if (agentStat.getSetField() == null) {
			throw new NullPointerException("AgentStat has no statistics");
		}
		
		if (!(registry instanceof MetricMonitorRegistry)) {
			throw new InvalidParameterException("not a MetricMonitorRegistry : " + registry.getClass());
		}
		
		MetricRegistry r = ((MetricMonitorRegistry) registry).getRegistry();
		Map<String, Gauge> map = r.getGauges();
		
		if (map == null) {
			return;
		}
		
		// 메시지 타입과 해당 타입에 따른 객체를 얻어온다.
		_Fields type = agentStat.getSetField();
		Object typeObject = agentStat.getFieldValue(type);
		long timestamp = System.currentTimeMillis();
		
		// 타입에 따라 필요한 값을 매핑한다. FIXME 더 좋은 방법 없나?
		switch (type) {
		case CMS:
			StatWithCmsCollector cms = (StatWithCmsCollector) typeObject;
			cms.setTimestamp(timestamp);
			for (StatWithCmsCollector._Fields each : StatWithCmsCollector.metaDataMap.keySet()) {
				Gauge value = map.get(convertName(each.name()));
				if (value != null) {
					cms.setFieldValue(each, value.getValue());
				}
			}
			break;
		case G1:
			StatWithG1Collector g1 = (StatWithG1Collector) typeObject;
			g1.setTimestamp(timestamp);
			for (StatWithG1Collector._Fields each : StatWithG1Collector.metaDataMap.keySet()) {
				Gauge value = map.get(convertName(each.name()));
				if (value != null) {
					g1.setFieldValue(each, value.getValue());
				}
			}
			break;
		case PARALLEL:
			StatWithParallelCollector parallel = (StatWithParallelCollector) typeObject;
			parallel.setTimestamp(timestamp);
			for (StatWithParallelCollector._Fields each : StatWithParallelCollector.metaDataMap.keySet()) {
				Gauge value = map.get(convertName(each.name()));
				if (value != null) {
					parallel.setFieldValue(each, value.getValue());
				}
			}
			break;
		}
	}
	
}
