package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;

/**
 * application의 host정보.
 * 
 * @author netspider
 * 
 */
public class Host {
	/**
	 * UI에서 호스트를 구분하기 위한 목적으로 hostname, agentid, endpoint등 구분할 수 있는 아무거나 넣으면 됨.
	 */
	private final String host;
	private final AgentInfoBo agentInfo;
	private final ResponseHistogram histogram;

	public Host(String host, ServiceType serviceType, AgentInfoBo agentInfo) {
		this.host = host;
		this.histogram = new ResponseHistogram(serviceType);
		this.agentInfo = agentInfo;
	}

	public String getHost() {
		return host;
	}

	public AgentInfoBo getAgentInfo() {
		return agentInfo;
	}

	public ResponseHistogram getHistogram() {
		return histogram;
	}

	public void mergeWith(Host host) {
		this.histogram.mergeWith(host.getHistogram());
	}

	public String getJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\"").append(host).append("\",");
		if (agentInfo != null) {
			sb.append("\"agentInfo\":").append(agentInfo.getJson()).append(",");
		} else {
			sb.append("\"agentInfo\":").append("null").append(",");
		}
		// sb.append("\"callCount\":").append(histogram.getTotalCount()).append(",");
		// sb.append("\"error\":").append(histogram.getErrorCount()).append(",");
		// sb.append("\"slow\":").append(histogram.getSlowCount()).append(",");
		sb.append("\"histogram\":").append(histogram);
		sb.append("}");
		return sb.toString();
	}
}
