package com.nhn.pinpoint.web.vo.linechart;

import java.util.HashMap;
import java.util.Map;

import com.nhn.pinpoint.thrift.dto.AgentStat;
import com.nhn.pinpoint.thrift.dto.StatWithCmsCollector;
import com.nhn.pinpoint.thrift.dto.AgentStat._Fields;

/**
 * FIXME 일반화 해야 할까?
 * @author harebox
 */
public class AgentStatLineChart {

	private String type;
	private Map<String, TimestampToValue> charts = new HashMap<String, TimestampToValue>();
	
	public void addData(AgentStat data) {
		if (data == null) {
			return;
		}

		// 먼저 메시지에 포함된 데이터 타입을 알아낸다.
		_Fields type = data.getSetField();
		Object typeObject = data.getFieldValue(type);
		
		switch (type) {
		case CMS:
			StatWithCmsCollector stat = (StatWithCmsCollector) typeObject;
			for (StatWithCmsCollector._Fields each : StatWithCmsCollector.metaDataMap.keySet()) {
				Object fieldValue = stat.getFieldValue(each);
				if (! (fieldValue instanceof Long)) {
					continue;
				}
				
				if (! charts.containsKey(each.getFieldName())) {
					charts.put(each.getFieldName(), new TimestampToValue());
				}
				
				TimestampToValue chart = charts.get(each.getFieldName());
				chart.addData(stat.getTimestamp(), (Long) fieldValue);
			}
			break;
		case G1:
			// FIXME 아직 구현되지 않음.
			break;
		case PARALLEL:
			// FIXME 아직 구현되지 않음.
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

	public Map<String, TimestampToValue> getCharts() {
		return charts;
	}

	public void setCharts(Map<String, TimestampToValue> charts) {
		this.charts = charts;
	}

}