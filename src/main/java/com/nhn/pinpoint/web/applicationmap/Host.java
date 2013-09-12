package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.common.ServiceType;

/**
 * 
 * @author netspider
 * 
 */
public class Host {
	/**
	 * UI에서 호스트를 구분하기 위한 목적으로 hostname, agentid, endpoint등 구분할 수 있는 아무거나 넣으면 됨.
	 */
	private final String host;
	private final ResponseHistogram histogram;

	public Host(String host, ServiceType serviceType) {
		this.host = host;
		this.histogram = new ResponseHistogram(serviceType);
	}

	public String getHost() {
		return host;
	}

	public ResponseHistogram getHistogram() {
		return histogram;
	}

	public void mergeWith(Host host) {
		this.histogram.mergeWith(host.histogram);
	}

	public String getJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"name\":\"").append(host).append("\",");
		sb.append("\"histogram\":").append(histogram); // .append(",");
		// sb.append("\"agentList\":[");
		// Iterator<AgentInfoBo> iterator = agentList.iterator();
		// while (iterator.hasNext()) {
		// AgentInfoBo agent = iterator.next();
		// sb.append(agent.getJson());
		// if (iterator.hasNext()) {
		// sb.append(",");
		// }
		// }
		sb.append("}");
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Host [host=" + host + ", histogram=" + histogram + "]";
	}
}
