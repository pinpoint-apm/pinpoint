package com.nhn.pinpoint.web.vo.linechart.agentstat;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.thrift.dto.TAgentStat;
import com.nhn.pinpoint.thrift.dto.TJvmGc;
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
		TJvmGc gc = data.getGc();
		
		for (TJvmGc._Fields each : TJvmGc.metaDataMap.keySet()) {
			Object fieldValue = gc.getFieldValue(each);
			if (! (fieldValue instanceof Long)) {
				continue;
			}
			
			if (! charts.containsKey(each.getFieldName())) {
				charts.put(each.getFieldName(), new SampledLineChart(sampleRate));
			}
			
			LineChart chart = charts.get(each.getFieldName());
			chart.addPoint(new Long[]{ data.getTimestamp(), (Long) fieldValue });
		}
		
		this.type = gc.getType().name();
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