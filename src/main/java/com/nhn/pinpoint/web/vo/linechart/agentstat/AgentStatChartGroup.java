package com.nhn.pinpoint.web.vo.linechart.agentstat;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TAgentStat._Fields;
import com.nhn.pinpoint.thrift.dto.TStatWithCmsCollector;
import com.nhn.pinpoint.thrift.dto.TStatWithG1Collector;
import com.nhn.pinpoint.thrift.dto.TStatWithParallelCollector;
import com.nhn.pinpoint.thrift.dto.TStatWithSerialCollector;
import com.nhn.pinpoint.web.vo.linechart.LineChart;
import com.nhn.pinpoint.web.vo.linechart.SampledLineChart;

/**
 * @author harebox
 */
public class AgentStatChartGroup {

	private String type;
	private Map<String, LineChart> charts = new HashMap<String, LineChart>();
	
	public void addData(TAgentStat data, int sampleRate) {
		if (data == null) {
			return;
		}

		// 먼저 메시지에 포함된 데이터 타입을 알아낸다.
		_Fields type = data.getSetField();
		Object typeObject = data.getFieldValue(type);
		
		// TODO profiler에서 한 것 처럼 리팩토링 해야 한다.
		switch (type) {
		case CMS:
			TStatWithCmsCollector cms = (TStatWithCmsCollector) typeObject;
			for (TStatWithCmsCollector._Fields each : TStatWithCmsCollector.metaDataMap.keySet()) {
				Object fieldValue = cms.getFieldValue(each);
				if (! (fieldValue instanceof Long)) {
					continue;
				}
				
				if (! charts.containsKey(each.getFieldName())) {
					charts.put(each.getFieldName(), new SampledLineChart(sampleRate));
				}
				
				LineChart chart = charts.get(each.getFieldName());
				chart.addPoint(new Long[]{ cms.getTimestamp(), (Long) fieldValue });
			}
			break;
		case G1:
			TStatWithG1Collector g1 = (TStatWithG1Collector) typeObject;
			for (TStatWithG1Collector._Fields each : TStatWithG1Collector.metaDataMap.keySet()) {
				Object fieldValue = g1.getFieldValue(each);
				if (! (fieldValue instanceof Long)) {
					continue;
				}
				
				if (! charts.containsKey(each.getFieldName())) {
					charts.put(each.getFieldName(), new SampledLineChart(sampleRate));
				}
				
				LineChart chart = charts.get(each.getFieldName());
				chart.addPoint(new Long[]{ g1.getTimestamp(), (Long) fieldValue });
			}
			break;
		case PARALLEL:
			TStatWithParallelCollector parallel = (TStatWithParallelCollector) typeObject;
			for (TStatWithParallelCollector._Fields each : TStatWithParallelCollector.metaDataMap.keySet()) {
				Object fieldValue = parallel.getFieldValue(each);
				if (! (fieldValue instanceof Long)) {
					continue;
				}
				
				if (! charts.containsKey(each.getFieldName())) {
					charts.put(each.getFieldName(), new SampledLineChart(sampleRate));
				}
				
				LineChart chart = charts.get(each.getFieldName());
				Long[] point = new Long[]{ parallel.getTimestamp(), (Long) fieldValue };
				chart.addPoint(point);
			}
			break;
		case SERIAL:
			TStatWithSerialCollector serial = (TStatWithSerialCollector) typeObject;
			for (TStatWithSerialCollector._Fields each : TStatWithSerialCollector.metaDataMap.keySet()) {
				Object fieldValue = serial.getFieldValue(each);
				if (! (fieldValue instanceof Long)) {
					continue;
				}
				
				if (! charts.containsKey(each.getFieldName())) {
					charts.put(each.getFieldName(), new SampledLineChart(sampleRate));
				}
				
				LineChart chart = charts.get(each.getFieldName());
				chart.addPoint(new Long[]{ serial.getTimestamp(), (Long) fieldValue });
			}
			break;
		}
		
		this.type = type.getFieldName();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, LineChart> getCharts() {
		return charts;
	}

	public void setCharts(Map<String, LineChart> charts) {
		this.charts = charts;
	}

}